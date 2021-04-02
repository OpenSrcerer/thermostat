package thermostat.util.enumeration;

/**
 * Used to better organize Embeds.
 */
public enum EmbedType {
    // ***************************************************************
    // **                           GENERAL                         **
    // ***************************************************************

    CHART_HOLDER ("chartHolder"),
    SAME_PREFIX ("samePrefix"),
    RESET_PREFIX ("resetPrefix"),
    NEW_PREFIX ("newPrefix"),
    GUIDE ("getGuide"),
    CHANNEL_SETTINGS ("channelSettings"),
    GET_VOTE ("getVote"),
    INVITE_SERVER ("inviteServer"),
    ACTION_SUCCESSFUL ("actionSuccessful"),
    PROMPT ("promptEmbed"),
    MISSED_PROMPT ("missedPrompt"),
    SET_CACHE ("setCache"),

    // ***************************************************************
    // **                            HELP                           **
    // ***************************************************************

    HELP_INFO ("helpInfo"),
    HELP_INVITE ("helpInvite"),
    HELP_VOTE ("helpVote"),
    HELP_CHART ("helpChart"),
    HELP_GETMONITOR ("helpGetMonitor"),
    HELP_SETTINGS ("helpSettings"),
    HELP_MONITOR ("helpMonitor"),
    HELP_SENSITIVITY ("helpSensitivity"),
    HELP_SETBOUNDS ("helpSetBounds"),
    HELP_SETCACHE ("helpCaching"),
    HELP_PREFIX ("helpPrefix"),
    HELP_FILTER ("helpFilter"),
    HELP_BAN ("helpBan"),
    HELP_KICK ("helpKick"),
    HELP_MUTE ("helpMute"),
    HELP_PURGE ("helpPurge"),
    MONITOR_INFO ("getMonitorInfo"),
    UTILITY_INFO ("getUtilityInfo"),
    OTHER_INFO ("getOtherInfo"),
    SELECTION ("getInfoSelection"),

    // ***************************************************************
    // **                            ERROR                          **
    // ***************************************************************

    ERR_PERMISSION ("errPermission"),
    ERR_PERMISSION_THERMO ("errPermission"),
    ERR_INPUT ("inputError"),
    ERR ("error"),
    ERR_FIX ("error"),

    // ***************************************************************
    // **                           SPECIAL                         **
    // ***************************************************************

    DYNAMIC ("dynamicEmbed");

    private final String functionName;

    EmbedType(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }
}
