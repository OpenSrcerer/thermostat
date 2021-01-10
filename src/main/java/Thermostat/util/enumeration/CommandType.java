package thermostat.util.enumeration;

import net.dv8tion.jda.api.Permission;
import thermostat.commands.CommandTrigger;

import java.util.EnumSet;

/**
 * Used to identify commands on the Command listener.
 * Handy for organizing permissions for every command.
 * @see CommandTrigger
 */
public enum CommandType {

    // ***************************************************************
    // **                       INFORMATIONAL                       **
    // ***************************************************************

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

    // ***************************************************************
    // **                       MONITORING                          **
    // ***************************************************************

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

    // ***************************************************************
    // **                       MODERATION                          **
    // ***************************************************************

    BAN("ban", "bn",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.BAN_MEMBERS
            ),
            EnumSet.of(
                    Permission.BAN_MEMBERS
            )),
    KICK("kick", "kk",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MANAGE_CHANNEL
            ),
            EnumSet.of(
                    Permission.KICK_MEMBERS
            )),
    MUTE("mute", "mt",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MANAGE_ROLES
            ),
            EnumSet.of(
                    Permission.VOICE_MUTE_OTHERS,
                    Permission.KICK_MEMBERS
            )),
    PURGE("purge", "ex",
            EnumSet.of(
                    Permission.MESSAGE_READ,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_MANAGE
            ),
            EnumSet.of(
                    Permission.MESSAGE_MANAGE
            )),

    // ***************************************************************
    // **                         UTILITY                           **
    // ***************************************************************
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

    // ***************************************************************
    // **                       OTHER                               **
    // ***************************************************************
    INFO("info", "io",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ADD_REACTION
            )
    ),
    HELP("help", "hp",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_ADD_REACTION
            )
    ),
    INVITE("invite", "iv",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    PREFIX("prefix", "px",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            ),
            EnumSet.of(
                    Permission.ADMINISTRATOR
            )),
    VOTE("vote", "vo",
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),

    // ***************************************************************
    // **                   Internal Commands                       **
    // **     (Used as reference for Thermostat's Permissions)      **
    // ***************************************************************
    ADD_REACTIONS(
            EnumSet.of(
                    Permission.MESSAGE_READ,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_MANAGE,
                    Permission.MESSAGE_ADD_REACTION
            )
    ),
    SYNAPSE_MONITOR(
            EnumSet.of(
                    Permission.MESSAGE_HISTORY,
                    Permission.MANAGE_CHANNEL
            )
    ),
    WORDFILTEREVENT(
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_MANAGE,
                    Permission.MANAGE_WEBHOOKS
            )
    ),
    DELETE_REACTIONS(
            EnumSet.of(
                    Permission.MESSAGE_READ,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_MANAGE
            )
    ),
    DELETE_MESSAGE(
            EnumSet.of(
                    Permission.MESSAGE_READ,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_MANAGE
            )
    ),
    EDIT_MESSAGE(
            EnumSet.of(
                    Permission.MESSAGE_READ,
                    Permission.MESSAGE_HISTORY
            )
    ),
    SEND_MESSAGE_TEXT(
            EnumSet.of(
                    Permission.MESSAGE_WRITE
            )
    ),
    SEND_MESSAGE_EMBED(
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS
            )
    ),
    SEND_MESSAGE_ATTACHMENT(
            EnumSet.of(
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_ATTACH_FILES
            )
    );

    /**
     * Long and short alias to run the command.
     */
    private final String alias1, alias2;

    /**
     * Permissions that Thermostat needs to run the command.
     */
    private final EnumSet<Permission> thermoPermissions;

    /**
     * Permissions that the initiator needs to run the command.
     */
    private final EnumSet<Permission> memberPermissions;

    CommandType(String alias1, String alias2, EnumSet<Permission> thermoPermissions, EnumSet<Permission> memberPermissions) {
        this.alias1 = alias1;
        this.alias2 = alias2;
        this.thermoPermissions = thermoPermissions;
        this.memberPermissions = memberPermissions;
    }

    CommandType(String alias1, String alias2, EnumSet<Permission> thermoPermissions) {
        this.alias1 = alias1;
        this.alias2 = alias2;
        this.thermoPermissions = thermoPermissions;
        this.memberPermissions = EnumSet.noneOf(Permission.class);
    }

    CommandType(EnumSet<Permission> thermoPermissions) {
        this.alias1 = null;
        this.alias2 = null;
        this.thermoPermissions = thermoPermissions;
        this.memberPermissions = null;
    }

    /**
     * @return First alias of command.
     */
    public String getAlias1() {
        return alias1;
    }

    /**
     * @return Second alias of command.
     */
    public String getAlias2() {
        return alias2;
    }

    // Functions below must return clones, otherwise the originals will get modified by methods
    // that process these EnumSets due to the abstraction EnumSet provides..

    /**
     * @return A set of permissions required by Thermostat to run the command.
     */
    public EnumSet<Permission> getThermoPerms() { return thermoPermissions.clone(); }

    /**
     * @return A set of permissions a member must have to run the command.
     */
    public EnumSet<Permission> getMemberPerms() { return memberPermissions.clone(); }
}