package thermostat.thermoFunctions.commands;

import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nonnull;

/**
 * Packets to be put in a queue and drained by worker threads.
 */
public class Request implements Runnable {

    private boolean successful;
    private final Command command;
    public final CommandType type;

    protected Request(CommandType commandType, Command command) {
        this.command = command;
        this.type = commandType;
    }

    @Override
    public void run() {
        try {
            command.run();
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
