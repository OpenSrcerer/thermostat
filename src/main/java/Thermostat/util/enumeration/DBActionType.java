package thermostat.util.enumeration;

import javax.annotation.Nonnull;

/**
 * An enum to better organize how database actions are handled.
 */
public enum DBActionType {
    FILTER("FILTERED = ?", "FILTERED"),
    MONITOR("MONITORED = ?", "MONITORED"),
    UNFILTER("FILTERED = ?, WEBHOOK_ID = 0, WEBHOOK_TOKEN = 0");

    /**
     * A String that contains an Action that the enum represents.
     */
    public final String sqlAction1;

    /**
     * A String that contains an Action that the enum represents.
     */
    public final String sqlAction2;

    DBActionType(@Nonnull final String sqlAction1) {
        this.sqlAction1 = sqlAction1;
        this.sqlAction2 = null;
    }

    DBActionType(@Nonnull final String sqlAction1, @Nonnull final String sqlAction2) {
        this.sqlAction1 = sqlAction1;
        this.sqlAction2 = sqlAction2;
    }
}
