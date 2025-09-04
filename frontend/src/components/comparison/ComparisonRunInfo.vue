<script setup lang="ts">
import RunInfo from "@/components/rundetail/RunInfo.vue";
import CommitOverviewBase from "@/components/overviews/CommitOverviewBase.vue";
import TarOverview from "@/components/overviews/TarOverview.vue";
import { CommitTaskSource, Run, TarTaskSource } from "@/store/types";
import { computed } from "vue";

const { run, title } = defineProps<{
  run: Run;
  title: string;
}>();

const commit = computed(() => {
  if (run.source instanceof CommitTaskSource) {
    return run.source.commitDescription;
  }
  return null;
});

const tar = computed(() => {
  if (run.source instanceof TarTaskSource) {
    return run.source;
  }
  return null;
});
</script>

<template>
  <RunInfo :run="run">
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
          <CommitOverviewBase
            :commit="commit"
            :outlined="true"
          ></CommitOverviewBase>
        </v-col>
      </v-row>
      <v-row class="mx-1" v-if="tar" justify="center">
        <v-col cols="auto">
          <TarOverview :tar-source="tar"></TarOverview>
        </v-col>
      </v-row>
    </template>
  </RunInfo>
</template>
