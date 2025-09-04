<template>
  <v-container class="pa-0" fluid>
    <v-row no-gutters align="center">
      <span v-if="tokenState === 'delete'" class="section-header mr-4">
        TOKEN WILL BE DELETED
      </span>
      <span>
        <v-btn
          v-if="tokenState === 'unchanged'"
          @click="tokenState = 'modify'"
          text
          outlined
          class="mr-2"
          color="primary"
        >
          <span v-if="hasToken">Change Github token</span>
          <span v-else>Set Github token</span>
        </v-btn>
        <v-btn
          v-if="tokenState === 'unchanged' && hasToken"
          @click="tokenState = 'delete'"
          text
          outlined
          class="mr-2"
          color="error"
        >
          Delete Github token
        </v-btn>
        <v-btn
          v-if="tokenState === 'modify' || tokenState === 'delete'"
          @click="tokenState = 'unchanged'"
          text
          outlined
          color="error"
        >
          <span v-if="tokenState === 'modify' && hasToken">
            KEEP OLD ACCESS TOKEN
          </span>
          <span v-if="tokenState === 'modify' && !hasToken">
            DON'T SET TOKEN
          </span>
          <span v-else>UNDO</span>
        </v-btn>
      </span>
      <v-text-field
        v-if="tokenState === 'modify'"
        :rules="[notEmpty]"
        label="*New Github access token"
        :value="newToken"
        @input="setNewToken"
        dense
        hide-details="auto"
        class="ml-4 mt-0 pt-0"
      ></v-text-field>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent } from "vue";
export type TokenState = "delete" | "modify" | "unchanged";

export default defineComponent({
  data() {
    const tokenStateImpl: TokenState = "unchanged";

    return {
      tokenStateImpl,
    };
  },
  computed: {
    tokenState: {
      get() {
        return this.tokenStateImpl;
      },
      set(state: TokenState) {
        this.tokenStateImpl = state;

        this.$emit("update:tokenState", state);
      },
    },
  },
  methods: {
    setNewToken(token: string) {
      this.$emit("update:newToken", token);
    },
    notEmpty(input: string): boolean | string {
      return input.trim().length > 0 ? true : "This field must not be empty";
    },
  },
  props: {
    hasToken: {
      type: String,
    },
    newToken: {
      type: String,
    },
  },
});
</script>

<style scoped>
.section-header {
  font-variant: small-caps;
}
</style>
