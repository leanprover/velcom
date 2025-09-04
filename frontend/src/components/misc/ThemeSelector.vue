<template>
  <footer>
    <v-toolbar bottom dense class="hidden-md-and-up">
      <v-spacer></v-spacer>
      <v-btn text @click="toggleDarkTheme">
        <v-icon left>{{ darkThemeIcon }}</v-icon>
        Use {{ isDarkTheme ? "light" : "dark" }} theme
      </v-btn>
    </v-toolbar>
    <v-tooltip left>
      <template #activator="{ on }">
        <v-btn
          v-on="on"
          icon
          @click="toggleDarkTheme"
          class="hidden-sm-and-down floater"
        >
          <v-icon :class="{ icon: true, 'light-icon': !isDarkTheme }">
            {{ darkThemeIcon }}
          </v-icon>
        </v-btn>
      </template>
      Use {{ isDarkTheme ? "light" : "dark" }} theme
    </v-tooltip>
  </footer>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
import { mdiInvertColors } from "@mdi/js";
import { vxm } from "@/store";

export default defineComponent({
  data() {
    return {
      darkThemeIcon: mdiInvertColors,
    };
  },
  computed: {
    isDarkTheme(): boolean {
      return vxm.userModule.darkThemeSelected;
    },
  },
  methods: {
    toggleDarkTheme() {
      this.$emit("use-dark-theme", !this.isDarkTheme);
    },
  },
});
</script>

<style scoped>
.floater {
  position: fixed;
  bottom: 16px;
  right: 16px;
  z-index: 100;
}

.icon {
  transition: transform 0.5s linear;
}

.light-icon {
  transform: matrix(-1, 0, 0, 1, 0, 0);
}
</style>
