<template>
  <v-dialog width="800" :value="open" @input="$emit('input', $event)">
    <v-card>
      <v-card-title>
        <v-toolbar dark color="toolbarColor">Upload TAR file</v-toolbar>
      </v-card-title>
      <v-card-text>
        <v-form v-model="vueFormValid" @submit="uploadTar">
          <v-text-field
            class="ml-3"
            label="Description"
            v-model="tarDescription"
            :rules="[notEmpty]"
          ></v-text-field>

          <v-row align="center" justify="start" class="mb-0 pb-0">
            <v-col cols="auto">
              <v-btn
                class="ma-0"
                text
                :color="assignToRepo ? 'warning' : 'primary'"
                @click="assignToRepo = !assignToRepo"
              >
                {{ assignToRepo ? "Make stand-alone" : "Assign to repo" }}
              </v-btn>
            </v-col>
            <v-col cols="auto">
              <repo-selection-component
                v-if="assignToRepo"
                v-model="repoId"
                :repos="allRepos"
              ></repo-selection-component>
            </v-col>
          </v-row>

          <file-select-component
            :error-style="tarFile == null"
            v-model="tarFile"
          ></file-select-component>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          :disabled="!formValid"
          color="primary"
          class="mr-5"
          @click="uploadTar"
        >
          Upload tar
        </v-btn>
        <v-btn color="error" text @click="$emit('input', false)">Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import RepoSelectionComponent from "@/components/misc/RepoSelectionComponent.vue";
import { vxm } from "@/store";
import FileSelectComponent from "@/components/misc/FileSelectComponent.vue";

export default defineComponent({
  components: { FileSelectComponent, RepoSelectionComponent },
  data() {
    const assignToRepo: boolean = false;
    const tarFile: File | null = null;
    const repoId: string | null = null;
    const tarDescription: string = "";
    const vueFormValid: boolean = false;

    return {
      vueFormValid,
      tarDescription,
      repoId,
      tarFile,
      assignToRepo,
    };
  },
  computed: {
    allRepos() {
      return vxm.repoModule.allRepos;
    },
    formValid(): boolean {
      return this.vueFormValid && this.tarFile !== null;
    },
  },
  methods: {
    async uploadTar() {
      if (!this.formValid) {
        return;
      }
      await vxm.queueModule.uploadTar({
        repoId: this.repoId,
        description: this.tarDescription,
        file: this.tarFile!,
      });
      await vxm.queueModule.fetchQueue();
      this.$emit("input", false);
    },
    notEmpty(input: string): boolean | string {
      return input.trim().length > 0 ? true : "This field must not be empty!";
    },
    onOpen() {
      if (this.open) {
        this.tarDescription = "";
        this.tarFile = null;
        this.repoId = null;
      }
    },
  },
  model: {
    prop: "open",
    event: "input",
  },
  props: {
    open: {
      type: Boolean,
      required: true,
    },
  },
  watch: {
    open: [
      {
        handler: "onOpen",
      },
    ],
  },
});
</script>
