<template>
  <v-container fluid class="ma-0 pa-0">
    <v-row align="baseline" justify="center" no-gutters>
      <v-col>
        <v-card>
          <v-card-text class="ma-0 pa-0">
            <v-container fluid class="ma-0 px-5">
              <v-row align="center" justify="space-between" no-gutters>
                <v-col v-for="field in timeFields" :key="field.ref">
                  <v-menu
                    :ref="field.ref"
                    v-model="field.opened"
                    :return-value.sync="field.value"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    offset-y
                    min-width="290px"
                  >
                    <template #activator="{ on }">
                      <v-text-field
                        class="mr-5 mb-5"
                        hide-details="auto"
                        :value="field.value"
                        :disabled="dateLocked === field.role"
                        :label="field.role + ':'"
                        :prepend-icon="dateIcon"
                        readonly
                        v-on="on"
                        :rules="field.rules"
                      >
                        <v-icon
                          slot="append"
                          @click="lockDates(field.role)"
                          class="lock-button"
                        >
                          {{ dateLocked === field.role ? lock : openLock }}
                        </v-icon>
                      </v-text-field>
                    </template>
                    <v-date-picker
                      hide-details="auto"
                      v-model="field.value"
                      no-title
                      scrollable
                      :allowed-dates="field.allowedDates"
                      :events="[startTimeString, endTimeString]"
                      :event-color="
                        (date: string) =>
                          date === startTimeString ? 'blue' : 'red'
                      "
                    >
                      <v-btn
                        text
                        color="primary"
                        @click="field.saveFunction(field, today)"
                      >
                        Today
                      </v-btn>
                      <v-spacer></v-spacer>
                      <v-btn text color="primary" @click="field.opened = false">
                        Cancel
                      </v-btn>
                      <v-btn
                        text
                        color="primary"
                        @click="field.saveFunction(field, field.value)"
                      >
                        OK
                      </v-btn>
                    </v-date-picker>
                  </v-menu>
                </v-col>

                <v-col>
                  <v-form @submit.prevent="saveDuration">
                    <v-text-field
                      @blur="saveDuration"
                      @input="temporaryDuration = $event"
                      :value="duration"
                      :disabled="dateLocked === 'neither'"
                      label="number of days to fetch:"
                      class="mr-5"
                      :rules="[ruleIsNumber]"
                    ></v-text-field>
                  </v-form>
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import { mdiCalendar, mdiLock, mdiLockOpenVariant } from "@mdi/js";
type TimeField = {
  ref: string;
  value: string;
  role: "start" | "end";
  opened: boolean;
  saveFunction: (field: TimeField, value: string) => void;
  rules: ((input: string) => string | boolean)[];
  allowedDates?: (input: string) => boolean;
};

export default defineComponent({
  data() {
    return {
      temporaryDuration: "",
      dateLocked: "end" as "start" | "end" | "neither",
      timeFields: [] as TimeField[],
      dateIcon: mdiCalendar,
      openLock: mdiLockOpenVariant,
      lock: mdiLock,
    };
  },
  computed: {
    duration(): number {
      const timeDiff = this.endTime.getTime() - this.startTime.getTime();

      return Math.ceil(timeDiff / (1000 * 3600 * 24)); // round up to days
    },
    startTimeString(): string {
      return this.startTime.toISOString().substring(0, 10);
    },
    endTimeString(): string {
      return this.endTime.toISOString().substring(0, 10);
    },
    today() {
      return new Date().toISOString().substr(0, 10);
    },
  },
  created() {
    this.temporaryDuration = "" + this.duration;
    this.timeFields = [
      {
        ref: "startDateMenu",
        value: this.startTimeString,
        role: "start",
        opened: false,
        saveFunction: this.saveStartTime,
        allowedDates: this.allowedDatesStart,
        rules: [],
      },
      {
        ref: "endDateMenu",
        value: this.endTimeString,
        role: "end",
        opened: false,
        saveFunction: this.saveEndTime,
        allowedDates: this.allowedDatesEnd,
        rules: [this.ruleStopAfterStart],
      },
    ];
  },
  methods: {
    allowedDatesStart(dateString: string) {
      return new Date(dateString) <= this.endTime;
    },
    allowedDatesEnd(dateString: string) {
      return new Date(dateString) >= this.startTime;
    },
    saveStartTime(field: TimeField, value: string) {
      this.saveMenu(field.ref, value);
      this.$emit("update:startTime", new Date(value));
      this.$emit("reload-graph-data");
    },
    saveEndTime(field: TimeField, value: string) {
      this.saveMenu(field.ref, value);
      this.$emit("update:endTime", new Date(value));
      this.$emit("reload-graph-data");
    },
    saveMenu(ref: string, value: string) {
      let field: any;
      const readRef = this.$refs[ref];

      if (readRef instanceof Vue) {
        field = readRef;
      } else if (Array.isArray(readRef)) {
        field = readRef[0];
      }
      field.save(value);
    },
    saveDuration() {
      const duration = parseInt(this.temporaryDuration);

      if (isNaN(duration)) {
        return;
      }

      if (this.duration === duration) {
        // Do not fetch everything again
        return;
      }

      const durationAsMillis = duration * 1000 * 60 * 60 * 24; // ms * minutes * hours * days

      if (this.dateLocked === "start") {
        this.$emit(
          "update:endTime",
          new Date(this.startTime.getTime() + durationAsMillis),
        );
      } else {
        this.$emit(
          "update:startTime",
          new Date(this.endTime.getTime() - durationAsMillis),
        );
      }

      this.$emit("reload-graph-data");
    },
    lockDates(date: "start" | "end"): void {
      if (this.dateLocked === "neither") {
        this.dateLocked = date;
      } else if ((this.dateLocked = date)) {
        this.dateLocked = "neither";
      } else {
        this.dateLocked = this.dateLocked === "start" ? "end" : "start";
      }
    },
    ruleStopAfterStart(input: string): boolean | string {
      return this.startTime <= new Date(input)
        ? true
        : "You have to select a date after the first one!";
    },
    ruleIsNumber(input: string): string | boolean {
      return isNaN(parseInt(input)) ? "Please enter a number" : true;
    },
  },
  props: {
    endTime: {
      type: Object as PropType<Date>,
      required: true,
    },
    startTime: {
      type: Object as PropType<Date>,
      required: true,
    },
  },
});
</script>
