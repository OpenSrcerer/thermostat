package thermostat.commands;

import org.slf4j.Logger;
import thermostat.util.entities.CommandContext;
import thermostat.util.enumeration.CommandType;

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
    CommandContext getData();

    /**
     * @return The type of the Command.
     */
    CommandType getType();
}
