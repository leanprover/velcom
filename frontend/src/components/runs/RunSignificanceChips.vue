<template>
  <v-row
    no-gutters
    class="ma-0 d-flex flex-wrap spaced"
    justify="center"
    :justify-sm="center ? 'center' : 'start'"
  >
    <v-col
      v-for="relevantChange in relevantChanges"
      :key="
        relevantChange.oldRunId +
        relevantChange.id.benchmark +
        relevantChange.id.metric
      "
      cols="auto"
    >
      <v-chip
        label
        outlined
        active-class="hide-active-state"
        :color="relevantChange.color"
        :input-value="false"
        :to="linkLocation(relevantChange)"
      >
        <v-icon left :size="20">{{ relevantChange.icon }}</v-icon>
        {{ relevantChange.id.benchmark }} —
        {{ relevantChange.id.metric }}
        <span class="font-weight-bold pl-3" style="font-size: 1.1rem">
          {{ relevantChange.change }}
        </span>
      </v-chip>
    </v-col>
    <v-col
      v-for="failedDimension in failedSignificantDimensions"
      :key="failedDimension.toString()"
      cols="auto"
    >
      <v-chip label outlined color="warning" :input-value="false">
        <v-icon left :size="20">{{ failedIcon }}</v-icon>
        {{ failedDimension.toString() }}
        <span class="font-weight-bold pl-3" style="font-size: 1.1rem">
          Failed
        </span>
      </v-chip>
    </v-col>
  </v-row>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import {
  Dimension,
  DimensionDifference,
  DimensionInterpretation,
  RunId,
} from "@/store/types";
import {
  mdiChevronDoubleDown,
  mdiChevronDoubleUp,
  mdiChevronDown,
  mdiChevronTripleDown,
  mdiChevronTripleUp,
  mdiChevronUp,
  mdiCloseCircleOutline,
} from "@mdi/js";
const numberFormat: Intl.NumberFormat = new Intl.NumberFormat(
  new Intl.NumberFormat().resolvedOptions().locale,
  { maximumFractionDigits: 1, notation: "compact" },
);

const iconMappings = {
  up: {
    small: mdiChevronUp,
    middle: mdiChevronDoubleUp,
    large: mdiChevronTripleUp,
  },
  down: {
    small: mdiChevronDown,
    middle: mdiChevronDoubleDown,
    large: mdiChevronTripleDown,
  },
};

class RelevantChange {
  readonly id: Dimension;
  readonly change: string;
  readonly color: string;
  readonly icon: string;
  readonly oldRunId: RunId;

  constructor(difference: DimensionDifference) {
    this.oldRunId = difference.oldRunId;
    this.id = difference.dimension;
    if (difference.stddevDiff !== undefined) {
      let change = difference.relDiff
        ? this.formatPercentage(difference.relDiff)
        : this.formatNumber(difference.absDiff);
      change += ` (${this.formatNumber(difference.stddevDiff)} σ)`;

      this.change = change;
    } else {
      this.change =
        difference.relDiff && !this.id.benchmark.startsWith("~")
          ? this.formatPercentage(difference.relDiff)
          : this.formatNumber(difference.absDiff);
    }
    this.color = this.changeColor(
      difference.absDiff,
      difference.dimension.interpretation,
    );
    this.icon = this.changeIcon(difference.absDiff);
  }

  private formatPercentage(percentage: number): string {
    const scaled = Math.round(percentage * 1000) / 10;
    return `${scaled}%`;
  }

  private formatNumber(value: number): string {
    return numberFormat.format(value);
  }

  private changeColor(change: number, interpretation: DimensionInterpretation) {
    if (Math.abs(change) === 0 || isNaN(change)) {
      return "";
    }

    if (interpretation === "NEUTRAL") {
      return "";
    }

    let bad = false;
    if (interpretation === "LESS_IS_BETTER") {
      bad = change > 0;
    } else if (interpretation === "MORE_IS_BETTER") {
      bad = change < 0;
    }

    if (bad) {
      return "warning";
    }
    return "success";
  }

  private changeIcon(change: number): string {
    const MIDDLE_CHANGE_THRESHOLD = 3;
    const LARGE_CHANGE_THRESHOLD = 5;
    const adjustedChange = Math.abs(Math.round(change * 100));

    const direction: "up" | "down" = change >= 0 ? "up" : "down";

    let magnitude: "small" | "middle" | "large" = "small";
    if (adjustedChange >= LARGE_CHANGE_THRESHOLD) {
      magnitude = "large";
    } else if (adjustedChange >= MIDDLE_CHANGE_THRESHOLD) {
      magnitude = "middle";
    }

    return iconMappings[direction][magnitude];
  }
}

export default defineComponent({
  data() {
    return {
      failedIcon: mdiCloseCircleOutline,
    };
  },
  computed: {
    relevantChanges(): RelevantChange[] {
      return this.differences.map((it) => new RelevantChange(it));
    },
  },
  methods: {
    linkLocation(relevantChange: RelevantChange) {
      if (this.noLinks) {
        return undefined;
      }
      return {
        name: "run-comparison",
        params: {
          first: relevantChange.oldRunId,
          second: this.runId,
        },
      };
    },
  },
  props: {
    differences: {
      type: Array as PropType<DimensionDifference[]>,
      required: true,
    },
    runId: {
      type: Object as PropType<RunId>,
      required: true,
    },
    failedSignificantDimensions: {
      type: Array as PropType<Dimension[]>,
      default: () => [],
    },
    center: {
      type: Boolean,
      default: false,
    },
    noLinks: {
      type: Boolean,
      default: false,
    },
  },
});
</script>

<style scoped>
.spaced {
  gap: 8px;
}
.hide-active-state::before {
  background: transparent !important;
}
</style>
