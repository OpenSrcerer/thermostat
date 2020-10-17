package thermostat.thermoFunctions.entities;

import net.dv8tion.jda.api.Permission;
import thermostat.thermoFunctions.commands.CommandManager;

import java.util.EnumSet;

/**
 * Used to identifty commands on the Command listener.
 *
 * @see CommandManager
 */
public enum CommandType {

    // ---- Informational ----
    CHART(
            "chart", "ch",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_ATTACH_FILES
            ),
            EnumSet.of(
                    Permission.MANAGE_SERVER
            )
    ),
    GETMONITOR("getmonitor", "gm",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            ),
            EnumSet.of(
                    Permission.MANAGE_CHANNEL
            )),
    SETTINGS("settings", "st",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            ),
            EnumSet.of(
                    Permission.MANAGE_CHANNEL
            )),

    // ---- Monitoring ----
    MONITOR("monitor", "mn",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_HISTORY,
                    Permission.MANAGE_CHANNEL
            ),
            EnumSet.of(
                    Permission.MANAGE_CHANNEL
            )),
    SENSITIVITY("sensitivity", "ss",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MANAGE_CHANNEL
            ),
            EnumSet.of(
                    Permission.MANAGE_CHANNEL
            )),
    SETBOUNDS("setbounds", "sb",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MANAGE_CHANNEL
            ),
            EnumSet.of(
                    Permission.MANAGE_CHANNEL
            )),

    // ---- Utility ----
    FILTER("filter", "ft",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_HISTORY,
                    Permission.MANAGE_CHANNEL,
                    Permission.MANAGE_WEBHOOKS
            ),
            EnumSet.of(
                    Permission.MANAGE_CHANNEL,
                    Permission.MANAGE_SERVER
            )),

    // ---- Other ----
    INFO("info", "io",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ADD_REACTION
            ),
            EnumSet.noneOf(
                    Permission.class
            )),
    HELP("help", "hp",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_ADD_REACTION
            ),
            EnumSet.noneOf(
                    Permission.class
            )),
    INVITE("invite", "iv",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            ),
            EnumSet.noneOf(
                    Permission.class
            )),
    PREFIX("prefix", "px",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            ),
            EnumSet.of(
                    Permission.ADMINISTRATOR
            )),
    WORDFILTEREVENT("wordfilter", "wf",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_MANAGE,
                    Permission.MANAGE_WEBHOOKS
            ),
            EnumSet.noneOf(
                    Permission.class
            )),
    VOTE("vote", "vo",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            ),
            EnumSet.noneOf(
                    Permission.class
            ));

    private final String alias1, alias2;
    private final EnumSet<Permission> thermoPermissions, memberPermissions;

    CommandType(String alias1, String alias2, EnumSet<Permission> thermoPermissions, EnumSet<Permission> memberPermissions) {
        this.alias1 = alias1;
        this.alias2 = alias2;
        this.thermoPermissions = thermoPermissions;
        this.memberPermissions = memberPermissions;
    }

    public String getAlias1() {
        return alias1;
    }

    public String getAlias2() {
        return alias2;
    }

    // Functions below must return clones, otherwise the originals will get modified by methods
    // that process these EnumSets due to the abstraction EnumSet provides..

    public EnumSet<Permission> getThermoPerms() { return thermoPermissions.clone(); }

    public EnumSet<Permission> getMemberPerms() { return memberPermissions.clone(); }
}