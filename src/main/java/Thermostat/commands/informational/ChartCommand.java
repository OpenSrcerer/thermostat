package thermostat.commands.informational;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.Embeds.GenericEmbeds;
import thermostat.util.Functions;
import thermostat.commands.Command;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChartCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(ChartCommand.class);

    private final GuildMessageReceivedEvent data;
    private final List<String> arguments;
    private final String prefix;
    private final long commandId;

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

    public ChartCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!chart <charttype> [channel]
     */
    @Override
    public void run() {
        // prefix removed (sends info msg)
        if (arguments.isEmpty()) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("No arguments provided. Chart Type is required.", commandId),
                    "User did not provide arguments.");
        }

        // <charttype>
        else if (arguments.size() == 1) {
            // freq chart
            if (arguments.get(0).equalsIgnoreCase("slowfreq")) {
                frequencyChart(data.getGuild(), data.getMember());
            } else {
                ResponseDispatcher.commandFailed(this,
                        ErrorEmbeds.inputError("\"" + arguments.get(0) + "\" chart type does not exist.", commandId),
                        "User provided an incorrect chart type.");
            }
            arguments.remove(0);
        }

        // <charttype> [channel]
        else {
            // Retrieve channel from last argument
            TextChannel argumentChannel = data.getGuild().getTextChannelById(Functions.parseMention(arguments.get(1), "#"));

            if (argumentChannel == null) {
                ResponseDispatcher.commandFailed(this,
                        ErrorEmbeds.inputError("Channel \"" + arguments.get(1) + "\" was not found.", commandId),
                        "Channel that user provided wasn't found.");
            } else if ((arguments.get(0).equalsIgnoreCase("slowfreq"))) {
                frequencyChart(data.getGuild(), data.getMember());
            } else {
                ResponseDispatcher.commandFailed(this,
                        ErrorEmbeds.inputError("\"" + arguments.get(0) + "\" chart type does not exist.", commandId),
                        "User provided an incorrect chart type.");
            }
        }
    }

    public void frequencyChart(Guild eventGuild, Member eventMember) {
        // #1 - Retrieve data through the database relevant to the chart.
        Map<String, Integer> top5slowmode;
        try {
            top5slowmode = DataSource.queryMap("SELECT CHANNEL_ID, MANIPULATED FROM CHANNELS WHERE GUILD_ID = ?"
                    + " AND (MANIPULATED != 0) ORDER BY MANIPULATED DESC LIMIT 5", eventGuild.getId());
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(), Functions.getCommandId()),
                    "Exception thrown while querying slowmode data.");
            return;
        }

        // #2 - If not enough data on the chart, channels were never slowmoded
        // in this guild.
        if (top5slowmode.isEmpty()) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error("Could not pull top slowmode data from database because no channels were ever slowmoded in your guild.",
                            "Get some channels slowmoded with `th!monitor`.", Functions.getCommandId()),
                    "Channels were never slowmoded in this guild.");
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
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(), Functions.getCommandId()),
                    ex);
            return;
        }

        ResponseDispatcher.commandSucceeded(this,
                GenericEmbeds.chartHolder(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl(), eventGuild.getName()),
                inputStream);
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.CHART;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public long getId() {
        return commandId;
    }
}
