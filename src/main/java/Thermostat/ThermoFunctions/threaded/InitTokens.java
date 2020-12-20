package thermostat.thermoFunctions.threaded;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;


/**
 * Initializes token variables for loading.
 * @see Thermostat
 */
public class InitTokens implements Callable<String[]> {

    private static final Logger lgr = LoggerFactory.getLogger(InitTokens.class);

    public InitTokens() {
        this.call();
    }

    @Override
    public String[] call() {
        String[] tokens = new String[3];

        try {
            InputStream configFile = Thermostat.class.getClassLoader().getResourceAsStream("config.json");

            if (configFile == null) { lgr.error("JSON config file not found."); return tokens; }

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(
                    new InputStreamReader(configFile, StandardCharsets.UTF_8
                    )
            );

            tokens[0] = jsonObject.get("Prefix").toString();
            tokens[1] = jsonObject.get("Token").toString();
            tokens[2] = jsonObject.get("DBLToken").toString();

        } catch (FileNotFoundException ex) {
            lgr.error("JSON config file not found.");
        } catch (ParseException ex) {
            lgr.error("Parsing error!", ex);
        } catch (IOException ex) {
            lgr.error("I/O Error while parsing JSON file.", ex);
        }

        return tokens;
    }
}
