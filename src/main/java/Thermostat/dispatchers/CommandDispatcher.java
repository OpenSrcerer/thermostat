package thermostat.dispatchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.thermoFunctions.commands.Command;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

public final class CommandDispatcher {

    private static final Logger lgr = LoggerFactory.getLogger(CommandDispatcher.class);

    private static final int nThreads = 4;
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(nThreads);
    private static final LinkedBlockingQueue<Command> commands = new LinkedBlockingQueue<>(nThreads);

    static {
        Runnable drainCommands = () -> {
            while (true) {
                Command command = null;

                // Pick up commands from the queue
                try {
                    // Wait until a command gets in the queue
                    command = commands.take();
                } catch (InterruptedException ex) {
                    lgr.error("Thread interrupted while waiting for request.", ex);
                }

                if (command == null) {
                    return;
                }

                command.run();
            }
        };

        for (int thread = 1; thread <= nThreads; ++thread) {
            executor.submit(drainCommands);
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
                    ErrorEmbeds.error("Please try again later.", ex.getCause().toString(), command.getId()),
                    ex
            );
        }
    }
}