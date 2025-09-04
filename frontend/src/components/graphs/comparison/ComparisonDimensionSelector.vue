<script setup lang="ts">
import { Dimension } from "@/store/types";
import { computed, ref, watch } from "vue";

class Item {
  readonly text: string;
  readonly value: string;
  readonly dimension: Dimension;

  constructor(dimension: Dimension) {
    this.text = dimension.toString();
    this.value = dimension.toString();
    this.dimension = dimension;
  }
}

const emit = defineEmits<{
  (e: "input", dimension: Dimension | null): void; // v-model output
}>();

const { possibleDimensions, errorMessage, value } = defineProps<{
  possibleDimensions: Dimension[];
  errorMessage: string | null;
  value: Dimension; // v-model input
}>();

// v-model implementation
const selectedDimension = ref<Dimension | null>(value);
watch(selectedDimension, (newValue) => emit("input", newValue));

const items = computed(() => {
  return possibleDimensions.map((dimension) => new Item(dimension));
});

const selectedItem = computed(() => {
  if (!value) {
    return null;
  }
  if (!possibleDimensions.find((it) => it.equals(selectedDimension.value))) {
    return null;
  }
  return (
    items.value.find((it) => it.dimension.equals(selectedDimension.value)) ||
    null
  );
});

function selectDimension(value: Item) {
  if (!value) {
    selectedDimension.value = null;
  } else {
    selectedDimension.value = value.dimension;
  }
}
</script>

<template>
  <v-card
    :outlined="selectedItem === null"
    :class="{ 'warning-outline': possibleDimensions.length !== 0 }"
    style="height: 100%"
  >
    <v-card-text
      :class="{ disabled: errorMessage !== null }"
      style="height: 100%"
    >
      <v-row class="my-0" align="center" style="height: 100%">
        <v-col
          v-if="errorMessage !== null"
          class="d-flex align-center text-warning py-0"
        >
          {{ errorMessage }}
        </v-col>
        <v-col v-if="errorMessage === null" class="py-0">
          <v-autocomplete
            :value="selectedItem"
            :items="items"
            @input="selectDimension"
            class="py-0"
            label="Dimension to compare…"
            return-object
            hide-details
            no-data-text="No repo(s) selected or no common dimensions"
          >
          </v-autocomplete>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<!--suppress CssUnresolvedCustomProperty -->
<style scoped>
.text-warning {
  color: var(--v-warning-base) !important;
}
</style>

<!--suppress CssUnresolvedCustomProperty -->
<style>
/*noinspection CssUnusedSymbol*/
.warning-outline {
  border-color: var(--v-warning-base) !important;
}
</style>
