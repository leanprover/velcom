<template>
  <v-chip
    outlined
    label
    color="accent"
    class="commit-hash-chip"
    @click="copyOnClick ? copyToClipboard(text) : undefined"
    :to="copyOnClick ? undefined : to"
    v-on="on"
  >
    {{ text }}
  </v-chip>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import { copyToClipboard } from "@/util/Clipboards";

export default defineComponent({
  methods: {
    copyToClipboard(hash: string) {
      const selection = window.getSelection();
      if (selection && selection.toString()) {
        // Do not overwrite user text selection
        return;
      }
      copyToClipboard(hash, this.$globalSnackbar);
    },
  },
  props: {
    text: {
      type: String,
    },
    copyOnClick: { default: true, type: Boolean },
    to: {
      type: Object as PropType<any>,
    },
    on: {
      type: Object as PropType<any>,
    },
  },
});
</script>

<style scoped>
.commit-hash-chip {
  font-family: monospace;
  font-size: 0.8em;

  user-select: text !important;
}
</style>
