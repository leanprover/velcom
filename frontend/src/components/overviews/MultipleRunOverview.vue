<template>
  <div>
    <div v-for="item in runs" :key="run(item).runId" class="my-2">
      <run-overview :run="run(item)" class="full-width">
        <template #content>
          <run-significance-chips
            :differences="significantDifferences(item)"
            :failed-significant-dimensions="failedSignificantDimensions(item)"
            :run-id="run(item).runId"
          ></run-significance-chips>
        </template>
      </run-overview>
    </div>
  </div>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import {
  Dimension,
  DimensionDifference,
  RunDescription,
  RunDescriptionWithDifferences,
} from "@/store/types";
import RunOverview from "./RunOverview.vue";
import RunSignificanceChips from "@/components/runs/RunSignificanceChips.vue";

export default defineComponent({
  components: {
    "run-significance-chips": RunSignificanceChips,
    "run-overview": RunOverview,
  },
  methods: {
    run(run: RunDescription | RunDescriptionWithDifferences): RunDescription {
      return run instanceof RunDescriptionWithDifferences ? run.run : run;
    },
    significantDifferences(
      run: RunDescription | RunDescriptionWithDifferences,
    ): DimensionDifference[] | undefined {
      return run instanceof RunDescriptionWithDifferences
        ? run.significantDifferences
        : [];
    },
    failedSignificantDimensions(
      run: RunDescription | RunDescriptionWithDifferences,
    ): Dimension[] {
      return run instanceof RunDescriptionWithDifferences
        ? run.significantFailedDimensions
        : [];
    },
  },
  props: {
    runs: {
      type: Object as PropType<
        RunDescription[] | RunDescriptionWithDifferences[]
      >,
      required: true,
    },
    numberOfChanges: {
      type: Number,
      default: 3,
    },
  },
});
</script>

<style scoped>
.full-width {
  width: 100%;
}
</style>
