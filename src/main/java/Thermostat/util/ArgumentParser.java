package thermostat.util;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentParser {
    /**
     * Checks if a list is not null and contains data.
     * @param list List to check.
     * @return True if the list does contain data, false otherwise.
     */
    public static boolean hasArguments(List<String> list) {
        if (list == null) {
            return false;
        }

        return !list.isEmpty();
    }

    @Nonnull
    public static Map<String, List<String>> parseArguments(List<String> arguments) throws ParseException, IllegalArgumentException {
        // Create a HashMap where the final parameters will be stored.
        final Map<String, List<String>> params = new HashMap<>();

        // create a temporary list for the options
        List<String> options = null;

        for (final String arg : arguments) {
            if (arg.charAt(0) == '-') {
                if (arg.length() < 2) {
                    throw new ParseException("Error at argument " + arg + ".", 1);
                }

                options = new ArrayList<>();
                params.put(arg.substring(1).toLowerCase(), options);
            } else if (options != null) {
                options.add(arg);
            } else {
                // throw new IllegalArgumentException("Illegal argument usage: " + arg + ".");
            }
        }

        return params;
    }
}
