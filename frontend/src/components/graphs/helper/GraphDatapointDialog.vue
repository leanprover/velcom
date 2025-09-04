<template>
  <v-dialog width="600" :value="dialogOpen" @input="onClose">
    <v-card class="datapointDialog">
      <v-card-title></v-card-title>
      <v-card-text>
        <v-row v-if="allowSelectAsReference" dense>
          <v-col>
            <v-btn
              width="100%"
              color="primary"
              text
              outlined
              @click="setAsReference"
            >
              Use datapoint as reference
            </v-btn>
          </v-col>
        </v-row>
        <v-row dense>
          <v-col>
            <v-btn
              v-if="hasReferenceLine"
              width="100%"
              color="primary"
              text
              outlined
              @click="removeReferenceLine"
            >
              Remove reference line
            </v-btn>
          </v-col>
        </v-row>
        <v-row v-if="allowSelectCompare" class="mt-4" dense>
          <v-col>
            <v-btn
              width="100%"
              color="primary"
              text
              outlined
              @click="setAsCompare"
            >
              Select this commit to compare
            </v-btn>
          </v-col>
        </v-row>
        <v-row v-if="allowSelectCompare && commitToCompare" dense>
          <v-col>
            <v-btn
              width="100%"
              color="primary"
              text
              outlined
              class="reflow-button py-2"
              @click="compareCommits"
            >
              {{ compareLabel }}
            </v-btn>
          </v-col>
        </v-row>
        <v-row v-if="commitToCompare" dense>
          <v-col class="mb-2">
            <v-btn
              width="100%"
              color="primary"
              text
              outlined
              class="reflow-button py-2"
              @click="removeCompare"
            >
              Reset comparison
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <benchmark-actions
          :has-existing-benchmark="commitHasRun"
          :commit-description="selectedDatapointAsCommitDescription"
        ></benchmark-actions>
        <v-spacer></v-spacer>
        <v-btn color="error" @click="onClose">Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import {
  AttributedDatapoint,
  CommitDescription,
  GraphDataPoint,
  SeriesId,
} from "@/store/types";
import CommitBenchmarkActions from "@/components/runs/CommitBenchmarkActions.vue";

export default defineComponent({
  components: {
    "benchmark-actions": CommitBenchmarkActions,
  },
  computed: {
    commitHasValue() {
      return this.selectedDatapoint.successful(this.seriesId);
    },
    commitHasRun() {
      return !this.selectedDatapoint.unbenchmarked(this.seriesId);
    },
    allowSelectAsReference(): boolean {
      return this.commitHasValue;
    },
    allowSelectCompare(): boolean {
      return !this.noCompare && this.commitHasValue;
    },
    hasReferenceLine() {
      return this.referenceDatapoint !== null;
    },
    selectedDatapointAsCommitDescription() {
      return new CommitDescription(
        this.selectedDatapoint.repoId,
        this.selectedDatapoint.hash,
        this.selectedDatapoint.author,
        this.selectedDatapoint.committerTime,
        this.selectedDatapoint.summary,
      );
    },
    compareLabel(): string {
      return this.commitToCompare
        ? "Compare this commit to commit " + this.commitToCompare.datapoint.hash
        : "";
    },
  },
  methods: {
    setAsReference() {
      this.$emit("update:referenceDatapoint", {
        datapoint: this.selectedDatapoint,
        seriesId: this.seriesId,
      } as AttributedDatapoint);
      this.$emit("close");
    },
    removeReferenceLine() {
      this.$emit("update:referenceDatapoint", null);
      this.$emit("close");
    },
    setAsCompare() {
      this.$emit("update:commitToCompare", {
        datapoint: this.selectedDatapoint,
        seriesId: this.seriesId,
      } as AttributedDatapoint);
      this.$emit("close");
    },
    removeCompare() {
      this.$emit("update:commitToCompare", null);
      this.$emit("close");
    },
    compareCommits() {
      this.pointDialogExecuteCompare();
      this.$emit("close");
    },
    pointDialogExecuteCompare() {
      if (!this.commitToCompare || !this.selectedDatapoint) {
        return;
      }
      const repoId = this.commitToCompare.datapoint.repoId;
      const hashFrom = this.commitToCompare.datapoint.hash;
      const hashTo = this.selectedDatapoint.hash;

      this.$router.push({
        name: "run-comparison",
        params: { first: repoId, second: repoId },
        query: { hash1: hashFrom, hash2: hashTo },
      });
    },
    onClose() {
      this.$emit("close");
    },
  },
  props: {
    dialogOpen: { default: false, type: Boolean },
    selectedDatapoint: {
      type: Object as PropType<GraphDataPoint>,
    },
    seriesId: {
      type: Object as PropType<SeriesId>,
    },
    commitToCompare: {
      default: null,
      type: Object as PropType<AttributedDatapoint | null>,
    },
    referenceDatapoint: {
      default: null,
      type: Object as PropType<AttributedDatapoint | null>,
    },
    noCompare: { default: false, type: Boolean },
  },
});
</script>

<style scoped>
.reflow-button {
  word-wrap: normal;
  white-space: normal;
  height: 100% !important;
}
</style>

<style>
.reflow-button > span {
  width: 100%;
}
</style>
