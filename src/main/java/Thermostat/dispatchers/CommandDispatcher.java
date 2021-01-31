package thermostat.dispatchers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Messages;
import thermostat.Thermostat;
import thermostat.commands.Command;
import thermostat.embeds.Embeds;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.LinkedBlockingQueue;

import static thermostat.util.PermissionComputer.getMissingPermissions;

/**
 * Organizes Command objects in a queue to be processed
 * by executor Threads.
 */
public final class CommandDispatcher {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(CommandDispatcher.class);

    /**
     * Queue to hold command objects waiting while the worker threads are
     * completing them.
     */
    private static final LinkedBlockingQueue<Command> commands = new LinkedBlockingQueue<>(100);

    static {
        Runnable drainCommands = () -> {
            while (true) {
                try {
                    if (Thread.interrupted()) {
                        return;
                    }

                    commands.take().run();
                } catch (Exception ex) {
                    lgr.error(Thread.currentThread().getName() + " encountered an exception:", ex);
                } catch (Error err) {
                    lgr.error("An Error was thrown. Shutting down Thermostat. Details:", err);
                    Thermostat.shutdownThermostat();
                }
            }
        };

        for (int thread = 1; thread <= 2; ++thread) {
            Thermostat.executor.submit(drainCommands);
        }
    }

    /**
     * Adds the command to the array of active commands.
     * @param command Command to be added.
     */
    public static void queueCommand(@Nonnull Command command) {
        try {
            commands.put(command);
        } catch (InterruptedException ex) {
            lgr.error("Request queue was interrupted!");
            ResponseDispatcher.commandFailed(
                    command,
                    Embeds.getEmbed(EmbedType.ERR_FIX, command.getData(),
                            Arrays.asList("Something went wrong on our end while handling your command.",
                                    "Please try again."
                            )
                    ), ex
            );
        }
    }

    /**
     * Adds a Command only related to Thermostat to the
     * Command Manager queue.
     * @param command Command to add to queue.
     */
    public static void checkThermoPermissionsAndQueue(@Nonnull final Command command) {
        GuildMessageReceivedEvent commandEvent = command.getData().event;

        commandEvent.getGuild()
                .retrieveMember(Thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get Thermostat's missing permissions, if applicable
                            EnumSet<Permission> missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(), command.getType().getThermoPerms());

                            if (missingThermostatPerms.isEmpty()) {
                                queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() + "/" + commandEvent.getGuild().getId() + "):" +
                                        " " + missingThermostatPerms.toString() + "");
                                Messages.sendMessage(commandEvent.getChannel(), Embeds.getEmbed(EmbedType.ERR_PERMISSION_THERMO, command.getData(), missingThermostatPerms));
                            }
                        }
                );
    }

    /**
     * Adds a given Command to the Request Manager queue if
     * permission conditions are met.
     * @param command Command to add to queue.
     */
    @SuppressWarnings("ConstantConditions")
    public static void checkPermissionsAndQueue(@Nonnull final Command command) {
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
                                queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() + "/" + commandEvent.getGuild().getId() + "):" +
                                        " " + missingThermostatPerms.toString() + " " + missingMemberPerms.toString() + "");
                                Messages.sendMessage(commandEvent.getChannel(), Embeds.getEmbed(EmbedType.ERR_PERMISSION, command.getData(), Arrays.asList(missingThermostatPerms, missingMemberPerms)));
                            }
                        }
                );
    }
}