<template>
  <component
    :is="run.source.type"
    :commit="commit"
    :source="run.source"
    :id="run.runId"
    :linkLocation="runLinkLocation"
  >
    <template #avatar>
      <v-list-item-avatar
        :class="!$vuetify.breakpoint.smAndUp ? ['ml-0', 'mt-0'] : []"
      >
        <v-tooltip top v-if="isSuccessful">
          <template #activator="{ on }">
            <v-icon v-on="on" size="32px" color="success">
              {{ successIcon }}
            </v-icon>
          </template>
          This run was successful :)
        </v-tooltip>
        <v-tooltip top v-else>
          <template #activator="{ on }">
            <v-icon
              :color="isCompleteFailure ? 'error' : 'orange'"
              v-on="on"
              size="32px"
              >{{ isCompleteFailure ? errorIcon : partialErrorIcon }}</v-icon
            >
          </template>
          <span v-if="isCompleteFailure">This run failed completely :(</span>
          <span v-else>This run suffered at least one failure :/</span>
        </v-tooltip>
      </v-list-item-avatar>
    </template>
    <template #actions v-if="commit">
      <commit-benchmark-actions
        :hasExistingBenchmark="true"
        :commitDescription="commit"
        :run-id="run.runId"
      ></commit-benchmark-actions>
      <slot name="actions"></slot>
    </template>
    <template #content>
      <slot name="content"></slot>
    </template>
  </component>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import {
  CommitDescription,
  CommitTaskSource,
  RunDescription,
} from "@/store/types";
import {
  mdiCheckboxMarkedCircleOutline,
  mdiCloseCircleOutline,
  mdiAlertCircleCheckOutline,
} from "@mdi/js";
import CommitBenchmarkActions from "../runs/CommitBenchmarkActions.vue";
import CommitOverviewBase from "./CommitOverviewBase.vue";
import TarTaskOverview from "./TarTaskOverview.vue";
import { RawLocation } from "vue-router";

export default defineComponent({
  components: {
    "commit-benchmark-actions": CommitBenchmarkActions,
    COMMIT: CommitOverviewBase,
    UPLOADED_TAR: TarTaskOverview,
  },
  data() {
    return {
      successIcon: mdiCheckboxMarkedCircleOutline,
      partialErrorIcon: mdiAlertCircleCheckOutline,
      errorIcon: mdiCloseCircleOutline,
    };
  },
  computed: {
    isSuccessful(): boolean {
      return this.run.success === "SUCCESS";
    },
    isCompleteFailure(): boolean {
      return this.run.success === "FAILURE";
    },
    runLinkLocation(): RawLocation {
      return {
        name: "run-detail",
        params: { first: this.run.runId },
      };
    },
    commit(): CommitDescription | undefined {
      return this.run.source instanceof CommitTaskSource
        ? this.run.source.commitDescription
        : undefined;
    },
  },
  props: {
    run: {
      type: Object as PropType<RunDescription>,
      required: true,
    },
  },
});
</script>
