<template>
  <v-card outlined>
    <v-list-item>
      <v-icon large>{{ branchIcon }}</v-icon>
      <v-list-item-content>
        <v-container fluid class="my-0 py-0">
          <v-row no-gutters align="center" justify="space-between">
            <v-col cols="auto" class="flex-shrink-too mr-3">
              <v-list-item-title>
                <repo-display :repoId="item.repoId"></repo-display>
                <span class="mx-2">—</span>
                <router-link class="concealed-link" :to="commitLinkLocation">
                  <span class="commit-message">{{ item.name }}</span>
                </router-link>
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ item.commitSummary }}
              </v-list-item-subtitle>
            </v-col>
            <v-col cols="auto">
              <v-container fluid class="ma-0 pa-0">
                <v-row no-gutters align="center" justify="space-between">
                  <v-col cols="auto">
                    <text-chip :text="item.hash"></text-chip>
                  </v-col>
                  <span class="pl-3">
                    <slot name="compare-actions" :has-run="item.hasRun"></slot>
                  </span>
                </v-row>
              </v-container>
            </v-col>
          </v-row>
        </v-container>
      </v-list-item-content>
    </v-list-item>
  </v-card>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import { SearchItemBranch } from "@/store/types";
import { RawLocation } from "vue-router";
import TextChip from "@/components/misc/TextChip.vue";
import InlineMinimalRepoDisplay from "@/components/misc/InlineMinimalRepoDisplay.vue";
import { mdiSourceBranch } from "@mdi/js";

export default defineComponent({
  components: {
    "repo-display": InlineMinimalRepoDisplay,
    TextChip,
  },
  data() {
    return {
      branchIcon: mdiSourceBranch,
    };
  },
  computed: {
    commitLinkLocation(): RawLocation {
      return {
        name: "run-detail",
        params: { first: this.item.repoId, second: this.item.hash },
      };
    },
  },
  props: {
    item: {
      type: Object as PropType<SearchItemBranch>,
    },
  },
});
</script>

<style scoped>
.commit-message {
  font-style: italic;
}
.flex-shrink-too {
  flex: 1 1 0;
  min-width: 200px;
}
</style>
