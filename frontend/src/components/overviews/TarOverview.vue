<template>
  <v-card outlined>
    <v-card-title>
      <span v-if="runId === null">Tar '{{ tarSource.description }}'</span>
      <v-row v-if="runId !== null" justify="space-between">
        <v-col> Tar '{{ tarSource.description }}' </v-col>
        <v-col cols="auto">
          <v-tooltip top>
            <template #activator="{ on }">
              <v-btn icon :to="compareRunLocation" v-on="on">
                <v-icon>{{ compareIcon }}</v-icon>
              </v-btn>
            </template>
            Compare this run with another
          </v-tooltip>
        </v-col>
      </v-row>
    </v-card-title>
    <v-card-subtitle v-if="tarSource.repoId">
      Attached to
      <inline-minimal-repo-display
        :repo-id="tarSource.repoId"
      ></inline-minimal-repo-display>
    </v-card-subtitle>
  </v-card>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import { RunId, TarTaskSource } from "@/store/types";
import InlineMinimalRepoNameDisplay from "@/components/misc/InlineMinimalRepoDisplay.vue";
import { mdiScaleBalance } from "@mdi/js";

export default defineComponent({
  components: {
    "inline-minimal-repo-display": InlineMinimalRepoNameDisplay,
  },
  data() {
    return {
      compareIcon: mdiScaleBalance,
    };
  },
  computed: {
    compareRunLocation() {
      if (!this.runId) {
        return null;
      }
      return {
        name: "search",
        params: {
          runId: this.runId,
        },
      };
    },
  },
  props: {
    tarSource: {
      type: Object as PropType<TarTaskSource>,
      required: true,
    },
    runId: {
      type: Object as PropType<RunId | null>,
      default: null,
    },
  },
});
</script>
