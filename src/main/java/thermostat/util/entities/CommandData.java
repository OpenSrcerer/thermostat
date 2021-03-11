package thermostat.util.entities;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;

import java.util.List;
import java.util.Map;

/**
 * Contains parameters and data for a Command call.
 */
public class CommandData {
    public GuildMessageReceivedEvent event;
    public Map<String, List<String>> parameters;
    public final String prefix;
    public final long commandId;

    public CommandData(final GuildMessageReceivedEvent event)
    {
        this.commandId = MiscellaneousFunctions.getCommandId();
        this.prefix = null;
        this.parameters = null;
        this.event = event;
    }

    public CommandData(final GuildMessageReceivedEvent event, final String prefix)
    {
        this.commandId = MiscellaneousFunctions.getCommandId();
        this.prefix = prefix;
        this.parameters = null;
        this.event = event;
    }

    public CommandData(final GuildMessageReceivedEvent event,
                       final List<String> arguments,
                       final String prefix)
    {
        this.commandId = MiscellaneousFunctions.getCommandId();
        this.prefix = prefix;

        try {
            parameters = ArgumentParser.parseArguments(arguments);
        } catch (Exception ex) {
            parameters = null;
        }

        this.event = event;
    }
}
