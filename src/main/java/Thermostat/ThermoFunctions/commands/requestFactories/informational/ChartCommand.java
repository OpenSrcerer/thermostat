package thermostat.thermoFunctions.commands.requestFactories.informational;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.entities.RequestType;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ChartCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(ChartCommand.class);
    private final CommandData data;

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

    public ChartCommand(@Nonnull CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.CHART, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!chart <charttype> [channel]
     * @return
     */
    @Override
    public MessageEmbed execute() {
        // prefix removed (sends info msg)
        if (data.arguments().isEmpty()) {
            Messages.sendMessage(data.channel(), HelpEmbeds.helpChart(data.prefix()));
        }

        // <charttype>
        else if (data.arguments().size() == 1) {
            // freq chart
            if (data.arguments().get(0).equalsIgnoreCase("slowfreq")) {
                frequencyChart(data.guild(), data.channel(), data.member());
            } else {
                Messages.sendMessage(data.channel(), HelpEmbeds.helpChart(data.prefix()));
            }
            data.arguments().remove(0);
        }

        // <charttype> [channel]
        else {
            TextChannel argumentChannel = data.guild().getTextChannelById(Functions.parseMention(data.arguments().get(1), "#"));

            if (argumentChannel == null) {
                Messages.sendMessage(data.channel(), ErrorEmbeds.channelNotFound(data.arguments().get(1)));
            } else if ((data.arguments().get(1).equalsIgnoreCase("slowfreq"))) {
                frequencyChart(data.guild(), argumentChannel, data.member());
            } else {
                Messages.sendMessage(data.channel(), HelpEmbeds.helpChart(data.prefix()));
            }
        }
    }

    public static void frequencyChart(Guild eventGuild, TextChannel eventChannel, Member eventMember) {

        // #1 - Retrieve data through the database relevant to the chart.
        Map<String, Integer> top5slowmode =
                DataSource.queryMap("SELECT CHANNEL_ID, MANIPULATED  FROM CHANNELS WHERE GUILD_ID = ?"
                        + " AND (MANIPULATED != 0) ORDER BY MANIPULATED DESC LIMIT 5", eventGuild.getId());

        // #2 - If not enough data on the chart, channels were never slowmoded
        // in this guild.
        if (top5slowmode == null) {
            Messages.sendMessage(eventChannel, GenericEmbeds.noChannelsEverSlowmoded());
            return;
        }

        // #3 - Create chart and put data in it.
        CategoryChart chart =
                new CategoryChartBuilder()
                        .width(800)
                        .height(600)
                        .title("Thermostat Slowmode Chart for " + eventGuild.getName())
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

        // Add values to chart series
        for (Map.Entry<String, Integer> entry : top5slowmode.entrySet()) {
            String channelName = entry.getKey();
            TextChannel channel = eventGuild.getTextChannelById(entry.getKey());

            if (channel != null) {
                channelName = channel.getName();
            }

            chart.addSeries(
                    channelName,
                    new ArrayList<>(
                            Collections.singletonList("-")),
                            new ArrayList<Number>(Collections.singletonList(entry.getValue())
                    )
            );
        }

        // Send message through an inputStream of bits
        InputStream inputStream;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", baos);
            inputStream = new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
            lgr.warn("(" + eventGuild.getName() + "/" + eventGuild.getId() + ") - " + ex.toString());
            return;
        }
        Messages.sendMessage(eventChannel, inputStream, GenericEmbeds.chartHolder(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl(), eventGuild.getName()));
        lgr.info("Successfully executed on (" + eventGuild.getName() + "/" + eventGuild.getId() + ").");
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
