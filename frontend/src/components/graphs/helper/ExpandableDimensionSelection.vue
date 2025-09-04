<template>
  <div>
    <v-dialog
      fullscreen
      scrollable
      v-model="dimensionSelectionInFullscreen"
      style="margin-top: 64px !important"
      transition="dialog-transition"
    >
      <graph-dimension-selector
        @reload-graph-data="$emit('reload-graph-data')"
        @shrink="dimensionSelectionInFullscreen = false"
        @update:selectedDimensions="updateSelectedDimensions"
        @update:selectorType="updateSelectorType"
        :is-fullscreen="true"
        :all-dimensions="allDimensions"
        :selected-dimensions="selectedDimensions"
        :selector-type="selectorType"
      >
      </graph-dimension-selector>
    </v-dialog>
    <graph-dimension-selector
      @expand="dimensionSelectionInFullscreen = true"
      @reload-graph-data="$emit('reload-graph-data')"
      @update:selectedDimensions="updateSelectedDimensions"
      @update:selectorType="updateSelectorType"
      :is-fullscreen="false"
      :all-dimensions="allDimensions"
      :selected-dimensions="selectedDimensions"
      :selector-type="selectorType"
    ></graph-dimension-selector>
  </div>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import GraphDimensionSelector from "@/components/repodetail/GraphDimensionSelector.vue";
import { Dimension } from "@/store/types";

export default defineComponent({
  components: {
    GraphDimensionSelector,
  },
  data() {
    return {
      dimensionSelectionInFullscreen: false,
    };
  },
  methods: {
    updateSelectedDimensions(newDimensions: Dimension[]) {
      this.$emit("update:selectedDimensions", newDimensions);
    },
    updateSelectorType(type: "tree" | "matrix") {
      this.$emit("update:selectorType", type);
    },
  },
  props: {
    allDimensions: {
      type: Array as PropType<Dimension[]>,
      required: true,
    },
    selectedDimensions: {
      type: Array as PropType<Dimension[]>,
      required: true,
    },
    selectorType: {
      type: Object as PropType<"tree" | "matrix">,
      default: "matrix",
    },
  },
});
</script>
