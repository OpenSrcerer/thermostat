package thermostat.dispatchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.embeds.Embeds;
import thermostat.commands.Command;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.util.concurrent.LinkedBlockingQueue;

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
                            "Something went wrong on our end while handling your command.",
                            "Please try again."
                            ), ex
            );
        }
    }
}