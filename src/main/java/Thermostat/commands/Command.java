package thermostat.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import thermostat.Messages;
import thermostat.Thermostat;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;

import static thermostat.util.PermissionComputer.getMissingPermissions;

public interface Command extends Runnable {

    /**
     * Execute the Command's Discord-side code.
     */
    void run();

    /**
     * @return The Logger for the specific Command.
     */
    Logger getLogger();

    /**
     * Get the Command's data package.
     * @return The Command's information. (Event, Prefix, etc.)
     */
    CommandData getData();

    /**
     * @return The type of the Command.
     */
    CommandType getType();

    /**
     * Adds a given Command to the Request Manager queue if
     * permission conditions are met.
     * @param command Command to add to queue.
     */
    @SuppressWarnings("ConstantConditions")
    default void checkPermissionsAndQueue(@Nonnull Command command) {
        GuildMessageReceivedEvent commandEvent = command.getData().event;

        // check main permissions
        commandEvent.getGuild()
                .retrieveMember(Thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get member and thermostat's missing permissions, if applicable
                            EnumSet<Permission>
                                    missingMemberPerms = getMissingPermissions(commandEvent.getMember(), commandEvent.getChannel(), command.getType().getMemberPerms()),
                                    missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(), command.getType().getThermoPerms());

                            if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
                                CommandDispatcher.queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() + "/" + commandEvent.getGuild().getId() + "):" +
                                        " " + missingThermostatPerms.toString() + " " + missingMemberPerms.toString() + "");
                                Messages.sendMessage(commandEvent.getChannel(), Embeds.getEmbed(EmbedType.ERR_PERMISSION, command.getData(), Arrays.asList(missingThermostatPerms, missingMemberPerms)));
                            }
                        }
                );
    }

    /**
     * Adds a Command only related to Thermostat to the
     * Command Manager queue.
     * @param command Command to add to queue.
     */
    default void checkThermoPermissionsAndQueue(@Nonnull Command command) {
        GuildMessageReceivedEvent commandEvent = command.getData().event;

        commandEvent.getGuild()
                .retrieveMember(Thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get Thermostat's missing permissions, if applicable
                            EnumSet<Permission> missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(), command.getType().getThermoPerms());

                            if (missingThermostatPerms.isEmpty()) {
                                CommandDispatcher.queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() + "/" + commandEvent.getGuild().getId() + "):" +
                                        " " + missingThermostatPerms.toString() + "");
                                Messages.sendMessage(commandEvent.getChannel(), Embeds.getEmbed(EmbedType.ERR_PERMISSION_THERMO, command.getData(), missingThermostatPerms));
                            }
                        }
                );
    }
}
