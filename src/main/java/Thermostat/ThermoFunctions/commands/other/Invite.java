package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Sends an Invite embed when command is called.
 */
public class Invite implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Invite.class);

    private final TextChannel eventChannel;
    private final Member eventMember;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    public Invite(@Nonnull TextChannel tc, @Nonnull Member em) {
        eventChannel = tc;
        eventMember = em;

        checkPermissions();
        if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
            execute();
        } else {
            lgr.info("Missing permissions on (" + eventChannel.getGuild().getName() + "/" + eventChannel.getGuild().getId() + "):" +
                    " [" + missingThermostatPerms.toString() + "] [" + missingMemberPerms.toString() + "]");
            Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
        }
    }

    @Override
    public void checkPermissions() {
        eventChannel.getGuild()
                .retrieveMember(thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    missingThermostatPerms = findMissingPermissions(CommandType.INVITE.getThermoPerms(), thermostat.getPermissions());
                    return thermostat;
                })
                .queue();

        missingMemberPerms = findMissingPermissions(CommandType.INVITE.getMemberPerms(), eventMember.getPermissions());
    }

    /**
     * Command form: th!invite
     */
    @Override
    public void execute() {
        Messages.sendMessage(eventChannel, GenericEmbeds.inviteServer(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()));
        lgr.info("Successfully executed on (" + eventChannel.getGuild().getName() + "/" + eventChannel.getGuild().getId() + ").");
    }
}
