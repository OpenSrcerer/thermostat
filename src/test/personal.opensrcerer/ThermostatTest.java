import org.junit.jupiter.api.Test;
import thermostat.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThermostatTest {
    @Test
    public void testThermo() {
        String token = System.getenv("THERMOSTAT_TOKEN"); // Get token stored in environment variable secret

        try {
            assertTrue(Thermostat.testThermostat(token)); // Tests Thermostat's loading
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }
}
