<template>
  <div>
    <v-dialog width="600" v-model="dialogOpen">
      <template #activator="{ on }">
        <slot :on="on" name="activator"></slot>
      </template>

      <v-card>
        <v-toolbar dark color="toolbarColor">
          <v-toolbar-title>Add Repository</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-form v-model="formValid" ref="form">
            <v-text-field
              :rules="[notEmpty]"
              label="*Remote URL"
              v-model="remoteUrl"
            ></v-text-field>
            <v-text-field
              :rules="[notEmpty]"
              label="*Repository name"
              v-model="repoName"
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            :loading="addInProgress"
            :disabled="!formValid || addInProgress"
            @click="addRepository"
          >
            Add Repository
          </v-btn>
          <v-spacer></v-spacer>
          <v-btn color="error" @click="dialogOpen = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import { vxm } from "@/store";

export default defineComponent({
  data() {
    const addInProgress: boolean = false;
    const dialogOpen: boolean = false;
    const formValid: boolean = false;
    const repoName: string = "";
    const remoteUrl: string = "";

    return {
      remoteUrl,
      repoName,
      formValid,
      dialogOpen,
      addInProgress,
    };
  },
  methods: {
    notEmpty(input: string): boolean | string {
      return input.trim().length > 0 ? true : "This field must not be empty!";
    },
    addRepository() {
      this.addInProgress = true;
      vxm.repoModule
        .addRepo({
          repoName: this.repoName,
          remoteUrl: this.remoteUrl,
        })
        .then((it) => {
          this.$emit("value", it);
          this.dialogOpen = false;
        })
        .finally(() => (this.addInProgress = false));
    },
    clearDialogOnOpen(opened: boolean): void {
      if (opened) {
        this.remoteUrl = "";
        this.repoName = "";
      }
    },
  },
  watch: {
    dialogOpen: [
      {
        handler: "clearDialogOnOpen",
      },
    ],
  },
});
</script>

<style scoped></style>
