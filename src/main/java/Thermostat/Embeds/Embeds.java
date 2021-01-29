package thermostat.embeds;

import net.dv8tion.jda.api.Permission;
import okhttp3.internal.annotations.EverythingIsNonNull;
import thermostat.util.Constants;
import thermostat.util.entities.CommandData;
import thermostat.util.entities.SettingsData;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Static builder methods for all Thermostat Embeds.
 */
public final class Embeds {
    @EverythingIsNonNull
    public static ThermoEmbed getEmbed(final EmbedType type, final CommandData data) {
        ThermoEmbed embed = new ThermoEmbed(data);
        return matchTypeToOptions(type, embed, data, null);
    }

    @EverythingIsNonNull
    public static ThermoEmbed getEmbed(final EmbedType type, Object... options) {
        ThermoEmbed embed = new ThermoEmbed();
        return matchTypeToOptions(type, embed, null, options);
    }

    @EverythingIsNonNull
    public static ThermoEmbed getEmbed(final EmbedType type, final CommandData data, final Object... options) {
        ThermoEmbed embed = new ThermoEmbed(data);
        return matchTypeToOptions(type, embed, data, options);
    }

    @SuppressWarnings("unchecked")
    private static ThermoEmbed matchTypeToOptions(@Nonnull final EmbedType type, @Nonnull final ThermoEmbed embed,
                                                 final CommandData data, @Nullable final Object options) {
        // Wtf do i use to show that an embed is an error embed
        if (options == null) {
            return switch (type) {
                case SAME_PREFIX ->             samePrefix(embed, data.prefix);
                case RESET_PREFIX ->            resetPrefix(embed);
                case NEW_PREFIX ->              setPrefix(embed, data.prefix);
                case GET_VOTE ->                getVote(embed);
                case INVITE_SERVER ->           inviteServer(embed);
                case MISSED_PROMPT ->           missedPrompt(embed);
                case PROMPT ->                  promptEmbed(embed);
                case HELP_INFO ->               helpInfo(embed, data.prefix);
                case HELP_INVITE ->             helpInvite(embed, data.prefix);
                case HELP_VOTE ->               helpVote(embed, data.prefix);
                case HELP_CHART ->              helpChart(embed, data.prefix);
                case HELP_GETMONITOR ->         helpGetMonitor(embed, data.prefix);
                case HELP_SETTINGS ->           helpSettings(embed, data.prefix);
                case HELP_MONITOR ->            helpMonitor(embed, data.prefix);
                case HELP_SENSITIVITY ->        helpSensitivity(embed, data.prefix);
                case HELP_SETBOUNDS ->          helpSetBounds(embed, data.prefix);
                case HELP_PREFIX ->             helpPrefix(embed, data.prefix);
                case HELP_FILTER ->             helpFilter(embed, data.prefix);
                case MONITOR_INFO ->            getMonitorInfo(embed, data.prefix);
                case UTILITY_INFO ->            getUtilityInfo(embed, data.prefix);
                case OTHER_INFO ->              getOtherInfo(embed, data.prefix);
                case SELECTION ->               getInfoSelection(embed);
                default ->                      embed;
            };
        } else {
            return switch (type) {
                case CHART_HOLDER ->            chartHolder(embed, (String) options);
                case GET_PREFIX ->              getPrefix(embed, data.prefix, (String) options);
                case CHANNEL_SETTINGS ->        channelSettings(embed, (SettingsData) options);
                case ALL_REMOVED ->             allRemoved(embed, (String) options);
                case ERR_PERMISSION ->          errPermission(embed, (Set<Permission>[]) options);
                case ERR_PERMISSION_THERMO ->   errPermission(embed, (Set<Permission>) options);
                case ERR_INPUT ->               inputError(embed, (String) options);
                case ERR_FIX ->                 error(embed, (String[]) options);
                case ERR ->                     error(embed, (String) options);
                case DYNAMIC ->                 dynamicEmbed(embed, (List<String>) options);
                default ->                      embed;
            };
        }
    }

    // ***************************************************************
    // **                           GENERAL                         **
    // ***************************************************************

    private static ThermoEmbed chartHolder(final ThermoEmbed embed, final String serverName) {
        embed.setTitle("Slowmode Frequency Chart");
        embed.setDescription(serverName);
        return embed;
    }

    private static ThermoEmbed samePrefix(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("My prefix is already `" + prefix + "` !");
        return embed;
    }

    private static ThermoEmbed resetPrefix(final ThermoEmbed embed) {
        embed.setTitle("Thermostat's prefix has been reset to `" + Constants.DEFAULT_PREFIX + "` .");
        return embed;
    }

    private static ThermoEmbed setPrefix(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Thermostat will now reply to: " + "`" + prefix + "` .");
        return embed;
    }

    private static ThermoEmbed getPrefix(final ThermoEmbed embed, final String prefix, final String guildName) {
        embed.setTitle("❓ Are you confused? Need help? Read our handy dandy wiki.",
                "https://github.com/OpenSrcerer/thermostat/wiki"
        );
        embed.addField("🎯 My prefix in " + guildName + " is: " + "`" + prefix + "`",
                "Reset it to the default using <@" + Constants.THERMOSTAT_USER_ID + "> `--reset` if you need to!", false
        );
        embed.addField("💖 Like the bot? Please give it an upvote!",
                "Thank you for your support! Vote for Thermostat with `" + prefix + "vote`", false
        );
        return embed;
    }

    private static ThermoEmbed channelSettings(final ThermoEmbed embed, final SettingsData data) {
        embed.setTitle("Settings for #" + data.channelName + ":");
        if (data.min == 0) {
            embed.addField("Min Slowmode:", "**-**", true);
        } else {
            embed.addField("Min Slowmode:", "**" + data.min + "**", true);
        }

        if (data.max == 0) {
            embed.addField("Max Slowmode:", "**-**", true);
        } else {
            embed.addField("Max Slowmode:", "**" + data.max + "**", true);
        }

        if (data.monitor) {
            embed.addField("Monitored:", "**Yes**", false);
        } else {
            embed.addField("Monitored:", "**No**", false);
        }

        if (data.filter) {
            embed.addField("Filtered:", "**Yes**", true);
        } else {
            embed.addField("Filtered:", "**No**", true);
        }

        String indicator = "`   ";

        for (float index = 0.5f; index <= data.sensitivity; index += 0.05f) {
            indicator = indicator.concat(" ");
        }

        indicator = indicator.concat("^ (" + String.format("%.1f", (data.sensitivity - 1f) * 20f) + ")`");
        embed.addField("Sensitivity:\n `-10 -------------------- 10`\n " + indicator, "", false);

        return embed;
    }

    private static ThermoEmbed getVote(final ThermoEmbed embed) {
        embed.setTitle("🎉 Thank you for your continuous support! 🎉");
        embed.setDescription(
                "[@top.gg](https://top.gg/bot/700341788136833065/vote)\n" +
                "[@discord.boats](https://discord.boats/bot/700341788136833065/vote)"
        );
        return embed;
    }

    private static ThermoEmbed inviteServer(final ThermoEmbed embed) {
        embed.setTitle("Useful Hyperlinks 🔧");
        embed.setDescription("[Join Support Server](https://discord.gg/FnPb4nM)\n[Get Thermostat for your own server](https://top.gg/bot/700341788136833065)");
        return embed;
    }

    private static ThermoEmbed allRemoved(final ThermoEmbed embed, final String action) {
        embed.setTitle("All channels are no longer being " + action + ".");
        return embed;
    }

    private static ThermoEmbed missedPrompt(final ThermoEmbed embed) {
        embed.setTitle("Did not react to prompt in time. Operation cancelled.");
        return embed;
    }

    private static ThermoEmbed promptEmbed(final ThermoEmbed embed) {
        embed.setTitle("Are you sure you want to perform this action? Click the reaction below if you're sure you want to continue.");
        return embed;
    }

    // ***************************************************************
    // **                            HELP                           **
    // ***************************************************************

    private static ThermoEmbed helpInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n `" + prefix + "info [command]`");
        embed.setDescription("**Aliases: " + prefix + "help/hp/io** ⇨ Brings up an interactive help menu, or specific details about a command if you need to know more." +
                " Arguments surrounded by <> are mandatory, and ones surrounded by [] are optional.");
        return embed;
    }

    private static ThermoEmbed helpInvite(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n `" + prefix + "chart <charttype> [channel(s)/category(ies)]`");
        embed.setDescription("**Alias: " + prefix + "iv** ⇨ Provides an invite link to Thermostat's support server, and the top.gg website.");
        return embed;
    }

    private static ThermoEmbed helpVote(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n `" + prefix + "chart <charttype> [channel(s)/category(ies)]`");
        embed.setDescription("**Alias: " + prefix + "vo** ⇨ Provides links to voting websites where you can vote for Thermostat. Thank you for your support!");
        return embed;
    }

    private static ThermoEmbed helpChart(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n `" + prefix + "chart <charttype> [channel(s)/category(ies)]`");
        embed.setDescription("**Alias: " + prefix + "ch** ⇨ Command that gives informational data about Thermostat's operation in chart form.");
        embed.addField("Chart Types (Name - CmdName): ", "★ Slowmode Frequency - (slowfreq)", false);
        return embed;
    }

    private static ThermoEmbed helpGetMonitor(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n `" + prefix + "getmonitor`");
        embed.setDescription("**Alias: " + prefix + "gm** ⇨ Shows which channels are currently being monitored or filtered in your server.");
        return embed;
    }

    private static ThermoEmbed helpSettings(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n `" + prefix + "settings [channel]`");
        embed.setDescription("**Alias: " + prefix + "st** ⇨ Shows details about the configuration of the given channel, " +
                "such as if it's currently being monitored or filtered, the bounds you have provided, etc.");
        return embed;
    }

    private static ThermoEmbed helpMonitor(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n `" + prefix + "monitor <on/off> [channel(s)/category(ies)]`");
        embed.setDescription("**Alias: " + prefix + "mn** ⇨ Adds/Removes text channels to the slowmode monitoring database. " +
                "When a channel is being monitored, slowmode will be automatically adjusted by Thermostat depending " +
                "on the amount of messages currently coming in.");
        return embed;
    }

    private static ThermoEmbed helpSensitivity(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n ```" + prefix + "sensitivity <sensitivity> [channel(s)/category(ies)]```");
        embed.setDescription("**Alias: " + prefix + "ss** ⇨ Sets the sensitivity level for the channel. " +
                "**Requires a value between -10 and 10, you may use decimal numbers.** " +
                "The higher the sensitivity, the easier for Thermostat to initiate slowmode.");
        return embed;
    }

    private static ThermoEmbed helpSetBounds(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n ```" + prefix + "setbounds <min/max> <slowmode> [channel(s)/category(ies)]```");
        embed.setDescription("**Alias: " + prefix + "sb** ⇨ Sets the upper and lower bounds for the slowmode of the channel. " +
                "This means that when Thermostat adjusts this channel's slowmode, the slowmode will be kept within the " +
                "minimum/maximum limits you have provided.");
        return embed;
    }

    private static ThermoEmbed helpPrefix(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n ```" + prefix + "prefix <newprefix/reset>```");
        embed.setDescription("**Alias: " + prefix + "px** ⇨ Manages Thermostat's prefix. You can change it to pretty much anything. " +
                "Note: You can call this command by using @Thermostat instead of the prefix.");
        return embed;
    }

    private static ThermoEmbed helpFilter(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Command Usage:\n ```" + prefix + "filter <on/off> [channel(s)/category(ies)]```");
        embed.setDescription("**Alias: " + prefix + "fi** ⇨ [WIP] Enables/Disables curse-word filtering for a channel.");
        return embed;
    }

    private static ThermoEmbed getMonitorInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("🌡┇Monitoring Commands");
        embed.addField("⬆ Menu", "Go back to the Main Menu", false);
        embed.addField(prefix + "monitor", "Syntax: `" + prefix + "monitor <on/off> [channel(s)/category(ies)].`", false);
        embed.addField(prefix + "getmonitor", "Syntax: `" + prefix + "getmonitor.`", false);
        embed.addField(prefix + "setbounds", "Syntax: `" + prefix + "setbounds <min/max> <slowmode> [channel(s)/category(ies)].`", false);
        embed.addField(prefix + "settings", "Syntax: `" + prefix + "settings[channel].`", false);
        embed.addField(prefix + "sensitivity", "Syntax: `" + prefix + "sensitivity <sensitivity> [channel(s)].`", false);
        embed.addField("❌ Exit", "Exit the info menu.", false);
        return embed;
    }

    private static ThermoEmbed getUtilityInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("🔧┇Utility Commands");
        embed.addField("⬆ Menu", "Go back to the Main Menu", false);
        embed.addField(prefix + "filter", "Syntax: `" + prefix + "filter <charttype>.`", false);
        embed.addField("❌ Exit", "Exit the info menu.", false);
        return embed;
    }

    private static ThermoEmbed getOtherInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("ℹ┇Other Commands");
        embed.addField("⬆ Menu", "Go back to the Main Menu", false);
        embed.addField(prefix + "info", "`Syntax:`" + prefix + "info.`", false);
        embed.addField("@Thermostat prefix", "Syntax: <@!" + Constants.THERMOSTAT_USER_ID + "> `prefix.`", false);
        embed.addField(prefix + "chart", "Syntax: `" + prefix + "chart <charttype>.`", false);
        embed.addField(prefix + "vote", "Syntax: `" + prefix + "vote.`", false);
        embed.addField(prefix + "invite", "Syntax: `" + prefix + "invite.`", false);
        embed.addField("❌ Exit", "Exit the info menu.", false);
        return embed;
    }

    private static ThermoEmbed getInfoSelection(final ThermoEmbed embed) {
        embed.setTitle("Menu");
        embed.addField("🌡 Monitoring", "Commands to help you manage slowmode in channels.", false);
        embed.addField("🔧 Utility", "Useful features to help you moderate your server.", false);
        embed.addField("ℹ Other", "Informational commands that provide other functionalities.", false);
        embed.addField("❌ Exit", "Exit the info menu.", false);
        return embed;
    }

    // ***************************************************************
    // **                            ERROR                          **
    // ***************************************************************

    private static ThermoEmbed errPermission(final ThermoEmbed embed, final Set<Permission>[] permissions) {
        embed.setTitle("❌ Error encountered! Details:");

        if (!permissions[0].isEmpty()) {
            StringBuilder missingPerms = new StringBuilder();
            permissions[0].forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
            embed.addField("Thermostat lacks these permissions:", missingPerms.toString(), false);
        }
        if (!permissions[1].isEmpty()) {
            StringBuilder missingPerms = new StringBuilder();
            permissions[1].forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
            embed.addField("You lack these permissions:", missingPerms.toString(), false);
        }
        return embed;
    }

    private static ThermoEmbed errPermission(final ThermoEmbed embed, final Set<Permission> thermoPermissions) {
        embed.setTitle("❌ Error encountered! Details:");

        StringBuilder missingPerms = new StringBuilder();
        thermoPermissions.forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
        embed.addField("Thermostat lacks these permissions:", missingPerms.toString(), false);
        return embed;
    }

    private static ThermoEmbed inputError(final ThermoEmbed embed, final String error) {
        embed.setTitle("You have an error in your input:");
        embed.addField("`" + error + "`", "", false);
        return embed;
    }

    private static ThermoEmbed error(final ThermoEmbed embed, final String[] error) {
        embed.setTitle("❌ An error has occurred. ❌");
        embed.addField("Error details:", error[0], false);
        embed.addField("Suggested fix: ", error[1], false);
        embed.addField("Support server: https://discord.gg/FnPb4nM", "", false);
        return embed;
    }

    private static ThermoEmbed error(final ThermoEmbed embed, final String error) {
        embed.setTitle("❌ An error has occurred. ❌");
        embed.addField("Error details:", error, false);
        embed.addField("Support server: https://discord.gg/FnPb4nM", "", false);
        return embed;
    }

    // ***************************************************************
    // **                           SPECIAL                         **
    // ***************************************************************

    private static ThermoEmbed dynamicEmbed(final ThermoEmbed embed, final List<String> options) {
        embed.setColor(0x00aeff);

        // Elements are grouped two by two:
        // index = Description; index + 1 = dynamic slowmode value
        for (int index = 0; index < options.size(); index += 2) {
            if (!options.get(index + 1).isEmpty()) {
                embed.addField(options.get(index), options.get(index + 1), false);
            }
        }

        return embed;
    }
}
