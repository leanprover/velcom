<template>
  <div>
    <v-dialog width="600" v-model="dialogOpen">
      <template #activator="{ on }">
        <slot :on="on" name="activator"></slot>
      </template>

      <v-card>
        <v-toolbar dark color="toolbarColor">
          <v-toolbar-title>Login</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-form v-model="formValid" ref="form" @submit="login">
            <v-text-field
              type="password"
              :rules="[nonEmptyToken]"
              label="Access token"
              v-model="token"
              @keydown.enter.prevent="login"
              autofocus
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            :disabled="!formValid || loading"
            :loading="loading"
            @click="login"
          >
            Login
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
import RepoSelectionComponent from "./RepoSelectionComponent.vue";

export default defineComponent({
  components: {
    "repo-selection": RepoSelectionComponent,
  },
  data() {
    const dialogOpen: boolean = false;
    const formValid: boolean = false;
    const token: string = "";
    const repoId: string = "";
    const loading: boolean = false;

    return {
      loading,
      repoId,
      token,
      formValid,
      dialogOpen,
    };
  },
  methods: {
    nonEmptyToken(input: string): boolean | string {
      return input.length > 0 ? true : "This field must not be empty!";
    },
    login() {
      if (!this.formValid) {
        return;
      }

      this.loading = true;

      vxm.userModule
        .logIn(this.token)
        .then(() => (this.dialogOpen = false))
        .finally(() => (this.loading = false));
    },
    onOpened(opened: boolean) {
      if (!opened) {
        this.token = "";
        this.repoId = "";
      }
    },
  },
  watch: {
    dialogOpen: [
      {
        handler: "onOpened",
      },
    ],
  },
});
</script>
