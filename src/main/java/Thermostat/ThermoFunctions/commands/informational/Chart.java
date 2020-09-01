package thermostat.thermoFunctions.commands.informational;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import thermostat.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;

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

public class Chart {

    public static void execute(@Nonnull ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember, String prefix) {

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, Embeds.userNoPermission());
            return;
        }

        // chart (sends info message)
        if (args.size() == 1) {
            Messages.sendMessage(eventChannel, Embeds.helpChart(prefix));
        }

        // chart <charttype>
        else if (args.size() == 2) {
            // removes command
            args.remove(0);

            // freq chart
            if (args.get(0).equalsIgnoreCase("slowfreq")) {
                frequencyChart(eventGuild, eventChannel, eventMember);
            }
        }

        // chart [channel] <charttype>
        else {
            args.remove(0);

            TextChannel argumentChannel = eventGuild.getTextChannelById(args.get(0));

            if ((args.get(1).equalsIgnoreCase("slowfreq"))) {
                frequencyChart(eventGuild, argumentChannel, eventMember);
            }
        }
    }

    public static void frequencyChart(Guild eventGuild, TextChannel eventChannel, Member eventMember) {

        Map<String, Integer> top5slowmode =
                DataSource.queryMap("SELECT CHANNEL_ID, MANIPULATED  FROM CHANNELS WHERE GUILD_ID = ?"
                        + " AND (MANIPULATED != 0) ORDER BY MANIPULATED DESC LIMIT 5", eventGuild.getId());

        if (top5slowmode == null) {
            Messages.sendMessage(eventChannel, Embeds.noChannelsEverSlowmoded());
            return;
        }

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

        {
            Color discordGray = new Color(54, 57, 63);
            Color discordGrayer = new Color(47, 49, 54);
            Color[] chartTheme = new Color[]{
                    new Color(0, 63, 92),
                    new Color(88, 80, 141),
                    new Color(188, 80, 144),
                    new Color(255, 99, 97),
                    new Color(255, 166, 0)
            };
            Font titleFont = new Font("Arial", Font.BOLD, 16);

            // Customize Chart Colors
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

        InputStream inputStream = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", baos);
            inputStream = new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ex) {
            Messages.sendMessage(eventChannel, Embeds.ioError());
        }

        Messages.sendMessage(eventChannel, inputStream, Embeds.chartHolder(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl(), eventGuild.getName()));
    }
}
