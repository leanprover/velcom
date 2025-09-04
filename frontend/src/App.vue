<template>
  <v-app>
    <v-main>
      <nav-bar @refresh-view="routerViewKey++"></nav-bar>
      <snackbar ref="global-snackbar"></snackbar>
      <router-view :key="routerViewKey" />
      <theme-selector @use-dark-theme="setDarkTheme"></theme-selector>
    </v-main>
  </v-app>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import NavigationBar from "./components/misc/NavigationBar.vue";
import Snackbar from "./components/misc/Snackbar.vue";
import { vxm } from "./store";
import ThemeSelector from "./components/misc/ThemeSelector.vue";
import { storeToLocalStorage } from "./store/persistence";
import { Route } from "vue-router";
import "@/css/AnsiTheme.css";

export default defineComponent({
  components: {
    "nav-bar": NavigationBar,
    snackbar: Snackbar,
    "theme-selector": ThemeSelector,
  },
  data() {
    const routerViewKey: number = 0;
    const clickHandler: any = this.checkClick;

    return {
      clickHandler,
      routerViewKey,
    };
  },
  computed: {
    isDarkTheme() {
      return vxm.userModule.darkThemeSelected;
    },
  },
  created(): void {
    this.$vuetify.theme.dark = this.isDarkTheme;
    document.addEventListener("click", this.clickHandler);
    document.addEventListener("mousedown", this.clickHandler);

    vxm.repoModule.fetchRepos();
    this.$router.afterEach((to: Route, from: Route) => {
      if (to.name !== from.name) {
        vxm.repoModule.fetchRepos();
      }
    });
  },
  mounted(): void {
    if (vxm.userModule.usesBrowsersThemePreferences && this.isDarkTheme) {
      this.$globalSnackbar.setSuccess(
        "theme",
        "Selected dark mode based on your browser preferences.",
      );
    }
  },
  beforeDestroy(): void {
    document.removeEventListener("click", this.clickHandler);
    document.removeEventListener("mousedown", this.clickHandler);
  },
  methods: {
    checkClick(event: Event) {
      if (!event.target) {
        return;
      }
      if (!(event.target instanceof HTMLElement)) {
        return;
      }

      let tmpElement: HTMLElement | null = event.target;
      while (tmpElement && tmpElement.tagName.toLowerCase() !== "a") {
        tmpElement = tmpElement.parentElement;
      }

      if (!tmpElement) {
        return;
      }
      storeToLocalStorage();
    },
    setDarkTheme(darkTheme: boolean) {
      vxm.userModule.darkThemeSelected = darkTheme;
    },
    onDarkThemeChanged() {
      this.$vuetify.theme.dark = this.isDarkTheme;
    },
  },
  watch: {
    isDarkTheme: [
      {
        handler: "onDarkThemeChanged",
      },
    ],
  },
});
</script>

<style>
.v-toolbar > .v-toolbar__content {
  height: auto !important;
  min-height: 64px;
}
.v-toolbar {
  height: auto !important;
  min-height: 64px;
}

.theme--light.v-application a.concealed-link {
  color: inherit !important;
}

.v-application .theme--dark a.concealed-link {
  color: inherit !important;
}

.concealed-link {
  text-decoration: none;
  cursor: pointer;
}

.concealed-link:hover {
  text-decoration: underline;
}
</style>
