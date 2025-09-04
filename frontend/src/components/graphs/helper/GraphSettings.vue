<template>
  <v-card>
    <v-card-text>
      <v-row align="center" justify="space-between">
        <v-col cols="auto">
          <v-btn-toggle v-model="selectedGraphVariant" mandatory dense>
            <v-btn
              v-for="{ component, name } in availableGraphComponents"
              :key="name"
              :value="component"
            >
              {{ name }}
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col cols="auto" style="gap: 12px" class="d-flex flex-wrap">
          <v-btn
            @click="$emit('update:normalized', !normalized)"
            color="primary"
            outlined
            text
          >
            Normalize
            <v-icon class="ml-2" style="margin-right: -6px">
              {{ normalized ? iconOn : iconOff }}
            </v-icon>
          </v-btn>
          <!--
          <v-btn
            @click="$emit('update:stacked', !stacked)"
            color="primary"
            outlined
            text
          >
            Stack Charts
            <v-icon class="ml-2" style="margin-right: -6px">
              {{ stacked ? iconOn : iconOff }}
            </v-icon>
          </v-btn>
          -->
          <v-btn
            @click="$emit('update:beginYAtZero', !beginYAtZero)"
            color="primary"
            outlined
            text
          >
            Start Y-Axis at zero
            <v-icon class="ml-2" style="margin-right: -6px">
              {{ beginYAtZero ? iconOn : iconOff }}
            </v-icon>
          </v-btn>
          <v-btn
            color="primary"
            outlined
            text
            @click="
              $emit(
                'update:dayEquidistantGraphSelected',
                !dayEquidistantGraphSelected,
              )
            "
          >
            Use Day-Equidistant Graph
            <v-icon class="ml-2" style="margin-right: -6px">
              {{ dayEquidistantGraphSelected ? iconOn : iconOff }}
            </v-icon>
          </v-btn>
          <slot></slot>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import { availableGraphComponents } from "@/util/GraphVariantSelection";
import { mdiCheckboxBlankOutline, mdiCheckboxMarked } from "@mdi/js";

export default defineComponent({
  data() {
    return {
      iconOff: mdiCheckboxBlankOutline,
      iconOn: mdiCheckboxMarked,
    };
  },
  computed: {
    selectedGraphVariant: {
      get() {
        return this.graphComponent;
      },
      set(variant: typeof Vue) {
        this.$emit("update:graphComponent", variant);
      },
    },
    availableGraphComponents() {
      return availableGraphComponents;
    },
  },
  props: {
    graphComponent: {
      type: Object as PropType<typeof Vue>,
      required: true,
    },
    beginYAtZero: {
      type: Boolean,
      default: true,
    },
    stacked: {
      type: Boolean,
      default: false,
    },
    normalized: {
      type: Boolean,
      default: false,
    },
    dayEquidistantGraphSelected: {
      type: Boolean,
      default: true,
    },
  },
});
</script>
