<script setup lang="ts">
import GraphDatapointDialog from "@/components/graphs/helper/GraphDatapointDialog.vue";
import {
  ComparisonDataPoint,
  SeriesId,
  SeriesInformation,
} from "@/store/types";
import Vue, { computed, ref } from "vue";
import OverscrollToZoom from "@/components/graphs/helper/OverscrollToZoom";
import { vxm } from "@/store";
import { roundDateDown, roundDateUp } from "@/util/Times";

const {
  comparisonDatapoints,
  graphComponent,
  beginYAtZero,
  height = "80vh",
} = defineProps<{
  comparisonDatapoints: ComparisonDataPoint[];
  graphComponent: typeof Vue;
  beginYAtZero: boolean;
  height: string;
}>();

const graphRefreshKey = ref(0);

const overscrollToZoom = computed(() => {
  return new OverscrollToZoom(() => {
    // Not needed as the RepoComparison will handle refresh
    // FIXME: Also move repo detail refreshes to parent component and remove this parameter
    return Promise.resolve();
  }, vxm.comparisonGraphModule);
});

const seriesInformation = computed<SeriesInformation[]>(() => {
  return Array.from(vxm.comparisonGraphModule.selectedBranches.keys()).map(
    (repoId) => ({
      id: repoId as SeriesId,
      color: vxm.colorModule.colorForRepo(repoId),
      displayName: vxm.repoModule.repoById(repoId)!.name,
    }),
  );
});

const dataRangeMin = computed({
  get() {
    return vxm.comparisonGraphModule.startTime;
  },
  set(newValue) {
    vxm.comparisonGraphModule.startTime = roundDateDown(newValue);
  },
});

const dataRangeMax = computed({
  get() {
    return vxm.comparisonGraphModule.endTime;
  },
  set(newValue) {
    vxm.comparisonGraphModule.endTime = roundDateUp(newValue);
  },
});

const referenceDatapoint = computed({
  get() {
    return vxm.comparisonGraphModule.referenceDatapoint;
  },
  set(newValue) {
    vxm.comparisonGraphModule.referenceDatapoint = newValue;
  },
});

const visiblePointCount = computed(() => {
  const startValue = zoomXStartValue.value;
  const endValue = zoomXEndValue.value;

  // TODO: Is this a performance problem? There might be 10.000+ items here
  // and this method is called every time the slider is dragged or the user
  // zooms using the mouse wheel
  let visibleDataPoints = 0;
  for (const point of comparisonDatapoints) {
    if (
      (startValue === null || point.positionTime.getTime() >= startValue) &&
      (endValue === null || point.positionTime.getTime() <= endValue)
    ) {
      visibleDataPoints++;
    }
  }
  return visibleDataPoints;
});

const overlayText = computed(() => {
  if (seriesInformation.value.length === 0) {
    return "Please select a repo, branch and dimension on the left";
  }
  if (!vxm.comparisonGraphModule.selectedDimension) {
    return "Please select a dimension in the top left";
  }
  return null;
});

const zoomXStartValue = computed({
  get() {
    return vxm.comparisonGraphModule.zoomXStartValue;
  },
  set(newValue) {
    vxm.comparisonGraphModule.zoomXStartValue = newValue;
  },
});

const zoomXEndValue = computed({
  get() {
    return vxm.comparisonGraphModule.zoomXEndValue;
  },
  set(newValue) {
    vxm.comparisonGraphModule.zoomXEndValue = newValue;
  },
});

const zoomYStartValue = computed({
  get() {
    return vxm.comparisonGraphModule.zoomYStartValue;
  },
  set(newValue) {
    vxm.comparisonGraphModule.zoomYStartValue = newValue;
  },
});

const zoomYEndValue = computed({
  get() {
    return vxm.comparisonGraphModule.zoomYEndValue;
  },
  set(newValue) {
    vxm.comparisonGraphModule.zoomYEndValue = newValue;
  },
});

function resetZoom() {
  zoomYStartValue.value = null;
  zoomYEndValue.value = null;
  zoomXStartValue.value = dataRangeMin.value.getTime();
  zoomXEndValue.value = dataRangeMax.value.getTime();
  graphRefreshKey.value++;
}
</script>

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
          <GraphDatapointDialog
            :dialog-open="dialogOpen"
            :selected-datapoint="selectedDatapoint"
            :series-id="seriesInformation.id"
            :reference-datapoint.sync="referenceDatapoint"
            :no-compare="true"
            @close="closeDialog()"
          ></GraphDatapointDialog>
        </template>
      </component>
      <v-overlay v-if="overlayText" absolute class="ma-0 pa-0" color="black">
        <span class="text-h6">{{ overlayText }}</span>
      </v-overlay>
    </v-card-text>
  </v-card>
</template>
