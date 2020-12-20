package thermostat.thermoFunctions.commands.monitoring.synapses;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SynapseMonitor implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SynapseMonitor.class);
    public static final ChronoUnit millis = ChronoUnit.MILLIS;

    /**
     * Synapse that started this monitoring event.
     */
    private final Synapse synapse;

    /**
     * ID of this Command.
     */
    private final long commandId;

    /**
     * Create a new Monitor event for each Synapse.
     * @param synapse Synapse to take as an argument.
     */
    public SynapseMonitor(Synapse synapse) {
        this.synapse = synapse;
        this.commandId = Functions.getCommandId();

        CommandDispatcher.queueCommand(this);
    }

    @Override
    public void run() {
        Guild synapseGuild = Thermostat.thermo.getGuildById(synapse.getGuildId());
        if (synapseGuild == null) {
            // ignore/log null guild
            return;
        }

        for (String channelId : synapse.getChannels()) {
            TextChannel channel = synapseGuild.getTextChannelById(channelId);
            if (channel == null) {
                // ignore/log null channel
                return;
            }

            channel.getHistoryBefore(channel.getLatestMessageId(), 10)
                    .queue(
                            history -> {
                                OffsetDateTime timeNow = OffsetDateTime.now().toInstant().atOffset(ZoneOffset.UTC).truncatedTo(millis);
                                List<Message> messages = history.getRetrievedHistory();

                                System.out.println(synapseGuild.getName() + "(" + messages.get(0).getContentRaw() + "): " + millis.between(messages.get(0).getTimeCreated(), timeNow));
                                if (millis.between(messages.get(0).getTimeCreated(), timeNow) > 64000) {
                                    SynapseFunctions.putSlowmode(channel, Integer.MIN_VALUE);
                                } else {
                                    SynapseFunctions.slowmodeSwitch(
                                            channel,
                                            SynapseFunctions.calculateAverageTime(messages),
                                            millis.between(messages.get(0).getTimeCreated(), timeNow)
                                    );
                                }
                            }
                    );


        }

        lgr.info(this.toString());
    }

    @Nullable
    public Guild getSynapseGuild() {
        return Thermostat.thermo.getGuildById(synapse.getGuildId());
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNAPSE_MONITOR;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public long getId() {
        return commandId;
    }

    @Nullable
    @Override
    public GuildMessageReceivedEvent getEvent() {
        return null;
    }
}
