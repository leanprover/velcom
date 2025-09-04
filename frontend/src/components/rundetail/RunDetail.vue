<template>
  <v-container fluid class="pa-0">
    <v-row no-gutters>
      <v-col v-if="error !== undefined">
        <v-card>
          <v-card-title>
            <v-toolbar dark :color="runColor">{{ errorType }} Error</v-toolbar>
          </v-card-title>
          <v-card-text class="mx-2 error-text overflow-y-auto">
            <span v-html="error"></span>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    <v-row no-gutters v-if="measurements !== undefined">
      <v-col>
        <v-card>
          <v-card-title>
            <v-toolbar dark color="toolbarColor">Run Result</v-toolbar>
          </v-card-title>
          <v-card-text class="mx-2">
            <measurements-display
              :measurements="measurements"
              :differences="differences"
              @dimension-clicked="navigateToDetailGraph"
            ></measurements-display>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    <v-row no-gutters class="mt-3">
      <v-col>
        <run-info :run="run"></run-info>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import {
  Run,
  RunResultScriptError,
  RunResultVelcomError,
  RunResultSuccess,
  Measurement,
  DimensionDifference,
  RunWithDifferences,
  Dimension,
} from "@/store/types";
import MeasurementsDisplay from "@/components/rundetail/MeasurementsDisplay.vue";
import RunInfo from "@/components/rundetail/RunInfo.vue";
import { safeConvertAnsi } from "@/util/Texts";
import { vxm } from "@/store";

export default defineComponent({
  components: {
    "run-info": RunInfo,
    "measurements-display": MeasurementsDisplay,
  },
  computed: {
    run(): Run {
      return this.runWithDifferences.run;
    },
    runColor() {
      if (this.run.result instanceof RunResultScriptError) {
        return "warning";
      } else if (this.run.result instanceof RunResultVelcomError) {
        return "error";
      }
      return "primary";
    },
    error(): string | undefined {
      if (this.errorType === undefined) {
        return undefined;
      }
      return safeConvertAnsi((this.run.result as RunResultVelcomError).error);
    },
    errorType(): string | undefined {
      if (this.run.result instanceof RunResultScriptError) {
        return "Benchmark-Script";
      }
      if (this.run.result instanceof RunResultVelcomError) {
        return "VelCom";
      }
      return undefined;
    },
    measurements(): Measurement[] | undefined {
      if (this.run.result instanceof RunResultSuccess) {
        return this.run.result.measurements;
      }
      return undefined;
    },
    differences(): DimensionDifference[] | undefined {
      if (this.run.result instanceof RunResultSuccess) {
        return this.runWithDifferences.differences;
      }
      return undefined;
    },
    darkThemeSelected() {
      return vxm.userModule.darkThemeSelected;
    },
  },
  methods: {
    navigateToDetailGraph(dimension: Dimension) {
      this.$emit("navigate-to-detail-graph", dimension);
    },
    onDarkThemeSelectionChanged() {
      // The ANSI conversion needs to be redone
      this.$forceUpdate();
    },
  },
  props: {
    runWithDifferences: {
      type: Object as PropType<RunWithDifferences>,
      required: true,
    },
  },
  watch: {
    darkThemeSelected: [
      {
        handler: "onDarkThemeSelectionChanged",
      },
    ],
  },
});
</script>

<style scoped>
.error-text {
  white-space: pre-wrap;
  font-family: monospace;
}
</style>
