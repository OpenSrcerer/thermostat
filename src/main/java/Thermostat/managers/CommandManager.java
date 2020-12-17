package thermostat.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.thermoFunctions.commands.Command;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public final class CommandManager {

    private static final Logger lgr = LoggerFactory.getLogger(CommandManager.class);

    private static final int nThreads = 4;
    private static final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    private static final LinkedBlockingQueue<Command> commands = new LinkedBlockingQueue<>(nThreads);

    static {
        Runnable runRequests = () -> {
            while (true) {
                Command command = null;

                try {
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
            executor.submit(runRequests);
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
            ResponseManager.commandFailed();
        }
    }
}