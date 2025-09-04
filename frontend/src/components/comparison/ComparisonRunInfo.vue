<template>
  <run-info :run="run">
    <template #title>
      <span>{{ title }}</span>
      <v-spacer></v-spacer>
      <v-chip
        :to="{
          name: 'run-detail',
          params: { first: run.id },
        }"
        outlined
        label
      >
        Run id: {{ run.id }}
      </v-chip>
    </template>
    <template #before-body>
      <v-row class="mx-1" v-if="commit">
        <v-col>
          <commit-overview-base
            :commit="commit"
            :outlined="true"
          ></commit-overview-base>
        </v-col>
      </v-row>
      <v-row class="mx-1" v-if="tar" justify="center">
        <v-col cols="auto">
          <tar-overview :tar-source="tar"></tar-overview>
        </v-col>
      </v-row>
    </template>
  </run-info>
</template>
<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import CommitOverviewBase from "@/components/overviews/CommitOverviewBase.vue";
import RunInfo from "@/components/rundetail/RunInfo.vue";
import TarOverview from "@/components/overviews/TarOverview.vue";
import { CommitTaskSource, Run, TarTaskSource } from "@/store/types";
export default defineComponent({
  components: {
    "commit-overview-base": CommitOverviewBase,
    "tar-overview": TarOverview,
    "run-info": RunInfo,
  },
  computed: {
    commit() {
      if (this.run.source instanceof CommitTaskSource) {
        return this.run.source.commitDescription;
      }
      return null;
    },
    tar() {
      if (this.run.source instanceof TarTaskSource) {
        return this.run.source;
      }
      return null;
    },
  },
  props: {
    run: {
      type: Object as PropType<Run>,
    },
    title: {
      type: String,
    },
  },
});
</script>
