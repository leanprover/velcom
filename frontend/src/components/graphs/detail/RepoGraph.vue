<template>
  <v-card>
    <v-card-text style="height: 70vh">
      <v-row no-gutters style="height: 100%">
        <v-col style="position: relative; height: 100%">
          <component
            ref="graphComponent"
            :placeholderHeight="graphPlaceholderHeight"
            :is="selectedGraphComponent"
            :zoom-x-start-value.sync="zoomXStartValue"
            :zoom-x-end-value.sync="zoomXEndValue"
            :zoom-y-start-value.sync="zoomYStartValue"
            :zoom-y-end-value.sync="zoomYEndValue"
            :datapoints="datapoints"
            :data-range-min.sync="dataRangeMin"
            :data-range-max.sync="dataRangeMax"
            :series-information="seriesInformation"
            :visible-point-count="visiblePointCount"
            :point-table-formatter="pointFormatter"
            :reference-datapoint="referenceDatapoint"
            :commit-to-compare="commitToCompare"
            :begin-y-at-zero="yStartsAtZero"
            :stacked="stacked"
            :normalized="normalized"
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
                :commit-to-compare.sync="commitToCompare"
                :reference-datapoint.sync="referenceDatapoint"
                @close="closeDialog()"
              ></graph-datapoint-dialog>
            </template>
          </component>
          <v-overlay
            v-if="overlayText"
            absolute
            class="ma-0 pa-0"
            color="black"
          >
            <span class="text-h6">{{ overlayText }}</span>
          </v-overlay>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import MatrixDimensionSelection from "@/components/graphs/detail/MatrixDimensionSelection.vue";
import TreeDimensionSelection from "@/components/graphs/detail/TreeDimensionSelection.vue";
import { vxm } from "@/store";
import OverscrollToZoom from "@/components/graphs/helper/OverscrollToZoom";
import {
  AttributedDatapoint,
  DetailDataPoint,
  Dimension,
  DimensionId,
  SeriesInformation,
} from "@/store/types";
import { getInnerHeight } from "@/util/Measurements";
import { escapeHtml } from "@/util/Texts";
import { formatDate } from "@/util/Times";
import GraphDatapointDialog from "@/components/graphs/helper/GraphDatapointDialog.vue";
import { selectGraphVariant } from "@/util/GraphVariantSelection";

export default defineComponent({
  components: {
    GraphDatapointDialog,
    "matrix-dimension-selection": MatrixDimensionSelection,
    "normal-dimension-selection": TreeDimensionSelection,
  },
  data() {
    const graphPlaceholderHeight: number = 100;

    return {
      graphPlaceholderHeight,
      graphRefreshKey: 0,
    };
  },
  computed: {
    selectedDimensions(): Dimension[] {
      return vxm.detailGraphModule.selectedDimensions;
    },
    yStartsAtZero() {
      return vxm.detailGraphModule.beginYScaleAtZero;
    },
    stacked() {
      return vxm.detailGraphModule.stacked;
    },
    normalized() {
      return vxm.detailGraphModule.normalized;
    },
    overscrollToZoom() {
      return new OverscrollToZoom(
        () => vxm.detailGraphModule.fetchDetailGraph(),
        vxm.detailGraphModule,
      );
    },
    overlayText() {
      if (this.selectedDimensions.length === 0) {
        return "Please select a benchmark and metric on the left.";
      }
      // We do not show an overlay if we have no datapoints as you can load more by zooming out
      return null;
    },
    datapoints(): DetailDataPoint[] {
      return vxm.detailGraphModule.detailGraph;
    },
    seriesInformation(): SeriesInformation[] {
      return this.selectedDimensions.map((dimension) => ({
        displayName: dimension.toString(),
        id: dimension.toString(),
        color: this.dimensionColor(dimension),
        unit: dimension.unit,
      }));
    },
    visiblePointCount() {
      return vxm.detailGraphModule.visiblePoints;
    },
    dataRangeMin: {
      get() {
        return vxm.detailGraphModule.startTime;
      },
      set(date: Date) {
        vxm.detailGraphModule.startTime = date;
        vxm.detailGraphModule.fetchDetailGraph();
      },
    },
    dataRangeMax: {
      get() {
        return vxm.detailGraphModule.endTime;
      },
      set(date: Date) {
        vxm.detailGraphModule.endTime = date;
        vxm.detailGraphModule.fetchDetailGraph();
      },
    },
    commitToCompare: {
      get(): AttributedDatapoint | null {
        return vxm.detailGraphModule.commitToCompare;
      },
      set(commit: AttributedDatapoint | null) {
        vxm.detailGraphModule.commitToCompare = commit;
      },
    },
    referenceDatapoint: {
      get(): AttributedDatapoint | null {
        return vxm.detailGraphModule.referenceDatapoint;
      },
      set(datapoint: AttributedDatapoint | null) {
        vxm.detailGraphModule.referenceDatapoint = datapoint;
      },
    },
    zoomXStartValue: {
      get(): number | null {
        return vxm.detailGraphModule.zoomXStartValue;
      },
      set(value: number | null) {
        vxm.detailGraphModule.zoomXStartValue = value;
      },
    },
    zoomXEndValue: {
      get(): number | null {
        return vxm.detailGraphModule.zoomXEndValue;
      },
      set(value: number | null) {
        vxm.detailGraphModule.zoomXEndValue = value;
      },
    },
    zoomYStartValue: {
      get(): number | null {
        return vxm.detailGraphModule.zoomYStartValue;
      },
      set(value: number | null) {
        vxm.detailGraphModule.zoomYStartValue = value;
      },
    },
    zoomYEndValue: {
      get(): number | null {
        return vxm.detailGraphModule.zoomYEndValue;
      },
      set(value: number | null) {
        vxm.detailGraphModule.zoomYEndValue = value;
      },
    },
  },
  mounted() {
    this.retrieveGraphData();
  },
  methods: {
    resetZoom() {
      this.zoomYStartValue = null;
      this.zoomYEndValue = null;
      this.zoomXStartValue = this.dataRangeMin.getTime();
      this.zoomXEndValue = this.dataRangeMax.getTime();
      this.graphRefreshKey++;
    },
    pointFormatter(point: DetailDataPoint) {
      const committerDate = formatDate(point.committerTime);
      return `
            <tr>
              <td>Hash</td>
              <td>${escapeHtml(point.hash)}</td>
            </tr>
            </tr>
              <td>Message</td>
              <td>${escapeHtml(point.summary)}</td>
            </tr>
            <tr>
              <td>Author</td>
              <td>
                ${escapeHtml(point.author)} at ${committerDate}
              </td>
            </tr>
            `;
    },
    dimensionColor(dimension: DimensionId) {
      return vxm.colorModule.colorForDetailDimension(dimension);
    },
    async retrieveGraphData(): Promise<void> {
      if (this.$refs.graphComponent) {
        const element = (this.$refs.graphComponent as Vue).$el as HTMLElement;
        this.graphPlaceholderHeight = getInnerHeight(element);
      }

      await vxm.detailGraphModule.fetchDetailGraph();
      const correctSeries = selectGraphVariant(
        vxm.detailGraphModule.visiblePoints *
          vxm.detailGraphModule.selectedDimensions.length,
      );
      if (correctSeries) {
        this.$emit("selectedGraphComponent", correctSeries.component);
      }
    },
  },
  props: {
    reloadGraphDataCounter: {
      type: Number,
    },
    selectedGraphComponent: {
      type: Object as PropType<typeof Vue>,
    },
  },
  watch: {
    reloadGraphDataCounter: [
      {
        handler: "retrieveGraphData",
      },
    ],
  },
});
</script>
