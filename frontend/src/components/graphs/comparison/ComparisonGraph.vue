<template>
  <v-card :style="{ height: height }">
    <v-card-text style="height: 100%" class="py-0 my-0">
      <component
        :is="graphComponent"
        :zoom-x-start-value.sync="zoomXStartValue"
        :zoom-x-end-value.sync="zoomXEndValue"
        :zoom-y-start-value.sync="zoomYStartValue"
        :zoom-y-end-value.sync="zoomYEndValue"
        :datapoints="comparisonDatapoints"
        :data-range-min.sync="dataRangeMin"
        :data-range-max.sync="dataRangeMax"
        :series-information="seriesInformation"
        :visible-point-count="visiblePointCount"
        :reference-datapoint="referenceDatapoint"
        :begin-y-at-zero="beginYAtZero"
        :refresh-key="graphRefreshKey"
        @wheel="overscrollToZoom.scrolled($event)"
        @reset-zoom="resetZoom"
      >
        <template
          #dialog="{
            dialogOpen,
            selectedDatapoint,
            seriesInformation,
            closeDialog,
          }"
        >
          <graph-datapoint-dialog
            :dialog-open="dialogOpen"
            :selected-datapoint="selectedDatapoint"
            :series-id="seriesInformation.id"
            :reference-datapoint.sync="referenceDatapoint"
            :no-compare="true"
            @close="closeDialog()"
          ></graph-datapoint-dialog>
        </template>
      </component>
      <v-overlay v-if="overlayText" absolute class="ma-0 pa-0" color="black">
        <span class="text-h6">{{ overlayText }}</span>
      </v-overlay>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import DytailGraph from "@/components/graphs/graph/DytailGraph.vue";
import {
  AttributedDatapoint,
  ComparisonDataPoint,
  SeriesId,
  SeriesInformation,
} from "@/store/types";
import { vxm } from "@/store";
import GraphDatapointDialog from "@/components/graphs/helper/GraphDatapointDialog.vue";
import OverscrollToZoom from "@/components/graphs/helper/OverscrollToZoom";
import { roundDateDown, roundDateUp } from "@/util/Times";
import EchartsDetailGraph from "@/components/graphs/graph/EchartsDetailGraph.vue";

export default defineComponent({
  components: {
    GraphDatapointDialog,
    EchartsDetailGraph,
    DytailGraph,
  },
  data() {
    const graphRefreshKey: number = 0;

    return {
      graphRefreshKey,
    };
  },
  computed: {
    overscrollToZoom() {
      return new OverscrollToZoom(() => {
        // Not needed as the RepoComparison will handle refresh
        // FIXME: Also move repo detail refreshes to parent component and remove this parameter
        return Promise.resolve();
      }, vxm.comparisonGraphModule);
    },
    seriesInformation(): SeriesInformation[] {
      return Array.from(vxm.comparisonGraphModule.selectedBranches.keys()).map(
        (repoId) => ({
          id: repoId as SeriesId,
          color: vxm.colorModule.colorForRepo(repoId),
          displayName: vxm.repoModule.repoById(repoId)!.name,
        }),
      );
    },
    dataRangeMin: {
      get() {
        return vxm.comparisonGraphModule.startTime;
      },
      set(date: Date) {
        vxm.comparisonGraphModule.startTime = roundDateDown(date);
      },
    },
    dataRangeMax: {
      get() {
        return vxm.comparisonGraphModule.endTime;
      },
      set(date: Date) {
        vxm.comparisonGraphModule.endTime = roundDateUp(date);
      },
    },
    referenceDatapoint: {
      get(): AttributedDatapoint | null {
        return vxm.comparisonGraphModule.referenceDatapoint;
      },
      set(datapoint: AttributedDatapoint | null) {
        vxm.comparisonGraphModule.referenceDatapoint = datapoint;
      },
    },
    visiblePointCount() {
      const startValue = this.zoomXStartValue;
      const endValue = this.zoomXEndValue;

      // TODO: Is this a performance problem? There might be 10.000+ items here
      // and this method is called every time the slider is dragged or the user
      // zooms using the mouse wheel
      let visibleDataPoints = 0;
      for (const point of this.comparisonDatapoints) {
        if (
          (startValue === null || point.positionTime.getTime() >= startValue) &&
          (endValue === null || point.positionTime.getTime() <= endValue)
        ) {
          visibleDataPoints++;
        }
      }
      return visibleDataPoints;
    },
    overlayText() {
      if (this.seriesInformation.length === 0) {
        return "Please select a repo, branch and dimension on the left";
      }
      if (!vxm.comparisonGraphModule.selectedDimension) {
        return "Please select a dimension in the top left";
      }
      return null;
    },
    zoomXStartValue: {
      get(): number | null {
        return vxm.comparisonGraphModule.zoomXStartValue;
      },
      set(value: number | null) {
        vxm.comparisonGraphModule.zoomXStartValue = value;
      },
    },
    zoomXEndValue: {
      get(): number | null {
        return vxm.comparisonGraphModule.zoomXEndValue;
      },
      set(value: number | null) {
        vxm.comparisonGraphModule.zoomXEndValue = value;
      },
    },
    zoomYStartValue: {
      get(): number | null {
        return vxm.comparisonGraphModule.zoomYStartValue;
      },
      set(value: number | null) {
        vxm.comparisonGraphModule.zoomYStartValue = value;
      },
    },
    zoomYEndValue: {
      get(): number | null {
        return vxm.comparisonGraphModule.zoomYEndValue;
      },
      set(value: number | null) {
        vxm.comparisonGraphModule.zoomYEndValue = value;
      },
    },
  },
  methods: {
    resetZoom() {
      this.zoomYStartValue = null;
      this.zoomYEndValue = null;
      this.zoomXStartValue = this.dataRangeMin.getTime();
      this.zoomXEndValue = this.dataRangeMax.getTime();
      this.graphRefreshKey++;
    },
  },
  props: {
    comparisonDatapoints: {
      type: Array as PropType<ComparisonDataPoint[]>,
    },
    graphComponent: {
      type: Object as PropType<typeof Vue>,
    },
    beginYAtZero: {
      type: Boolean,
    },
    height: { default: "80vh", type: String },
  },
});
</script>
