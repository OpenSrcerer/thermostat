package thermostat.util;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;

/**
 * Static utility methods to create specific charts.
 */
public final class ThermosCharts {

    // Stylizing for Charts
    private static final Color
            discordGray = new Color(54, 57, 63),
            discordGrayer = new Color(47, 49, 54);

    private static final Color[] chartTheme = new Color[]{
            new Color(0, 63, 92),
            new Color(88, 80, 141),
            new Color(188, 80, 144),
            new Color(255, 99, 97),
            new Color(255, 166, 0)
    };

    private static final Font titleFont = new Font("Arial", Font.BOLD, 16);

    /**
     * Construct a CategoryChart using Thermostat's theme.
     * @param guildName Name of the guild this chart will be used for.
     * @return A CategoryChart that is used for slowfreq charts.
     */
    public static CategoryChart getFrequencyChart(String guildName) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Thermostat Slowmode Chart for " + guildName)
                .xAxisTitle("Channels")
                .yAxisTitle("Times Slowmode was activated")
                .theme(Styler.ChartTheme.GGPlot2)
                .build();

        // Customize Chart Positions
        chart.getStyler()
                .setPlotGridVerticalLinesVisible(false)
                .setXAxisTitleVisible(false)
                .setXAxisTicksVisible(false)
                .setHasAnnotations(true)
                .setShowTotalAnnotations(true)
                .setAnnotationsPosition(0.5f)
                .setAntiAlias(true);

        // Customize Chart Colors
        {
            chart.getStyler()
                    .setAnnotationsFont(new Font("Verdana", Font.BOLD, 18))
                    .setAnnotationsFontColor(Color.WHITE)
                    .setChartTitleFont(titleFont)
                    .setChartFontColor(Color.WHITE)
                    .setChartBackgroundColor(discordGray)
                    .setYAxisGroupTitleColor(0, Color.WHITE)
                    .setChartTitleBoxBackgroundColor(discordGray)
                    .setPlotBackgroundColor(discordGrayer)
                    .setLegendBackgroundColor(discordGrayer)
                    .setSeriesColors(chartTheme);
        }

        return chart;
    }
}
