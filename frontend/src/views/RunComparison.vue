<template>
  <v-container>
    <v-row v-if="(!run1Successful || !run2Successful) && comparison">
      <v-col>
        <v-card>
          <v-card-title>
            <v-toolbar dark color="toolbarColor">Run Comparison</v-toolbar>
          </v-card-title>
          <v-card-text class="mx-2">
            <div class="text-body-1 mb-3">
              Comparison not possible
              <span v-if="run2Successful">as the first run failed.</span>
              <span v-else-if="run1Successful">as the second run failed.</span>
              <span v-else>as both runs failed.</span>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    <v-row v-if="comparison && run1Successful && run2Successful">
      <v-col>
        <v-card>
          <v-card-title>
            <v-toolbar dark color="toolbarColor">Run Comparison</v-toolbar>
          </v-card-title>
          <v-card-text style="max-width: 1185px; margin: auto">
            <run-significance-chips
              class="mb-6"
              :center="true"
              :no-links="true"
              :differences="comparison.significantDifferences"
              :run-id="comparison.run2.id"
              :failed-significant-dimensions="
                comparison.significantFailedDimensions
              "
            ></run-significance-chips>
            <comparison-table
              :first="comparison.run1"
              :second="comparison.run2"
              :differences="comparison.differences"
            ></comparison-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    <v-row v-if="comparison && comparison.run1">
      <v-col>
        <comparison-run-info
          :run="comparison.run1"
          :title="'First Run Information' + (run1Successful ? '' : ' (Failed)')"
        />
      </v-col>
    </v-row>
    <v-row v-if="comparison && comparison.run2" class="mt-3 mb-3">
      <v-col>
        <comparison-run-info
          :run="comparison.run2"
          :title="
            'Second Run Information' + (run2Successful ? '' : ' (Failed)')
          "
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import { vxm } from "@/store";
import { RunComparison, RunResultSuccess } from "@/store/types";
import RunComparisonTable from "@/components/comparison/RunComparisonTable.vue";
import CommitOverviewBase from "@/components/overviews/CommitOverviewBase.vue";
import RunInfo from "@/components/rundetail/RunInfo.vue";
import TarOverview from "@/components/overviews/TarOverview.vue";
import ComparisonRunInfo from "@/components/comparison/ComparisonRunInfo.vue";
import RunSignificanceChips from "@/components/runs/RunSignificanceChips.vue";

export default defineComponent({
  components: {
    RunSignificanceChips,
    ComparisonRunInfo,
    "tar-overview": TarOverview,
    "commit-overview-base": CommitOverviewBase,
    "run-info": RunInfo,
    "comparison-table": RunComparisonTable,
  },
  data() {
    const comparison: RunComparison | null = null;

    return {
      comparison,
    };
  },
  computed: {
    first(): string {
      return this.$route.params["first"];
    },
    second(): string {
      return this.$route.params["second"];
    },
    hash1(): string | undefined {
      return this.$route.query["hash1"] as string | undefined;
    },
    hash2(): string | undefined {
      return this.$route.query["hash2"] as string | undefined;
    },
    run1Successful() {
      return (
        this.comparison &&
        this.comparison.run1.result instanceof RunResultSuccess
      );
    },
    run2Successful() {
      return (
        this.comparison &&
        this.comparison.run2.result instanceof RunResultSuccess
      );
    },
  },
  mounted(): void {
    this.fetchData();
  },
  watch: {
    first: [
      {
        handler: "fetchData",
      },
    ],
    second: [
      {
        handler: "fetchData",
      },
    ],
    hash1: [
      {
        handler: "fetchData",
      },
    ],
    hash2: [
      {
        handler: "fetchData",
      },
    ],
  },
  methods: {
    async fetchData() {
      this.comparison = null;

      // We don't need reactivity on this object, we only swap it wholesale.
      this.comparison = Object.freeze(
        await vxm.commitDetailComparisonModule.fetchComparison({
          first: this.first,
          second: this.second,
          hash1: this.hash1,
          hash2: this.hash2,
        }),
      );
    },
  },
});
</script>
