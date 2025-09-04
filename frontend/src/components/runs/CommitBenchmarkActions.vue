<template>
  <span>
    <v-tooltip top v-for="item in buttonDescriptions" :key="item.tooltip">
      <template #activator="{ on }">
        <v-btn
          v-on="on"
          small
          icon
          @click="item.handler && item.handler($event)"
          :to="item.to"
          :href="item.href"
          :target="item.external ? '_blank' : ''"
          :rel="item.external ? 'noopener nofollow' : ''"
        >
          <v-icon>{{ item.icon }}</v-icon>
        </v-btn>
      </template>
      <span v-html="item.tooltip"></span>
    </v-tooltip>
  </span>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import {
  mdiBitbucket,
  mdiFlash,
  mdiGithub,
  mdiGitlab,
  mdiHistory,
  mdiOneUp,
  mdiOpenInNew,
  mdiScaleBalance,
} from "@mdi/js";
import { vxm } from "@/store";
import { CommitDescription, Repo, RunId } from "@/store/types";
import { RawLocation } from "vue-router";

type ButtonDescription = {
  handler?: (e: MouseEvent) => void;
  icon: string;
  tooltip: string;
  to?: RawLocation;
  href?: string;
  external?: boolean;
  show: boolean;
};

export default defineComponent({
  data() {
    return {
      rebenchmarkIcon: mdiHistory,
      benchmarkIcon: mdiFlash,
      benchmarkUpwardsIcon: mdiOneUp,
      compareIcon: mdiScaleBalance,
    };
  },
  computed: {
    buttonDescriptions(): ButtonDescription[] {
      return [
        {
          handler: this.benchmark,
          icon: this.hasExistingBenchmark
            ? this.rebenchmarkIcon
            : this.benchmarkIcon,
          tooltip: this.hasExistingBenchmark
            ? "Re-run all benchmarks for this commit"
            : "Run all benchmarks for this commit",
          show: this.isAdmin,
        },
        {
          handler: this.benchmarkUpwards,
          icon: this.benchmarkUpwardsIcon,
          tooltip:
            "Benchmark all commits upwards of this commit (this <strong>one</strong> and <strong>up</strong>)",
          show: this.isAdmin,
        },
        {
          to: this.compareRunLocation || undefined,
          icon: this.compareIcon,
          tooltip: "Compare this run with another",
          show: this.runId !== null,
        },
        {
          href: this.commitRemoteLink,
          icon: this.commitRemoteIcon,
          tooltip: `View this commit on <strong>${this.getRemoteHostname}</strong>`,
          external: true,
          show: !!this.getRemoteHostname,
        },
      ].filter((it) => it.show);
    },
    isAdmin(): boolean {
      return vxm.userModule.isAdmin;
    },
    compareRunLocation() {
      if (!this.runId) {
        return null;
      }
      return {
        name: "search",
        params: {
          runId: this.runId,
        },
      };
    },
    repo(): Repo | undefined {
      return vxm.repoModule.repoById(this.commitDescription.repoId);
    },
    commitRemoteLink(): string | undefined {
      if (!this.repo) {
        return undefined;
      }
      const match = /^(https:\/\/(.+)\/|git@(.+):)(.+)\/(.+?)(\.git)?$/.exec(
        this.repo.remoteURL,
      );
      if (!match) {
        return undefined;
      }
      const domain = match[2] || match[3];
      const account = match[4];
      const projectName = match[5];

      const commitMarker = this.repo.remoteURL.includes("bitbucket")
        ? "commits"
        : "commit";

      return `https://${domain}/${account}/${projectName}/${commitMarker}/${this.commitDescription.hash}`;
    },
    getRemoteHostname(): string | undefined {
      if (!this.repo) {
        return undefined;
      }
      const match = /^(https:\/\/(.+)\/|git@(.+):)(.+)\/(.+?)(\.git)?$/.exec(
        this.repo.remoteURL,
      );
      if (!match) {
        return undefined;
      }
      return match[2] || match[3];
    },
    commitRemoteIcon(): string {
      if (!this.repo) {
        return mdiOpenInNew;
      }
      if (this.repo.remoteURL.includes("github.com")) {
        return mdiGithub;
      }
      if (this.repo.remoteURL.includes("gitlab.com")) {
        return mdiGitlab;
      }
      if (this.repo.remoteURL.includes("bitbucket")) {
        return mdiBitbucket;
      }
      return mdiOpenInNew;
    },
  },
  methods: {
    benchmark() {
      vxm.queueModule.startManualTask({
        repoId: this.commitDescription.repoId,
        hash: this.commitDescription.hash,
      });
    },
    async benchmarkUpwards() {
      const taskCount = await vxm.queueModule.dispatchQueueUpwardsOf(
        this.commitDescription,
      );
      this.$globalSnackbar.setSuccess(
        "one-up",
        `One upped ${taskCount} commits!`,
      );
    },
  },
  props: {
    hasExistingBenchmark: { default: true, type: Boolean },
    commitDescription: {
      type: Object as PropType<CommitDescription>,
    },
    runId: { default: null, type: Object as PropType<RunId | null> },
  },
});
</script>
