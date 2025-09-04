<template>
  <div class="home">
    <v-container :fluid="$vuetify.breakpoint.mdAndDown">
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>
              <v-toolbar color="toolbarColor" dark>
                Recent Significant Runs
              </v-toolbar>
            </v-card-title>
            <v-card-text>
              <v-container fluid class="mt-0 pt-0">
                <v-row align="baseline" justify="center">
                  <v-col class="mx-0 px-0">
                    <multiple-run-overview
                      :runs="recentSignificant"
                    ></multiple-run-overview>
                  </v-col>
                </v-row>
                <v-row align="baseline" justify="center" class="mt-2">
                  <v-col cols="auto">
                    <v-btn
                      outlined
                      text
                      color="primary"
                      @click="recentSignificantAmount += 5"
                    >
                      load more
                    </v-btn>
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>
              <v-toolbar color="toolbarColor" dark>Recent Runs</v-toolbar>
            </v-card-title>
            <v-card-text>
              <v-container fluid class="mt-0 pt-0">
                <v-row align="baseline" justify="center">
                  <v-col class="mx-0 px-0">
                    <multiple-run-overview
                      :runs="recent"
                    ></multiple-run-overview>
                  </v-col>
                </v-row>
                <v-row align="baseline" justify="center" class="mt-2">
                  <v-col cols="auto">
                    <v-btn
                      outlined
                      text
                      color="primary"
                      @click="recentAmount += 10"
                    >
                      load more
                    </v-btn>
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import { vxm } from "@/store";
import { RunDescriptionWithDifferences, RunDescription } from "@/store/types";
import MultipleRunOverview from "@/components/overviews/MultipleRunOverview.vue";

export default defineComponent({
  components: {
    "multiple-run-overview": MultipleRunOverview,
  },
  data() {
    const recentSignificantAmount: number = 10;
    const recentAmount: number = 10;

    return {
      recentAmount,
      recentSignificantAmount,
    };
  },
  computed: {
    recent(): RunDescription[] {
      return vxm.newsModule.recentRuns;
    },
    recentSignificant(): RunDescriptionWithDifferences[] {
      return vxm.newsModule.recentSignificantRuns;
    },
  },
  created(): void {
    this.fetchRecent();
    this.fetchRecentSignificant();
  },
  watch: {
    recentAmount: [
      {
        handler: "fetchRecent",
      },
    ],
    recentSignificantAmount: [
      {
        handler: "fetchRecentSignificant",
      },
    ],
  },
  methods: {
    fetchRecent() {
      vxm.newsModule.fetchRecentRuns(this.recentAmount);
    },
    fetchRecentSignificant() {
      vxm.newsModule.fetchRecentSignificantRuns(this.recentSignificantAmount);
    },
  },
});
</script>
