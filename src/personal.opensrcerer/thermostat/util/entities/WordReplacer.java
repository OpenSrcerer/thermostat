package thermostat.util.entities;

import thermostat.mySQL.DataSource;

import java.sql.SQLException;
import java.util.*;

/**
 * Contains the RuleSet and methods for a Guild's filtering information.
 * There are two types of rules: Overrides, and regular replacement rules.
 * In regular replacement rules, any random word from the replacableWords set is replaced
 * by a random word in the replacementWords set.
 *
 * In overrides, the key of the map is replaced with the value.
 */
public class WordReplacer {
    /**
     * Used to store word replacement overrides.
     */
    private final Map<String, String> overridenReplacements;

    /**
     * Set of words that will be replaced.
     */
    private final Set<String> replacableWords;

    /**
     * Set of words that will replace others.
     */
    private final Set<String> replacementWords;

    public WordReplacer() throws SQLException {
        overridenReplacements = new TreeMap<>();
        replacableWords = new HashSet<>();
        replacementWords = new HashSet<>();

        DataSource.demand(conn -> {
            overridenReplacements.put("", "");
            return null;
        });
    }
}
