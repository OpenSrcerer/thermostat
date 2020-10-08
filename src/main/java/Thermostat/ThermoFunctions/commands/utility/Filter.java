package thermostat.thermoFunctions.commands.utility;

import net.dv8tion.jda.api.Permission;
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
import thermostat.thermostat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static thermostat.thermoFunctions.Functions.convertToBooleanString;


public class Filter implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(SetBounds.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventPrefix;
    private ArrayList<String> args;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    public Filter(Guild eg, TextChannel tc, Member em, String px, ArrayList<String> ag) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        eventPrefix = px;
        args = ag;

        checkPermissions();
        if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
            execute();
        } else {
            lgr.info("Missing permissions on (" + eventGuild.getName() + "/" + eventGuild.getId() + "):" +
                    " [" + missingThermostatPerms.toString() + "] [" + missingMemberPerms.toString() + "]");
            Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
        }
    }

    @Override
    public void checkPermissions() {
        eventGuild
                .retrieveMember(thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    missingThermostatPerms = findMissingPermissions(CommandType.FILTER.getThermoPerms(), thermostat.getPermissions());
                    return thermostat;
                })
                .queue();

        missingMemberPerms = findMissingPermissions(CommandType.FILTER.getMemberPerms(), eventMember.getPermissions());
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpFilter(eventPrefix));
            return;
        }

        String filtered = convertToBooleanString(args.get(0)), message;
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
        complete = Create.setFilter(filtered, args, eventChannel);

        // switch message depending on user action
        if (filtered.equals("1")) {
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
