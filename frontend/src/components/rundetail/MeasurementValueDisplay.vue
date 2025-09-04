<template>
  <span>
    <v-tooltip top v-if="displayTooltip">
      <template #activator="{ on }">
        <span v-on="on" class="text--disabled">
          {{ displayedValue }}
        </span>
      </template>
      {{ tooltipMessage }}
    </v-tooltip>
    <span v-else :style="{ color: color }">{{ displayedValue }}</span>
  </span>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
export default defineComponent({
  computed: {
    displayedValue(): string {
      return this.value !== undefined ? this.value : "N/A";
    },
    displayTooltip(): boolean {
      return this.value === undefined;
    },
  },
  props: {
    value: {
      type: String,
    },
    color: {
      type: String,
    },
    tooltipMessage: { default: "This value was not provided", type: String },
  },
});
</script>

<style scoped></style>
