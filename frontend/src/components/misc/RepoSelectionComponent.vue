<template>
  <v-autocomplete
    :disabled="disabled"
    :items="sortedRepos"
    :rules="rules"
    :value="repoId"
    :label="label"
    :hide-details="rules.length === 0"
    :clearable="clearable"
    @input="setRepoId"
    item-text="name"
    item-value="id"
    class="mt-0 pt-0"
  >
    <template v-slot:item="data">
      <v-list-item-avatar>
        <v-icon small>{{ repoIcon }}</v-icon>
      </v-list-item-avatar>
      <v-list-item-content>
        <v-list-item-title>{{ data.item.name }}</v-list-item-title>
        <v-list-item-subtitle>{{ data.item.id }}</v-list-item-subtitle>
      </v-list-item-content>
    </template>
  </v-autocomplete>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import { Repo } from "@/store/types";
import { mdiSourceBranch } from "@mdi/js";

export default defineComponent({
  data() {
    return {
      repoIcon: mdiSourceBranch,
    };
  },
  computed: {
    sortedRepos() {
      return this.repos.slice().sort((a, b) => a.name.localeCompare(b.name));
    },
  },
  methods: {
    setRepoId(newValue: string) {
      this.$emit("value", newValue);
    },
  },
  props: {
    rules: {
      type: Object as PropType<[(input: string) => string | boolean]>,
      default: () => [],
    },
    repos: {
      type: Object as PropType<[Repo]>,
      default: () => [],
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    label: {
      type: String,
      default: "Repository name",
    },
    clearable: {
      type: Boolean,
      default: false,
    },
    repoId: {
      type: String,
      required: true,
    },
  },
  model: {
    prop: "repoId",
    event: "value",
  },
});
</script>

<style scoped></style>
