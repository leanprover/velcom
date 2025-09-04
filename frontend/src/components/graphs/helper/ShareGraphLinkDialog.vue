<template>
  <v-dialog width="800px" v-model="dialogOpen">
    <template #activator="{ on }">
      <v-btn v-on="on" outlined text color="primary">
        Share graph
        <v-icon right>{{ permanentLinkIcon }}</v-icon>
      </v-btn>
    </template>
    <v-card>
      <v-card-title>
        <v-toolbar color="toolbarColor" dark>
          Share a permanent link to this graph
        </v-toolbar>
      </v-card-title>
      <v-card-text>
        <v-container fluid>
          <v-row no-gutters v-if="shareOptions.length > 0">
            <v-col
              v-for="{
                label,
                selectable,
                unselectableMessage,
                key,
              } in shareOptions"
              :key="key"
            >
              <v-tooltip top :disabled="selectable">
                <template #activator="{ on }">
                  <span v-on="on">
                    <v-checkbox
                      v-on="on"
                      hide-details
                      v-model="options[key]"
                      :disabled="!selectable"
                      :indeterminate="!selectable"
                      :label="label"
                    ></v-checkbox>
                  </span>
                </template>
                {{ unselectableMessage }}
              </v-tooltip>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-text-field
                id="url-field"
                readonly
                autofocus
                :value="permanentLinkUrl"
              >
                <template #append>
                  <div class="pb-2 pl-2">
                    <v-btn
                      outlined
                      text
                      color="primary"
                      @click="copyPermanentLink"
                    >
                      Copy
                    </v-btn>
                  </div>
                </template>
              </v-text-field>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import { mdiLinkVariantPlus } from "@mdi/js";
import { copyToClipboard } from "@/util/Clipboards";
import { PermanentLinkOptions } from "@/store/modules/detailGraphStore";
export type Option = {
  label: string;
  selectable: boolean;
  unselectableMessage: string;
  key: keyof PermanentLinkOptions;
};

export default defineComponent({
  data() {
    const options: PermanentLinkOptions = {
      includeYZoom: true,
      includeXZoom: true,
      includeDataRestrictions: true,
    };
    const dialogOpen: boolean = false;

    return {
      dialogOpen,
      options,
      permanentLinkIcon: mdiLinkVariantPlus,
    };
  },
  computed: {
    permanentLinkUrl() {
      return this.linkGenerator(this.options);
    },
  },
  methods: {
    copyPermanentLink() {
      copyToClipboard(this.permanentLinkUrl, this.$globalSnackbar);
    },
    onDialogVisibilityChange() {
      if (this.dialogOpen) {
        this.options = {
          includeDataRestrictions: true,
          includeXZoom: true,
          includeYZoom: true,
        };
      }
    },
  },
  props: {
    linkGenerator: {
      type: Function as PropType<(options: PermanentLinkOptions) => string>,
      required: true,
    },
    shareOptions: {
      type: Array as PropType<Option[]>,
      required: true,
    },
  },
  watch: {
    dialogOpen: [
      {
        handler: "onDialogVisibilityChange",
      },
    ],
  },
});
</script>
