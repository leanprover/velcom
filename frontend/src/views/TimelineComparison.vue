<template>
  <v-container fluid class="px-0 pt-1" style="height: 100%">
    <v-row style="height: 100%">
      <v-col cols="3" style="height: 100%">
        <v-row>
          <v-col :style="{ 'min-height': 68 + 12 * 2 + 'px' }">
            <comparison-dimension-selector
              :possible-dimensions="possibleDimensions"
              :error-message="dimensionSelectorErrorMessage"
              v-model="selectedDimension"
            ></comparison-dimension-selector>
          </v-col>
        </v-row>
        <v-row class="mt-0" style="height: 100%">
          <v-col style="height: 100%">
            <repo-branch-selector
              :selected-branches="selectedBranches"
              @update:toggle-branch="toggleRepoBranch"
              @update:set-all="setRepoBranches"
            ></repo-branch-selector>
          </v-col>
        </v-row>
      </v-col>
      <v-col cols="9" ref="graphColumn" style="height: 100%">
        <v-row>
          <v-col>
            <comparison-graph-settings
              :begin-y-at-zero.sync="beginYAtZero"
              :graph-component.sync="graphComponent"
              :day-equidistant-graph-selected.sync="dayEquidistantGraphSelected"
            >
              <share-graph-link-dialog
                :link-generator="getShareLink"
                :share-options="shareOptions"
              />
            </comparison-graph-settings>
          </v-col>
        </v-row>
        <v-row class="mt-0">
          <v-col>
            <comparison-graph
              :comparison-datapoints="comparisonDatapoints"
              :begin-y-at-zero="beginYAtZero"
              :graph-component="graphComponent"
              :height="graphHeight + 'px'"
            ></comparison-graph>
          </v-col>
        </v-row>
        <v-row class="pt-0">
          <v-col class="pt-0">
            <graph-timespan-controls
              :end-time.sync="endTime"
              :start-time.sync="startTime"
            ></graph-timespan-controls>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import RepoBranchSelector from "@/components/graphs/comparison/RepoBranchSelector.vue";
import { vxm } from "@/store";
import GraphTimespanControls from "@/components/graphs/helper/GraphTimespanControls.vue";
import ComparisonDimensionSelector from "@/components/graphs/comparison/ComparisonDimensionSelector.vue";
import { ComparisonDataPoint, Dimension, Repo } from "@/store/types";
import GraphSettings from "@/components/graphs/helper/GraphSettings.vue";
import { groupBy, spaceDayEquidistant } from "@/util/DayEquidistantUtils";
import { availableGraphComponents } from "@/util/GraphVariantSelection";
import { debounce } from "@/util/Debouncer";
import ShareGraphLinkDialog from "@/components/graphs/helper/ShareGraphLinkDialog.vue";
import { PermanentLinkOptions } from "@/store/modules/detailGraphStore";
import ComparisonGraph from "@/components/graphs/comparison/ComparisonGraph.vue";

export default defineComponent({
  components: {
    ShareGraphLinkDialog,
    ComparisonGraphSettings: GraphSettings,
    ComparisonGraph,
    ComparisonDimensionSelector,
    GraphTimespanControls,
    RepoBranchSelector,
  },
  data() {
    const debouncedUpdate: () => void = debounce(() => this.refetchData(), 100);
    const graphHeight: number = window.innerHeight;
    const graphComponent: typeof Vue | null =
      availableGraphComponents[0].component;
    const comparisonDatapoints: ComparisonDataPoint[] = [];

    return {
      comparisonDatapoints,
      graphComponent,
      graphHeight,
      debouncedUpdate,
    };
  },
  computed: {
    startTime: {
      get(): Date {
        return vxm.comparisonGraphModule.startTime;
      },
      set(date: Date) {
        vxm.comparisonGraphModule.startTime = date;
      },
    },
    endTime: {
      get(): Date {
        return vxm.comparisonGraphModule.endTime;
      },
      set(date: Date) {
        vxm.comparisonGraphModule.endTime = date;
      },
    },
    selectedDimension: {
      get() {
        return vxm.comparisonGraphModule.selectedDimension;
      },
      set(dimension: Dimension | null) {
        vxm.comparisonGraphModule.selectedDimension = dimension;
      },
    },
    dayEquidistantGraphSelected: {
      get() {
        return vxm.comparisonGraphModule.dayEquidistantGraphSelected;
      },
      set(selected: boolean) {
        vxm.comparisonGraphModule.dayEquidistantGraphSelected = selected;
      },
    },
    beginYAtZero: {
      get() {
        return vxm.comparisonGraphModule.beginYAtZero;
      },
      set(beginYAtZero: boolean) {
        vxm.comparisonGraphModule.beginYAtZero = beginYAtZero;
      },
    },
    selectedBranches() {
      return vxm.comparisonGraphModule.selectedBranches;
    },
    possibleDimensions() {
      const participatingRepos: Repo[] = this.selectedRepos;

      if (participatingRepos.length === 0) {
        return [];
      }

      let possibleDimensions: Dimension[] = participatingRepos
        .pop()!
        .dimensions.slice();

      participatingRepos.forEach((repo) => {
        const dimensionsInRepo = new Set(
          repo.dimensions.map((it) => it.toString()),
        );
        possibleDimensions = possibleDimensions.filter((it) =>
          dimensionsInRepo.has(it.toString()),
        );
      });

      possibleDimensions.sort((a, b) =>
        a.toString().localeCompare(b.toString()),
      );

      return possibleDimensions;
    },
    dimensionSelectorErrorMessage() {
      if (this.possibleDimensions.length > 0) {
        return null;
      }
      if (vxm.comparisonGraphModule.selectedBranches.size === 0) {
        return "No repo selected";
      }
      return "No mutual dimensions";
    },
    selectedRepos(): Repo[] {
      return (
        Array.from(vxm.comparisonGraphModule.selectedBranches.entries())
          .filter(([, value]) => value.length > 0)
          .map(([key]) => vxm.repoModule.repoById(key))
          // Repos might not exist anymore, as the selected branches are persisted
          .filter((it) => it !== undefined)
          .map((it) => it!)
      );
    },
    shareOptions() {
      return [
        {
          label: "Use X-axis zoom instead of start/end date",
          selectable: true,
          unselectableMessage:
            "That you see this is a bug. Please report it :)",
          key: "includeXZoom",
        },
        {
          label: "Include Y-axis zoom",
          selectable:
            vxm.comparisonGraphModule.zoomYStartValue !== null ||
            vxm.comparisonGraphModule.zoomYEndValue !== null,
          unselectableMessage: "You haven't zoomed the Y axis",
          key: "includeYZoom",
        },
        {
          label: "Include repos and branches",
          selectable: vxm.comparisonGraphModule.selectedBranches.size > 0,
          unselectableMessage: "You haven't selected any branches or repos",
          key: "includeDataRestrictions",
        },
      ];
    },
    shouldNotShowResults() {
      if (this.possibleDimensions.length === 0) {
        return true;
      }
      const possibleDimension = this.possibleDimensions.find((dim) =>
        dim.equals(this.selectedDimension),
      );
      return possibleDimension === undefined;
    },
  },
  async mounted() {
    this.onResized();
    window.addEventListener("resize", this.onResized);

    await this.debouncedUpdate();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.onResized);
  },
  methods: {
    toggleRepoBranch(payload: { repoId: string; branch: string }) {
      vxm.comparisonGraphModule.toggleRepoBranch(payload);
    },
    setRepoBranches(payload: { repoId: string; branches: string[] }) {
      vxm.comparisonGraphModule.setSelectedBranchesForRepo(payload);
    },
    getShareLink(options: PermanentLinkOptions) {
      return vxm.comparisonGraphModule.permanentLink(options);
    },
    applyDatapointTransformations(datapoints: ComparisonDataPoint[]) {
      if (!this.dayEquidistantGraphSelected) {
        return datapoints.map((it) => it.positionedAt(it.committerTime));
      }

      const byRepo = groupBy(datapoints, (it) => it.repoId);
      return Array.from(byRepo.entries())
        .map(([, points]) => spaceDayEquidistant(points))
        .reduce((a, b) => a.concat(b));
    },
    async refetchData() {
      if (this.shouldNotShowResults) {
        this.comparisonDatapoints = [];
        return;
      }
      this.comparisonDatapoints = this.applyDatapointTransformations(
        await vxm.comparisonGraphModule.fetchComparisonGraph(),
      );
    },
    adjustGraphHeight() {
      const graphColumn = this.$refs["graphColumn"] as HTMLElement;
      const app = document.querySelector("#app") as HTMLElement;

      const hasScrollBar = app.scrollHeight > window.innerHeight;
      const causedByGraph = graphColumn.scrollHeight >= window.innerHeight;
      if (hasScrollBar && causedByGraph) {
        this.graphHeight -= app.scrollHeight - window.innerHeight;
      }
    },
    onResized() {
      // Set the graph height to always cause a scroll bar. This simplifies the further calculations as we can assume we
      // need to shrink the graph, but never grow it.
      this.graphHeight = window.innerHeight;

      // Wait for the browser to reflow the page after the above update, then shrink the graph
      window.requestAnimationFrame(() => {
        this.adjustGraphHeight();
      });
    },
    async onSelectedRepoChange() {
      if (this.selectedRepos.length > 0) {
        await this.debouncedUpdate();
      } else {
        this.comparisonDatapoints = [];
      }
    },
    onDayEquidistantChanged() {
      this.comparisonDatapoints = this.applyDatapointTransformations(
        this.comparisonDatapoints,
      );
    },
    async onGraphInformationChanged() {
      await this.debouncedUpdate();
    },
  },
  watch: {
    selectedRepos: [
      {
        handler: "onSelectedRepoChange",
      },
    ],
    dayEquidistantGraphSelected: [
      {
        handler: "onDayEquidistantChanged",
      },
    ],
    selectedDimension: [
      {
        handler: "onGraphInformationChanged",
      },
    ],
    startTime: [
      {
        handler: "onGraphInformationChanged",
      },
    ],
    endTime: [
      {
        handler: "onGraphInformationChanged",
      },
    ],
  },
});
</script>
