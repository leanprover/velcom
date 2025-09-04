<script setup lang="ts">
import { Repo, RepoId } from "@/store/types";
import { computed } from "vue";
import { vxm } from "@/store";

const emit = defineEmits<{
  (
    e: "update:set-all",
    payload: {
      repoId: RepoId;
      branches: string[];
    },
  ): void;
  (
    e: "update:toggle-branch",
    payload: {
      repoId: RepoId;
      branch: string;
    },
  ): void;
}>();

const { selectedBranches } = defineProps<{
  selectedBranches: Map<RepoId, string[]>;
}>();

function selectedBranchesForRepo(repoId: RepoId) {
  return selectedBranches.get(repoId) || [];
}

const sortedRepos = computed(() => {
  return vxm.repoModule.allRepos.slice().sort((a, b) => {
    if (a.trackedBranches.length === 0 && b.trackedBranches.length === 0) {
      return a.name.localeCompare(b.name);
    }
    if (a.trackedBranches.length === 0) {
      return 1;
    }
    if (b.trackedBranches.length === 0) {
      return -1;
    }
    return a.name.localeCompare(b.name);
  });
});

const hasConflictingNames = computed(() => {
  const distinct = new Set(sortedRepos.value.map((it) => it.name.trim()));
  return distinct.size !== sortedRepos.value.length;
});

function colorForRepo(repoId: RepoId) {
  return vxm.colorModule.colorForRepo(repoId);
}

function onlySomeBranchesSelected(repo: Repo) {
  const selectedLength = selectedBranchesForRepo(repo.id).length;
  return selectedLength < repo.trackedBranches.length && selectedLength > 0;
}

function allBranchesSelected(repo: Repo) {
  if (repo.trackedBranches.length === 0) {
    return false;
  }
  const selectedLength = selectedBranchesForRepo(repo.id).length;
  return selectedLength === repo.trackedBranches.length;
}

function toggleRepo(repo: Repo) {
  if (allBranchesSelected(repo)) {
    emit("update:set-all", {
      repoId: repo.id,
      branches: [],
    });
  } else {
    emit("update:set-all", {
      repoId: repo.id,
      branches: repo.trackedBranches,
    });
  }
}

function toggleBranch(repo: Repo, branch: string) {
  emit("update:toggle-branch", {
    repoId: repo.id,
    branch,
  });
}
</script>

<template>
  <v-expansion-panels multiple accordion>
    <v-expansion-panel
      v-for="repo in sortedRepos"
      :key="repo.id"
      :disabled="repo.trackedBranches.length === 0"
    >
      <v-expansion-panel-header>
        <v-checkbox
          :input-value="allBranchesSelected(repo)"
          :indeterminate="onlySomeBranchesSelected(repo)"
          :style="{ color: colorForRepo(repo.id) }"
          :color="colorForRepo(repo.id)"
          dense
          hide-details
          class="ma-0 pa-0 flex-grow-0 colored-checkbox"
          @change="toggleRepo(repo)"
          @click.native.stop="() => {}"
        ></v-checkbox>
        <span v-if="!hasConflictingNames">{{ repo.name }}</span>

        <v-container fluid class="ma-0 pa-0" v-if="hasConflictingNames">
          <v-row no-gutters class="mt-0">
            <v-col>
              <span>{{ repo.name }}</span>
            </v-col>
          </v-row>
          <v-row no-gutters class="mt-0">
            <v-col>
              <span class="caption text--disabled">{{ repo.id }}</span>
            </v-col>
          </v-row>
        </v-container>

        <span v-if="repo.trackedBranches.length === 0" class="text-end ml-3">
          No tracked branches
        </span>
      </v-expansion-panel-header>
      <v-expansion-panel-content>
        <v-list dense class="pt-0">
          <v-list-item v-for="branch in repo.trackedBranches" :key="branch">
            <v-list-item-content class="ma-0 pa-0">
              <v-checkbox
                :input-value="selectedBranchesForRepo(repo.id)"
                :value="branch"
                :label="branch"
                :color="colorForRepo(repo.id)"
                :style="{ color: colorForRepo(repo.id) }"
                class="ma-0 pa-0 colored-checkbox"
                hide-details
                dense
                @change="toggleBranch(repo, branch)"
              ></v-checkbox>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-expansion-panel-content>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<style>
.colored-checkbox .v-icon {
  color: inherit !important;
}
</style>
