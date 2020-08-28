package thermostat.thermoFunctions.entities;

import thermostat.thermoFunctions.commands.Command;

/**
 * Used to identifty commands on the Command listener.
 *
 * @see Command
 */
public enum CommandType {

    // ---- Informational ----
    CHART("chart", "ch"),
    GETMONITORLIST("getmonitor", "gm"),
    SETTINGS("settings", "st"),

    // ---- Monitoring ----
    MONITOR("monitor", "mn"),
    SENSITIVITY("sensitivity", "ss"),
    SETMAXIMUM("setmaximum", "sx"),
    SETMINIMUM("setminimum", "sm"),
    UNMONITOR("unmonitor", "um"),
    UNMONITORALL("unmonitorall", "ua"),

    // ---- Other ----
    INFO("info", "io"),
    HELP("help", "hp"),
    INVITE("invite", "iv"),
    PREFIX("prefix", "px"),
    VOTE("vote", "vo");

    private final String alias1, alias2;

    CommandType(String alias1, String alias2) {
        this.alias1 = alias1;
        this.alias2 = alias2;
    }

    public String getAlias1() {
        return alias1;
    }

    public String getAlias2() {
        return alias2;
    }
}