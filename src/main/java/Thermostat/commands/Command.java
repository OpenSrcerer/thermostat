package thermostat.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import thermostat.embeds.ErrorEmbeds;
import thermostat.Messages;
import thermostat.Thermostat;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.util.EnumSet;

import static thermostat.util.PermissionComputer.getMissingPermissions;

public interface Command extends Runnable {

    /**
     * Execute the Command's Discord-side code.
     */
    void run();

    /**
     * Return the specific information
     * about this Command.
     * @return Event with specific case info.
     */
    GuildMessageReceivedEvent getEvent();

    /**
     * Get CommandType of Command.
     * @return type of Command.
     */
    CommandType getType();

    /**
     * Get specific Logger of Command.
     * @return Command's Logger
     */
    Logger getLogger();

    /**
     * Gets the specific Command ID of a Command.
     * @return Command's ID
     */
    long getId();

    /**
     * Checks if GuildMessageReceivedEvent is valid.
     * An event is valid when its Member object is not null.
     * @return true is event is valid, false otherwise.
     */
    default boolean validateEvent(GuildMessageReceivedEvent event) {
        if (event.getMember() == null)
            return false;

        return !event.getMember().getUser().isBot();
    }

    /**
     * Adds a given Command to the Request Manager queue if
     * permission conditions are met.
     * @param command Command to add to queue.
     */
    @SuppressWarnings("ConstantConditions")
    default void checkPermissionsAndQueue(@Nonnull Command command) {
        GuildMessageReceivedEvent commandEvent = command.getEvent();

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
                                Messages.sendMessage(commandEvent.getChannel(), ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms, command.getId()));
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
        GuildMessageReceivedEvent commandEvent = command.getEvent();

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
                                Messages.sendMessage(commandEvent.getChannel(), ErrorEmbeds.errPermission(missingThermostatPerms));
                            }
                        }
                );
    }
}
