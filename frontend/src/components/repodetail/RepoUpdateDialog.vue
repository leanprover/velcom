<template>
  <div>
    <v-dialog width="600" v-model="dialogOpen">
      <template #activator="{ on }">
        <slot :on="on" name="activator"></slot>
      </template>

      <v-card>
        <v-toolbar dark color="toolbarColor">
          <v-toolbar-title>Update Repository</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-form v-model="formValid" ref="form">
            <div class="section-header mt-3 mb-2">GENERAL SETTINGS</div>

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
            <github-token-configuration
              :has-token="hasGithubToken"
              :new-token.sync="newGithubToken"
              @update:tokenState="githubTokenState = $event"
            ></github-token-configuration>
            <div class="section-header mb-2 mt-4">BRANCHES</div>
            <v-data-iterator
              :custom-filter="filterName"
              :search="searchValue"
              :items="branchObjects"
              :items-per-page="itemsPerPage"
              :footer-props="{ itemsPerPageOptions: itemsPerPageOptions }"
              sort-by="lowerCased"
              :hide-default-footer="branchObjects.length < itemsPerPage"
            >
              <template v-slot:header>
                <v-text-field
                  :prepend-icon="searchIcon"
                  label="Search for a branch..."
                  v-model="searchValue"
                ></v-text-field>
                <v-checkbox
                  dense
                  class="my-0 pt-0 font-italic"
                  label="Track all branches"
                  :input-value="
                    newTrackedBranches.length === repo.branches.length
                  "
                  @change="toggleAll"
                ></v-checkbox>
              </template>
              <template v-slot:default="props">
                <v-row align="center">
                  <v-col
                    cols="4"
                    class="my-1 py-0"
                    style="word-wrap: anywhere"
                    v-for="branch in props.items"
                    :key="branch.name"
                  >
                    <v-checkbox
                      class="my-0 py-0 full-height-label-checkbox"
                      v-model="newTrackedBranches"
                      dense
                      :label="branch.name"
                      :value="branch.name"
                    ></v-checkbox>
                  </v-col>
                </v-row>
              </template>
            </v-data-iterator>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            class="mr-3"
            :disabled="!formValid"
            @click="updateRepo"
            >Update Repository</v-btn
          >
          <v-btn color="error" text outlined @click="dialogOpen = false">
            Close
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import { Repo } from "@/store/types";
import { vxm } from "@/store";
import { mdiMagnify } from "@mdi/js";
import GithubTokenConfiguration, {
  TokenState,
} from "@/components/repodetail/GithubTokenConfiguration.vue";
export default defineComponent({
  components: { GithubTokenConfiguration },
  data() {
    const githubTokenState: TokenState = "unchanged";
    const newGithubToken: string = "";
    const newTrackedBranches: string[] = [];
    const searchValue: string = "";
    const formValid: boolean = false;
    const itemsPerPageOptions: number[] = [1, 10, 20, 30, 50, 100, -1];
    const itemsPerPage: number = 30;
    const repoName: string = "";
    const remoteUrl: string = "";
    const dialogOpen: boolean = false;

    return {
      dialogOpen,
      remoteUrl,
      repoName,
      itemsPerPage,
      itemsPerPageOptions,
      formValid,
      searchValue,
      newTrackedBranches,
      newGithubToken,
      githubTokenState,
      searchIcon: mdiMagnify,
    };
  },
  computed: {
    branchObjects(): { name: string; lowerCased: string }[] {
      return this.repo.branches.map((it) => ({
        name: it.name,
        lowerCased: it.name.toLowerCase(),
      }));
    },
    repo(): Repo {
      return vxm.repoModule.repoById(this.repoId)!;
    },
    hasGithubToken() {
      return this.repo.lastGithubUpdate !== undefined;
    },
  },
  mounted(): void {
    Vue.nextTick(() => this.watchIdUpdates());
  },
  methods: {
    filterName(items: { lowerCased: string }[]) {
      return items.filter(
        (input) =>
          input.lowerCased.indexOf(this.searchValue.toLowerCase()) >= 0,
      );
    },
    notEmpty(input: string): boolean | string {
      return input.trim().length > 0 ? true : "This field must not be empty!";
    },
    toggleAll() {
      if (
        this.newTrackedBranches.length === this.repo.branches.length &&
        this.newTrackedBranches.length > 0
      ) {
        this.newTrackedBranches = [];
      } else {
        this.newTrackedBranches = this.repo.branches.map(
          (branch) => branch.name,
        );
      }
    },
    async updateRepo() {
      let newToken: string | undefined;
      if (this.githubTokenState === "delete") {
        newToken = "";
      } else if (this.githubTokenState === "modify") {
        newToken = this.newGithubToken;
      }

      await vxm.repoModule.updateRepo({
        id: this.repoId,
        name: this.repoName,
        remoteUrl: this.remoteUrl,
        trackedBranches: this.newTrackedBranches,
        githubToken: newToken,
      });

      this.dialogOpen = false;
    },
    watchIdUpdates() {
      this.remoteUrl = this.repo.remoteURL;
      this.repoName = this.repo.name;
      this.searchValue = "";

      this.newTrackedBranches = this.repo.branches
        .filter((it) => it.tracked)
        .map((it) => it.name);
    },
  },
  props: {
    repoId: {
      type: String,
    },
  },
  watch: {
    dialogOpen: [
      {
        handler: "watchIdUpdates",
      },
    ],
    repoId: [
      {
        handler: "watchIdUpdates",
      },
    ],
  },
});
</script>

<style scoped>
.section-header {
  font-variant: small-caps;
}
</style>

<style>
/* Not scoped, as the v-label does not get any data attribute */
/*noinspection CssUnusedSymbol*/
.full-height-label-checkbox .v-label {
  height: 100% !important;
}
</style>
