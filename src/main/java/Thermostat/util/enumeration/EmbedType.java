package thermostat.util.enumeration;

/**
 * Used to better organize Embeds.
 */
public enum EmbedType {
    // ***************************************************************
    // **                           GENERAL                         **
    // ***************************************************************

    CHART_HOLDER,
    SAME_PREFIX,
    RESET_PREFIX,
    NEW_PREFIX,
    GET_PREFIX,
    CHANNEL_SETTINGS,
    GET_VOTE,
    INVITE_SERVER,
    ALL_REMOVED,
    PROMPT,
    MISSED_PROMPT,

    // ***************************************************************
    // **                            HELP                           **
    // ***************************************************************

    HELP_INFO,
    HELP_INVITE,
    HELP_VOTE,
    HELP_CHART,
    HELP_GETMONITOR,
    HELP_SETTINGS,
    HELP_MONITOR,
    HELP_SENSITIVITY,
    HELP_SETBOUNDS,
    HELP_PREFIX,
    HELP_FILTER,
    MONITOR_INFO,
    UTILITY_INFO,
    OTHER_INFO,
    SELECTION,

    // ***************************************************************
    // **                            ERROR                          **
    // ***************************************************************

    ERR_PERMISSION,
    ERR_PERMISSION_THERMO,
    ERR_INPUT,
    ERR,
    ERR_FIX,

    // ***************************************************************
    // **                           SPECIAL                         **
    // ***************************************************************

    DYNAMIC;

    // --- Constructor ---
    /*
    public final String title;
    public final String description;

    EmbedType() {
        this.title = null;
        this.description = null;
    }

    EmbedType(final String title, final String description) {
        this.title = title;
        this.description = title;
    }*/
}