package thermostat.commands.informational;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.util.ThermosCharts;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static thermostat.util.ArgumentParser.hasArguments;

public class ChartCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(ChartCommand.class);
    private final CommandData data;

    public ChartCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.HELP_CHART, this.data),
                    "No arguments provided.");
            return;
        }

        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!chart --type [type] -c [channel/s]
     * Switches:
     * -type (Chart Type)
     * -c (Channel)
     */
    @Override
    public void run() {
        final List<String> chartType = data.parameters.get("-type");
        final List<String> channels = data.parameters.get("c");

        // prefix removed (sends info msg)
        if (!hasArguments(chartType)) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.HELP_CHART, data),
                    "User did not provide arguments."
            );
            return;
        }

        switch (chartType.get(0).toLowerCase()) {
            case "slowfreq" -> sendFrequencyChart();
            default -> ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data, "Chart \"" + chartType.get(0) + "\" does not exist."),
                    "User provided an incorrect chart type."
            );
        }
    }

    public void sendFrequencyChart() {
        // #1 - Retrieve data through the database relevant to the chart.
        final Map<String, Integer> top5slowmode = new LinkedHashMap<>();

        try {
            DataSource.execute(conn -> {
                PreparedStatement statement = conn.prepareStatement("SELECT CHANNEL_ID, MANIPULATED FROM CHANNELS WHERE GUILD_ID = ?"
                        + " AND (MANIPULATED != 0) ORDER BY MANIPULATED DESC LIMIT 5");
                statement.setString(1, data.event.getGuild().getId());
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    top5slowmode.put(rs.getString(1), rs.getInt(2));
                }
                return null;
            });
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    "Exception thrown while querying slowmode data.");
            return;
        }

        // #2 - If not enough data on the chart, channels were never slowmoded
        // in this guild.
        if (top5slowmode.isEmpty()) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_FIX, data,
                            Arrays.asList(
                                    "Could not pull top slowmode data from database because no channels were ever slowmoded in your guild.",
                                    "Get some channels slowmoded with `th!monitor`.")
                            ),
                    "Channels were never slowmoded in this guild.");
            return;
        }

        // #3 - Create chart and put data in it.
        CategoryChart chart = ThermosCharts.getFrequencyChart(data.event.getGuild().getName());

        // Add values to chart series
        for (Map.Entry<String, Integer> entry : top5slowmode.entrySet()) {
            String channelName = entry.getKey();
            TextChannel channel = data.event.getGuild().getTextChannelById(entry.getKey());

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
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
            return;
        }

        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(
                        EmbedType.CHART_HOLDER,
                        data,
                        data.event.getGuild().getName()
                ), inputStream
        );
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public CommandData getData() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.CHART;
    }
}
