<template>
  <v-card>
    <v-card-title>
      <v-toolbar dark :color="toolbarColor">
        <slot name="title">Run Information</slot>
      </v-toolbar>
    </v-card-title>
    <v-card-text class="py-0">
      <v-container fluid class="ma-0 pa-0">
        <slot name="before-body"></slot>
        <v-row align="center" justify="space-around" class="mx-1">
          <v-col
            :lg="item.alwaysAuto ? 'auto' : '2'"
            md="auto"
            sm="auto"
            v-for="item in runInfoItems"
            :key="item.header"
          >
            <v-card outlined>
              <v-card-title class="pb-1">
                <v-icon left dense>{{ item.icon }}</v-icon>
                {{ item.header }}
              </v-card-title>
              <v-card-text>
                <span :class="item.bodyClass">{{ item.body }}</span>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import {
  mdiAlarmCheck,
  mdiCameraTimer,
  mdiClockFast,
  mdiFlash,
  mdiRobot,
} from "@mdi/js";
import { formatDate, formatDurationHuman } from "@/util/Times";
import { Run, RunResultSuccess } from "@/store/types";

export default defineComponent({
  data() {
    return {
      iconTrigger: mdiFlash,
      iconStarted: mdiClockFast,
      iconFinished: mdiAlarmCheck,
      iconDuration: mdiCameraTimer,
      iconRunner: mdiRobot,
    };
  },
  computed: {
    toolbarColor() {
      const successful = this.run.result instanceof RunResultSuccess;
      return successful ? "toolbarColor" : "warning";
    },
    runInfoItems() {
      return [
        {
          header: "Trigger",
          icon: this.iconTrigger,
          body: `${this.run.source.type.toLocaleLowerCase()} by ${
            this.run.author
          }`,
        },
        {
          header: "Started",
          icon: this.iconStarted,
          body: this.formatDate(this.run.startTime),
        },
        {
          header: "Finished",
          icon: this.iconFinished,
          body: this.formatDate(this.run.stopTime),
        },
        {
          header: "Duration",
          icon: this.iconDuration,
          body: this.formatDuration(this.run.startTime, this.run.stopTime),
        },
        {
          header: this.run.runnerName,
          icon: this.iconRunner,
          body: this.run.runnerInfo,
          bodyClass: "worker-description",
          alwaysAuto: true,
        },
      ];
    },
  },
  methods: {
    formatDate(date: Date) {
      return formatDate(date);
    },
    formatDuration(start: Date, end: Date) {
      return formatDurationHuman(start, end);
    },
  },
  props: {
    run: {
      type: Object as PropType<Run>,
    },
  },
});
</script>

<style scoped>
/*noinspection CssUnusedSymbol*/
.worker-description {
  white-space: pre-wrap;
  font-family: monospace;
}
</style>
