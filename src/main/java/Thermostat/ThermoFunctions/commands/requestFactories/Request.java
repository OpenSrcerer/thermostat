package thermostat.thermoFunctions.commands.requestFactories;

import thermostat.thermoFunctions.entities.RequestType;

import javax.annotation.Nonnull;

/**
 * Packets to be put in a queue and drained by worker threads.
 * The callable is executed regardless of type.
 */
public class Request implements Runnable {

    private boolean successful;
    private final Command command;
    public final RequestType type;

    protected Request(RequestType requestType, Command command) {
        this.command = command;
        this.type = requestType;
    }

    @Override
    public void run() {
        try {
            command.execute();
            successful = true;
        } catch (Exception ex) {
            successful = false;
        }
    }

    @Nonnull
    public Command getCommand() {
        return command;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
