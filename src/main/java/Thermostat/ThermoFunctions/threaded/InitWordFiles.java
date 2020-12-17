package thermostat.thermoFunctions.threaded;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.thermoFunctions.commands.utility.WordFilterCommand;
import thermostat.thermostat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;


/**
 * Initializes word arrays for WordFilterEvent.
 * @see WordFilterCommand
 */
public class InitWordFiles implements Callable<Boolean> {
    private final String File1;
    private final String File2;

    private static final Logger lgr = LoggerFactory.getLogger(InitWordFiles.class);

    public InitWordFiles(String File1, String File2) {
        this.File1 = File1;
        this.File2 = File2;
        this.call();
    }

    @Override
    public Boolean call() {
        boolean complete = false;

        ArrayList<String>
                niceWords = new ArrayList<>(),
                badWords = new ArrayList<>();

        // reads from files
        try (
                InputStream niceFile = thermostat.class.getClassLoader().getResourceAsStream(File1);
                InputStream badFile = thermostat.class.getClassLoader().getResourceAsStream(File2)
        ) {
            lgr.debug("Loaded word files " + File1 + " and " + File2);

            if (niceFile != null && badFile != null) {
                Scanner niceScanner = new Scanner(niceFile);
                Scanner badScanner = new Scanner(badFile);
                niceScanner.useDelimiter(",+");
                badScanner.useDelimiter(",+");

                while (niceScanner.hasNext()) {
                    niceWords.add(niceScanner.next());
                }
                while (badScanner.hasNext()) {
                    badWords.add(badScanner.next());
                }
                complete = true;

            } else {
                return false;
            }

        } catch (IOException ex) {
            lgr.error("Issue while loading files!", ex);
        }

        WordFilterCommand.setWordArrays(niceWords, badWords);
        return complete;
    }
}
