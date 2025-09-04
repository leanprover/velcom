<template>
  <v-card outlined>
    <v-list-item>
      <v-icon large>{{ icon }}</v-icon>
      <v-list-item-content>
        <v-container fluid class="my-0 py-0">
          <v-row no-gutters align="center" justify="space-between">
            <v-col cols="auto" class="flex-shrink-too mr-3">
              <v-list-item-title>
                <repo-display
                  v-if="item.repoId"
                  :repoId="item.repoId"
                ></repo-display>
                <span v-if="item.repoId" class="mx-2">—</span>
                <router-link class="concealed-link" :to="linkLocation">{{
                  description
                }}</router-link>
              </v-list-item-title>
              <v-list-item-subtitle>
                Started at {{ startTimeString }} and ran for
                {{ durationString }}
              </v-list-item-subtitle>
            </v-col>
            <v-col cols="auto">
              <v-container fluid class="ma-0 pa-0">
                <v-row no-gutters align="center" justify="space-between">
                  <v-col cols="auto">
                    <text-chip :text="item.id"></text-chip>
                  </v-col>
                  <span class="pl-3">
                    <slot name="compare-actions" :has-run="true"></slot>
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
import { RawLocation } from "vue-router";
import TextChip from "@/components/misc/TextChip.vue";
import InlineMinimalRepoDisplay from "@/components/misc/InlineMinimalRepoDisplay.vue";
import { mdiFolderZipOutline, mdiRunFast } from "@mdi/js";
import { SearchItemRun } from "@/store/types";
import { formatDate, formatDurationHuman } from "@/util/Times";

export default defineComponent({
  components: {
    "repo-display": InlineMinimalRepoDisplay,
    TextChip,
  },
  data() {
    return {
      runIcon: mdiRunFast,
      tarIcon: mdiFolderZipOutline,
    };
  },
  computed: {
    linkLocation(): RawLocation {
      return {
        name: "run-detail",
        params: { first: this.item.id },
      };
    },
    startTimeString() {
      return formatDate(this.item.startTime);
    },
    durationString() {
      return formatDurationHuman(this.item.startTime, this.item.stopTime);
    },
    description() {
      return this.item.tarDescription || this.item.commitSummary;
    },
    icon() {
      return this.item.tarDescription ? this.tarIcon : this.runIcon;
    },
  },
  props: {
    item: {
      type: Object as PropType<SearchItemRun>,
    },
  },
});
</script>

<style scoped>
.flex-shrink-too {
  flex: 1 1 0;
  min-width: 200px;
}
</style>
