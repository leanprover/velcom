<template>
  <v-container align-center justify-center fluid class="mt-0 pt-0">
    <v-row>
      <v-col class="ma-2" cols="12">
        <label
          for="file-upload"
          class="custom-file-upload"
          :class="[errorStyle ? 'error-style' : '']"
        >
          {{ selectedText }}
          <span class="font-weight-bold pl-1">{{ selectedFileName }}</span>
        </label>
        <input
          accept=".tar,.tar.gz"
          @change="fileSelected"
          id="file-upload"
          type="file"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
export default defineComponent({
  computed: {
    selectedText(): string {
      return this.file ? "Selected" : "Click me to select the tar to upload :)";
    },
    selectedFileName() {
      return this.file ? this.file.name : "";
    },
  },
  methods: {
    fileSelected(event: Event) {
      if (!event.target) {
        return;
      }
      const files = (event.target as HTMLInputElement).files;
      if (!files) {
        return;
      }

      this.$emit("input", files[0]);
    },
  },
  props: {
    errorStyle: { default: false, type: Boolean },
    file: { type: File, default: () => null },
  },
  model: {
    prop: "file",
    event: "input",
  },
});
</script>

<!--suppress CssUnresolvedCustomProperty -->
<style scoped>
input[type="file"] {
  display: none;
}

.custom-file-upload {
  border-width: 4px;
  border-style: dashed;
  border-color: var(--v-primary-base);

  display: flex;
  align-items: center;
  justify-content: center;

  cursor: pointer;

  height: 100px;
}
.custom-file-upload.error-style {
  border-color: var(--v-error-base);
}
.custom-file-upload:hover {
  box-shadow: 0 0 6px rgba(35, 173, 278, 1);
}
</style>
