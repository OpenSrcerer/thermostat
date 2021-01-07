package thermostat.commands.moderation;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import thermostat.commands.Command;
import thermostat.enumeration.CommandType;

public class PurgeCommand implements Command {
    @Override
    public void run() {

    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return null;
    }

    @Override
    public CommandType getType() {
        return null;
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }
}
