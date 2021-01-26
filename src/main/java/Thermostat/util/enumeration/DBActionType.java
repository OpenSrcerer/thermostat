package thermostat.util.enumeration;

import javax.annotation.Nonnull;

/**
 * An enum to better organize how database actions are handled.
 */
public enum DBActionType {
    FILTER("FILTERED = ?"),
    MONITOR("MONITORED = ?"),
    UNFILTER("FILTERED = ?, WEBHOOK_ID = 0, WEBHOOK_TOKEN = 0");

    /**
     * A String that contains the Action that the enum represents.
     */
    public final String sqlAction;

    DBActionType(final @Nonnull String sqlAction) {
        this.sqlAction = sqlAction;
    }
}
