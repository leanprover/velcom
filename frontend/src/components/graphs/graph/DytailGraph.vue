<template>
  <v-container fluid style="height: 100%" class="mx-0 px-0">
    <v-row
      align="center"
      justify="center"
      style="height: 100%"
      class="mx-0 px-0"
    >
      <v-col style="height: 100%" class="mx-0 px-0">
        <div
          id="chart"
          @wheel="$emit('wheel', $event)"
          style="height: 100%; width: 100%"
        ></div>
        <div id="ranger" :style="{ height: '30px' }"></div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue, { defineComponent, PropType } from "vue";
import Dygraph from "dygraphs";
import {
  AttributedDatapoint,
  DetailDataPoint,
  GraphDataPoint,
  SeriesId,
  SeriesInformation,
} from "@/store/types";
import { vxm } from "@/store";
import "dygraphs/css/dygraph.css";
import Crosshair from "dygraphs/src/extras/crosshair.js";
import { escapeHtml } from "@/util/Texts";
import GraphDatapointDialog from "@/components/graphs/helper/GraphDatapointDialog.vue";
import { formatDate } from "@/util/Times";

// eslint-disable-next-line no-undef
type RealOptions = dygraphs.Options & {
  rangeSelectorPlotLineWidth?: number;
  rangeSelectorAlpha?: number;
};

export default defineComponent({
  components: {
    "datapoint-dialog": GraphDatapointDialog,
  },
  data() {
    const pointDialogOpen: boolean = false;
    const pointDialogSeries: SeriesInformation | null = null;
    const pointDialogDatapoint: DetailDataPoint | null = null;
    const graph: Dygraph = undefined;

    return {
      graph,
      pointDialogDatapoint,
      pointDialogSeries,
      pointDialogOpen,
      fancyPanningInteractions: Object.assign(
        {},
        Dygraph.defaultInteractionModel,
        {
          mouseup: (event: MouseEvent, g: any, context: any) => {
            if (context.isPanning) {
              (Dygraph as any).endPan(event, g, context);
              this.dygraphsPanned();
            }
          },
        },
      ),
    };
  },
  computed: {
    numberFormat(): Intl.NumberFormat {
      return new Intl.NumberFormat(
        new Intl.NumberFormat().resolvedOptions().locale,
        { maximumFractionDigits: 3 },
      );
    },
    darkTheme() {
      return vxm.userModule.darkThemeSelected;
    },
    graphFailedOrUnbenchmarkedColor() {
      return this.$vuetify.theme.currentTheme
        .graphFailedOrUnbenchmarked as string;
    },
    selectorAlpha(): number {
      return vxm.userModule.darkThemeSelected ? 0.2 : 0.7;
    },
    selectorLineWidth(): number {
      return vxm.userModule.darkThemeSelected ? 2 : 1.5;
    },
  },
  mounted(): void {
    window.addEventListener("resize", this.onResize);

    // empty string because div is not expected to be NULL
    const chartDiv = document.getElementById("chart") || "";

    this.graph = new Dygraph(
      // containing div
      chartDiv,

      [[1, 1]],

      {
        axes: {
          x: {
            axisLineColor: "currentColor",
            axisLabelFormatter: (
              x: number | Date,
              granularity: number,
              opts: (name: string) => any,
              dygraph: Dygraph,
            ) => {
              const [start, end] = dygraph.xAxisRange();
              if (x instanceof Date) {
                return this.xAxisFormatter(x, [start, end]);
              }
              return x % 1 === 0
                ? this.xAxisFormatter(new Date(x), [start, end])
                : "";
            },
          },
          y: {
            axisLineColor: "currentColor",
          },
        },
        ylabel: "",
        pointClickCallback: this.pointClickCallback,
        labels: ["", ""], // will be replaced in update
        legend: "follow",
        labelsSeparateLines: true,
        connectSeparatedPoints: true,
        drawPoints: false,
        animatedZooms: false, // does not work with slider
        pointSize: 3,
        panEdgeFraction: 0.5,
        zoomCallback: this.dygraphsZoomed,
        legendFormatter: this.tooltipFormatter,
        crosshairColor: "currentColor",
        showRangeSelector: true,
        rangeSelectorHeight: 30,
        rangeSelectorPlotLineWidth: this.selectorLineWidth,
        rangeSelectorAlpha: this.selectorAlpha,
        rangeSelectorForegroundLineWidth: 0.5,
        rangeSelectorPlotFillColor: "",
        rangeSelectorPlotStrokeColor: "grey",
        rangeSelectorBackgroundStrokeColor: "currentColor",
        plugins: [new Crosshair({ direction: "vertical" })],
        drawHighlightPointCallback: this.drawHighlightPointCallback as any,
        highlightCircleSize: 5,
        // and, to keep the ability to brush and zoom:
        interactionModel: this.fancyPanningInteractions,
      } as RealOptions,
    );

    this.update();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.onResize);
  },
  methods: {
    datapointByPositionDate(positionDate: number): DetailDataPoint | undefined {
      return this.datapoints.find(
        (point) => point.positionTime.getTime() === positionDate,
      ) as DetailDataPoint | undefined;
    },
    firstSuccessful(series: SeriesId): number {
      for (const datapoint of this.datapoints) {
        const value = datapoint.values.get(series);
        if (typeof value === "number") {
          return value;
        }
      }
      return 0;
    },
    onResize() {
      window.requestAnimationFrame(() => {
        this.graph.resize();
      });
    },
    xAxisFormatter(d: Date, [start, end]: [number, number]): string {
      const dateString: string = d.getMonth() + 1 + "-" + d.getDate();
      const hourString: string | number =
        d.getHours().toString().length === 1
          ? "0" + d.getHours()
          : d.getHours();
      const minuteString: string | number =
        d.getMinutes().toString().length === 1
          ? "0" + d.getMinutes()
          : d.getMinutes();

      const daysShown: number = Math.floor(
        (end - start) / (1000 * 60 * 60 * 24),
      );
      if (daysShown < 2) {
        return hourString + ":" + minuteString + "\n" + dateString;
      }
      return dateString + "\n" + d.getFullYear();
    },
    tooltipFormatter(legendData: dygraphs.LegendData) {
      const datapoint = this.datapointByPositionDate(legendData.x);
      if (datapoint) {
        const data = legendData.series.slice();
        // Sort them so the order corresponds to the order of the lines
        data.sort((a, b) => b.y - a.y);

        const seriesRows = data.map((val) => {
          const color = val.color;
          const series = this.seriesInformation.find(
            (it) => escapeHtml(it.id.toString()) === val.labelHTML,
          );
          let safeSeriesId = "N/A";
          if (series !== undefined) {
            safeSeriesId = escapeHtml(series.displayName);
          }

          let value: string = this.numberFormat.format(val.y);
          if (series && datapoint.commitUnbenchmarked(series.id)) {
            value = "Commit was not benchmarked";
          } else if (series && datapoint.metricNotBenchmarked(series.id)) {
            value = "Metric not measured for commit";
          } else if (series && !datapoint.successful(series.id)) {
            value = "Failed";
          }

          return `
                <tr>
                  <td>
                    <span class="color-preview" style="background-color: ${color}"></span>
                    ${safeSeriesId}
                  </td>
                  <td>${value}</td>
                </tr>
                `;
        });

        return `<table class="dygraphs-tooltip-table">
                  <tr>
                    <td>Hash</td>
                    <td>${escapeHtml(datapoint.hash)}</td>
                  </tr>
                  </tr>
                    <td>Message</td>
                    <td>${escapeHtml(datapoint.summary)}</td>
                  </tr>
                  <tr>
                    <td>Author</td>
                    <td>
                      ${escapeHtml(datapoint.author)} at
                      ${formatDate(datapoint.committerTime)}
                    </td>
                  </tr>
                 ${seriesRows.join("\n")}
              </table>
            `;
      }
      return "something went wrong :(\n couldn't find commit";
    },
    drawHighlightPointCallback(
      g: Dygraph,
      seriesName: string,
      canvasContext: CanvasRenderingContext2D,
      cx: number,
      cy: number,
      color: string,
      pointSize: number,
      idx: number,
    ) {
      const datapoint: DetailDataPoint = this.datapoints[
        idx
      ] as DetailDataPoint;
      const series = this.seriesInformation.find(
        (it) => it.id.toString() === seriesName,
      );
      if (!series) {
        return;
      }

      canvasContext.strokeStyle = color;

      if (datapoint.failed(series.id)) {
        // gray cross icon
        canvasContext.beginPath();
        canvasContext.strokeStyle = this.graphFailedOrUnbenchmarkedColor;
        canvasContext.lineWidth = 4;
        canvasContext.moveTo(cx - pointSize, cy - pointSize);
        canvasContext.lineTo(cx + pointSize, cy + pointSize);
        canvasContext.stroke();

        canvasContext.moveTo(cx + pointSize, cy - pointSize);
        canvasContext.lineTo(cx - pointSize, cy + pointSize);
        canvasContext.stroke();
        canvasContext.closePath();
      } else if (datapoint.unbenchmarked(series.id)) {
        // grey empty circle
        canvasContext.beginPath();
        canvasContext.lineWidth = 2;
        canvasContext.strokeStyle = this.graphFailedOrUnbenchmarkedColor;
        canvasContext.arc(cx, cy, pointSize, 0, 360);
        canvasContext.stroke();
        canvasContext.closePath();
      } else {
        // filled in circle in dimension color
        canvasContext.beginPath();
        canvasContext.lineWidth = 2;
        canvasContext.arc(cx, cy, pointSize, 0, 360);
        canvasContext.stroke();
        canvasContext.fill();
        canvasContext.closePath();
      }
    },
    drawMarkers() {
      const annotations = [];
      if (vxm.detailGraphModule.referenceDatapoint) {
        const { seriesId, datapoint } =
          vxm.detailGraphModule.referenceDatapoint;
        annotations.push({
          series: seriesId,
          x: datapoint.positionTime.getTime(),
          shortText: "R",
          text: "Reference datapoint",
          width: 20,
          height: 20,
          tickHeight: 20,
        });
      }

      this.graph.setAnnotations(annotations);
    },
    pointClickCallback(e: MouseEvent, graphPoint: any) {
      const datapoint: DetailDataPoint | undefined = graphPoint.xval
        ? this.datapointByPositionDate(graphPoint.xval)
        : undefined;

      if (!datapoint) {
        return;
      }

      // Datapoint dialog on right click
      if (e.button === 2) {
        this.pointDialogDatapoint = datapoint;
        this.pointDialogSeries = this.seriesInformation.find(
          (it) => it.id.toString() === graphPoint.name,
        )!;
        this.pointDialogOpen = true;

        return;
      }

      this.$emit("click", {
        datapoint,
        ctrl: e.ctrlKey,
      });
    },
    dygraphsPanned() {
      if (this.dataRangeMax.getTime() < this.graph.xAxisRange()[1]) {
        this.$emit("update:dataRangeMax", new Date(this.graph.xAxisRange()[1]));
        this.$emit("update:zoomXEndValue", this.graph.xAxisRange()[1]);
      } else if (this.dataRangeMin.getTime() > this.graph.xAxisRange()[0]) {
        this.$emit("update:dataRangeMin", new Date(this.graph.xAxisRange()[0]));
        this.$emit("update:zoomXStartValue", this.graph.xAxisRange()[0]);
      }
    },
    dygraphsZoomed(startX: number, endX: number, yRanges: [number, number][]) {
      if (this.graph.isZoomed("x")) {
        this.$emit("update:zoomXStartValue", startX);
        this.$emit("update:zoomXEndValue", endX);
      } else {
        this.$emit("update:zoomXStartValue", this.dataRangeMin.getTime());
        this.$emit("update:zoomXEndValue", this.dataRangeMax.getTime());
      }

      if (this.graph.isZoomed("y")) {
        const [yZoomStart, yZoomEnd] = yRanges[0];
        this.$emit("update:zoomYStartValue", yZoomStart);
        this.$emit("update:zoomYEndValue", yZoomEnd);
      } else {
        this.$emit("update:zoomYStartValue", null);
        this.$emit("update:zoomYEndValue", null);
      }
    },
    update() {
      const data: number[][] = [];

      // One array entry = #dimensions data points per commit
      // each array entry has the form [x-val, dim1, dim2, ...]
      for (let i = 0; i < this.datapoints.length; i++) {
        data[i] = [this.datapoints[i].positionTime.getTime()];
      }

      for (const series of this.seriesInformation) {
        let lastValue = this.firstSuccessful(series.id);
        this.datapoints
          .filter((it) => it.values.has(series.id))
          .forEach((point, index) => {
            let pointValue = point.values.get(series.id);
            if (typeof pointValue !== "number") {
              pointValue = lastValue;
            }
            lastValue = pointValue;

            data[index].push(pointValue);
          });
      }

      this.graph.updateOptions({
        file: data,
        labels: ["x", ...this.seriesInformation.map((it) => escapeHtml(it.id))],
        colors: this.seriesInformation.map((it) => it.color),
        dateWindow: [
          this.zoomXStartValue || this.dataRangeMin.getTime(),
          this.zoomXEndValue || this.dataRangeMax.getTime(),
        ],
        valueRange: [this.zoomYStartValue, this.zoomYEndValue],
        includeZero: this.beginYAtZero,
        rangeSelectorPlotLineWidth: this.selectorLineWidth,
        rangeSelectorAlpha: this.selectorAlpha,
      } as RealOptions);

      this.drawMarkers();
    },
  },
  props: {
    beginYAtZero: { default: false, type: Boolean },
    zoomXStartValue: {
      type: Object as PropType<number | null>,
    },
    zoomXEndValue: {
      type: Object as PropType<number | null>,
    },
    zoomYStartValue: {
      type: Object as PropType<number | null>,
    },
    zoomYEndValue: {
      type: Object as PropType<number | null>,
    },
    dataRangeMin: {
      type: Object as PropType<Date>,
    },
    dataRangeMax: {
      type: Object as PropType<Date>,
    },
    datapoints: {
      type: Array as PropType<GraphDataPoint[]>,
    },
    seriesInformation: {
      type: Array as PropType<SeriesInformation[]>,
    },
    visiblePointCount: {
      type: Number,
    },
    commitToCompare: {
      default: null,
      type: Object as PropType<AttributedDatapoint | null>,
    },
    referenceDatapoint: {
      default: null,
      type: Object as PropType<AttributedDatapoint | null>,
    },
    refreshKey: { default: 0, type: Number },
  },
  watch: {
    datapoints: [
      {
        handler: "update",
      },
    ],
    dimensions: [
      {
        handler: "update",
      },
    ],
    beginYAtZero: [
      {
        handler: "update",
      },
    ],
    darkTheme: [
      {
        handler: "update",
      },
    ],
    refreshKey: [
      {
        handler: "update",
      },
    ],
  },
});
</script>

<style>
/*noinspection CssUnusedSymbol*/
.dygraph-legend {
  position: absolute;
  display: block;
  width: auto;
  border-style: solid;
  white-space: nowrap;
  z-index: 9999999;
  box-shadow: rgba(0, 0, 0, 0.2) 1px 2px 10px;
  transition:
    opacity 0.2s cubic-bezier(0.23, 1, 0.32, 1) 0s,
    visibility 0.2s cubic-bezier(0.23, 1, 0.32, 1) 0s;
  background-color: rgb(255, 255, 255);
  border-width: 1px;
  border-radius: 4px;
  color: rgb(102, 102, 102);
  font: 14px / 21px sans-serif;
  padding: 10px;
  border-color: rgb(255, 255, 255);
  pointer-events: none;
}

.dygraphs-tooltip-table tr td {
  padding: 2px;
}

.dygraphs-tooltip-table tr td:nth-child(2) {
  font-family: monospace;
}
.dygraphs-tooltip-table tr td:first-child {
  padding-right: 10px;
}
.dygraphs-tooltip-table tr td:only-child {
  font-weight: bold;
  padding-top: 1em;
  font-size: 1.1em;
}

/*noinspection CssUnusedSymbol*/
.dygraphs-tooltip-table .color-preview {
  width: 10px;
  height: 10px;
  border-radius: 25%;
  display: inline-block;
}

/*noinspection CssUnusedSymbol*/
.dygraph-rangesel-fgcanvas {
  margin-top: 15px;
}

/*noinspection CssUnusedSymbol*/
.dygraph-rangesel-bgcanvas {
  margin-top: 15px;
}

/*noinspection CssUnusedSymbol*/
.dygraph-rangesel-zoomhandle {
  margin-top: 15px;
}

/*noinspection CssUnusedSymbol*/
.dygraph-axis-label {
  color: currentColor;
}
</style>
