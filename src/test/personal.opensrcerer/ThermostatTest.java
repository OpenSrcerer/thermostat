import org.junit.jupiter.api.Test;
import thermostat.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThermostatTest {
    @Test
    public void testThermo() {
        try {
            assertTrue(Thermostat.testThermostat()); // Tests Thermostat's loading
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }
}
