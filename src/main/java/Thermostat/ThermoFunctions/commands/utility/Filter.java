package thermostat.thermoFunctions.commands.utility;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.Create;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.commands.monitoring.SetBounds;
import thermostat.thermoFunctions.entities.CommandType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thermostat.thermoFunctions.Functions.convertToBooleanInteger;


public class Filter implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(SetBounds.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventPrefix;
    private ArrayList<String> args;

    public Filter(Guild eg, TextChannel tc, Member em, String px, ArrayList<String> ag) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        eventPrefix = px;
        args = ag;

        checkPermissionsAndExecute(CommandType.FILTER, eventMember, eventChannel, lgr);
    }

    /**
     * Command form: th!filter <true/false> [channel(s)/category(ies)]
     */
    @Override
    public void execute() {
        if (args.isEmpty()) {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpFilter(eventPrefix));
            return;
        }

        int filtered = convertToBooleanInteger(args.get(0));
        String message;

        args.remove(0);
        StringBuilder nonValid,
                noText,
                complete;

        {
            List<?> results = parseChannelArgument(eventChannel, args);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            // Suppressing is okay because type for
            // results.get(3) is always ArrayList<String>
            //noinspection unchecked
            args = (ArrayList<String>) results.get(2);
        }
        // args now remains as a list of target channel(s).

        // individually enable filtering in every channel
        // after checking whether the channel exists in the db
        try {
            addIfNotInDb(eventGuild.getId(), args);
            complete = Create.setFilter(Integer.toString(filtered), args);
        } catch (SQLException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
            lgr.warn("(" + eventGuild.getName() + "/" + eventGuild.getId() + ") - " + ex.toString());
            return;
        }

        // switch message depending on user action
        if (filtered == 1) {
            message = "Enabled filtering on:";
        } else {
            message = "Disabled filtering on:";
        }

        Messages.sendMessage(eventChannel, DynamicEmbeds.dynamicEmbed(
                Arrays.asList(
                        message,
                        complete.toString(),
                        "Channels that were not valid or found:",
                        nonValid.toString(),
                        "Categories with no Text Channels:",
                        noText.toString()
                ),
                eventMember.getUser()
        ));
        lgr.info("Successfully executed on (" + eventGuild.getName() + "/" + eventGuild.getId() + ").");
    }
}
