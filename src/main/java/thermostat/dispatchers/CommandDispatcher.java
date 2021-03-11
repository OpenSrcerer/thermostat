package thermostat.dispatchers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.commands.Command;
import thermostat.embeds.Embeds;
import thermostat.util.Constants;
import thermostat.util.RestActions;
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
                if (Thread.interrupted()) {
                    return;
                }

                Command commandToProcess = null;
                try {
                    commandToProcess = commands.take(); // Take a command from the request queue
                    commandToProcess.run(); // Run the command
                } catch (RuntimeException ex) {
                    if (commandToProcess != null) {
                        // Usually permission errors.
                        ResponseDispatcher.commandFailed(commandToProcess,
                                Embeds.getEmbed(EmbedType.ERR, commandToProcess.getData(), ex.getMessage())
                        );
                    }
                    lgr.error(Thread.currentThread().getName() + " encountered a runtime exception:", ex);
                } catch (Exception ex) {
                    // Other exceptions
                    lgr.error(Thread.currentThread().getName() + " encountered an exception:", ex);
                } catch (Error err) {
                    // Fatal Error, terminate program
                    lgr.error("A fatal error was thrown. Shutting down Thermostat. Details:", err);
                    Thermostat.shutdownThermostat();
                }
            }
        };

        for (int thread = 1; thread <= Constants.AVAILABLE_CORES; ++thread) {
            Thermostat.NON_SCHEDULED_EXECUTOR.submit(drainCommands);
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
     * Adds a Command to the Command queue if Thermostat has the right permissions.
     * @param command Command to add to queue.
     */
    public static void checkThermoPermissionsAndQueue(@Nonnull final Command command) {
        final GuildMessageReceivedEvent commandEvent = command.getData().event;

        commandEvent.getGuild().retrieveMember(Thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    EnumSet<Permission> missingThermostatPerms = getMissingPermissions(thermostat,
                            commandEvent.getChannel(), command.getType().getThermoPerms());

                    if (missingThermostatPerms.isEmpty()) {
                        queueCommand(command);
                    } else {
                        command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() +
                                "/" + commandEvent.getGuild().getId() + "):" +
                                " " + missingThermostatPerms.toString() + "");
                        RestActions.sendMessage(commandEvent.getChannel(),
                                Embeds.getEmbed(EmbedType.ERR_PERMISSION_THERMO,
                                        command.getData(), missingThermostatPerms)).queue();
                    }
                    return thermostat;
                }).queue();
    }

    /**
     * Adds a given Command to the Request Manager queue if the member who initiated the command and Thermostat
     * have the right permissions.
     * @param command Command to add to queue.
     */
    @SuppressWarnings("ConstantConditions")
    public static void checkPermissionsAndQueue(@Nonnull final Command command) {
        GuildMessageReceivedEvent commandEvent = command.getData().event;

        commandEvent.getGuild().retrieveMember(Thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    // Get member and thermostat's missing permissions, if applicable
                    EnumSet<Permission>
                            missingMemberPerms = getMissingPermissions(commandEvent.getMember(),
                            commandEvent.getChannel(), command.getType().getMemberPerms()),
                            missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(),
                                    command.getType().getThermoPerms());

                    if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
                        queueCommand(command);
                    } else {
                        command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() +
                                "/" + commandEvent.getGuild().getId() + "):" +
                                " " + missingThermostatPerms.toString() + " " + missingMemberPerms.toString() + "");
                        RestActions.sendMessage(commandEvent.getChannel(), Embeds.getEmbed(EmbedType.ERR_PERMISSION,
                                command.getData(), Arrays.asList(missingThermostatPerms, missingMemberPerms))).queue();
                    }
                    return thermostat;
                }).queue();
    }
}