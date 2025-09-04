<script setup lang="ts">
import { CleanupDimension } from "@/store/types";
import Vue, { computed, onMounted, ref } from "vue";
import { vxm } from "@/store";

const headers = [
  { text: "Benchmark", value: "dimension.benchmark", align: "left" },
  { text: "Metric", value: "dimension.metric", align: "left" },
  { text: "Total run count", value: "runs", align: "left" },
  { text: "Untracked run count", value: "untrackedRuns", align: "left" },
  { text: "Unreachable run count", value: "unreachableRuns", align: "left" },
];

const selectedDimensions = ref<CleanupDimension[]>([]);
const showDeleteDialog = ref<boolean>(false);
const confirmText = ref<string>("");

const allDimensions = computed<CleanupDimension[] | null>(() => {
  if (vxm.cleanupModule.dimensions === null) {
    return null;
  }
  return vxm.cleanupModule.dimensions
    .slice()
    .sort((a, b) =>
      a.dimension.toString().localeCompare(b.dimension.toString()),
    );
});

const deletedMeasurementsCount = computed(() => {
  return selectedDimensions.value
    .map((it) => it.runs)
    .reduce((a, b) => a + b, 0);
});

const expectedConfirmationText = computed(() => {
  return (
    "delete " +
    selectedDimensions.value.length +
    " dimension" +
    (selectedDimensions.value.length !== 1 ? "s" : "") +
    " and " +
    deletedMeasurementsCount.value +
    " measurement" +
    (deletedMeasurementsCount.value !== 1 ? "s" : "")
  );
});

const confirmedDeletion = computed(() => {
  return confirmText.value === expectedConfirmationText.value;
});

async function deleteSelectedDimensions() {
  showDeleteDialog.value = false;
  await vxm.cleanupModule.deleteDimensions(
    selectedDimensions.value.map((it) => it.dimension),
  );
  selectedDimensions.value = [];
  Vue.prototype.$globalSnackbar.setSuccess(
    "cleanup",
    "Dimensions and measurements deleted",
    2,
  );
  await vxm.cleanupModule.fetchDimensions();
}

onMounted(async () => {
  await vxm.cleanupModule.fetchDimensions();
});
</script>

<template>
  <v-container>
    <v-row>
      <v-col>
        <v-card>
          <v-card-title>
            <v-toolbar color="toolbarColor" dark>Clean up Dimensions</v-toolbar>
          </v-card-title>
          <v-card-actions class="d-flex justify-start ml-2">
            <v-dialog v-model="showDeleteDialog" width="700">
              <template #activator="{ on }">
                <v-btn
                  v-on="on"
                  color="warning"
                  text
                  outlined
                  @click="
                    showDeleteDialog = true;
                    confirmText = '';
                  "
                  :disabled="selectedDimensions.length === 0"
                >
                  Delete selected dimensions
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <v-toolbar color="warning" dark>Confirm deletion</v-toolbar>
                </v-card-title>
                <v-card-text>
                  <div class="mx-2">
                    <div
                      class="subtitle-1 font-weight-bold d-inline-block mb-2"
                    >
                      You are about to irrevocably delete the following
                      dimensions and their measurements:
                    </div>
                    <ul>
                      <li
                        v-for="{ dimension } in selectedDimensions"
                        :key="dimension.toString()"
                      >
                        {{ dimension.toString() }}
                      </li>
                    </ul>
                    <br />
                    In total,
                    <span class="font-weight-bold subtitle-1">
                      {{ deletedMeasurementsCount }} measurements
                    </span>
                    will be deleted.
                    <br />
                    Please confirm this by typing
                    <span
                      style="font-family: monospace"
                      class="font-weight-bold"
                      >{{ expectedConfirmationText }}</span
                    >
                    in the text field below.

                    <v-text-field
                      :placeholder="expectedConfirmationText"
                      v-model="confirmText"
                      hide-details
                    ></v-text-field>
                  </div>
                </v-card-text>
                <v-card-actions class="d-flex justify-center">
                  <v-btn
                    color="warning"
                    :disabled="!confirmedDeletion"
                    @click="deleteSelectedDimensions"
                  >
                    Confirm deletion
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-card-actions>
          <v-card-text>
            <v-container fluid class="mt-0 pt-0">
              <v-row justify="center">
                <v-col>
                  <v-data-table
                    v-model="selectedDimensions"
                    :headers="headers"
                    :items="allDimensions || []"
                    :items-per-page="-1"
                    :loading="allDimensions === null"
                    checkbox-color="primary"
                    item-key="key"
                    show-select
                    multi-sort
                    dense
                  >
                  </v-data-table>
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped></style>
