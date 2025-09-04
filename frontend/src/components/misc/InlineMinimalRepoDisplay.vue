<template>
  <router-link
    class="concealed-link"
    :to="{ name: 'repo-detail', params: { id: repo.id } }"
  >
    <span class="repo-name" :title="'Repo-ID: ' + repo.id">{{
      repo.name
    }}</span>
  </router-link>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import { vxm } from "@/store";
import { Repo } from "@/store/types";

export default defineComponent({
  computed: {
    repo() {
      return (
        vxm.repoModule.repoById(this.repoId) ||
        new Repo("Placeholder", "Not loaded yet :/", [], [], "", undefined)
      );
    },
  },
  props: {
    repoId: {
      type: String,
      required: true,
    },
  },
});
</script>

<style scoped>
.repo-name {
  font-weight: bold;
}
</style>
