package thermostat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a sole instance of Thermostat.
 */
public final class Run {

    /**
     * Logger for this class.
     */
    public static final Logger lgr = LoggerFactory.getLogger(Run.class);

    public static void main(String[] args) {
        try {
            Thermostat.initializeThermostat();
        } catch (Exception | Error ex) {
            lgr.error("Thermostat was unable to start due to this problem:", ex);
            Thermostat.shutdownThermostat();
        }
    }
}
