<template>
  <v-container>
    <v-row v-if="isAdmin">
      <v-col>
        <CleanupDimensions></CleanupDimensions>
      </v-col>
    </v-row>
    <v-row v-if="!isAdmin" justify="center" class="mt-4">
      <v-col cols="auto">
        <pre class="font-weight-bold">
           .----------.      ____________
          /  .-.  .-.  \    /            \
         /   | |  | |   \   |  Aaaaaaah! |
         \   `-'  `-'  _/   / ___________/
         /\     .--.  / |  /_/
         \ |   /  /  / /
         / |   `--' /\ \
        / / `-------' \ \      Jym Dyer
        </pre>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import CleanupDimensions from "@/components/cleanup/CleanupDimensions.vue";
import { vxm } from "@/store";

export default defineComponent({
  components: { CleanupDimensions },
  computed: {
    isAdmin() {
      return vxm.userModule.isAdmin;
    },
  },
});
</script>
