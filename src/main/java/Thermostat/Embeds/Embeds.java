package thermostat.embeds;

import net.dv8tion.jda.api.Permission;
import okhttp3.internal.annotations.EverythingIsNonNull;
import thermostat.Thermostat;
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
    public static ThermoEmbed getEmbed(final EmbedType type) {
        return matchTypeToOptions(type, new ThermoEmbed(), null, null);
    }

    @EverythingIsNonNull
    public static ThermoEmbed getEmbed(final EmbedType type, final Object options) {
        return matchTypeToOptions(type, new ThermoEmbed(), null, options);
    }

    @EverythingIsNonNull
    public static ThermoEmbed getEmbed(final EmbedType type, final CommandData data) {
        return matchTypeToOptions(type, new ThermoEmbed(data), data, null);
    }

    @EverythingIsNonNull
    public static ThermoEmbed getEmbed(final EmbedType type, final CommandData data, final Object options) {
        return matchTypeToOptions(type, new ThermoEmbed(data), data, options);
    }

    /**
     * Given the EmbedType provided, return the ThermoEmbed that matches the type.
     * @param type Type of embed.
     * @param embed Prepared ThermoEmbed with default parameters.
     * @param data Data of the Command to send an Embed back to.
     * @param options Extra options for the embed.
     * @return A ThermoEmbed that matches the given parameters.
     */
    @SuppressWarnings("unchecked")
    private static ThermoEmbed matchTypeToOptions(@Nonnull final EmbedType type, @Nonnull final ThermoEmbed embed,
                                                 final CommandData data, @Nullable final Object options) throws RuntimeException
    {
        if (options == null) {
            return switch (type) {
                case SAME_PREFIX ->             samePrefix(embed, data.prefix);
                case RESET_PREFIX ->            resetPrefix(embed);
                case GUIDE ->                   getGuide(embed, data.prefix, data.event.getGuild().getName());
                case GET_VOTE ->                getVote(embed);
                case INVITE_SERVER ->           inviteServer(embed);
                case MISSED_PROMPT ->           missedPrompt(embed);
                case PROMPT ->                  promptEmbed(embed);
                case ACTION_SUCCESSFUL ->       actionSuccessful(embed);
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
                case HELP_SETCACHE ->           helpCaching(embed, data.prefix);
                case SELECTION ->               getInfoSelection(embed);
                default ->                      throw new IllegalArgumentException("Invalid embed type.");
            };
        } else {
            return switch (type) {
                case MONITOR_INFO ->            getMonitorInfo(embed, (String) options);
                case UTILITY_INFO ->            getUtilityInfo(embed, (String) options);
                case OTHER_INFO ->              getOtherInfo(embed, (String) options);
                case CHART_HOLDER ->            chartHolder(embed, (String) options);
                case NEW_PREFIX ->              setPrefix(embed, (String) options);
                case CHANNEL_SETTINGS ->        channelSettings(embed, (SettingsData) options);
                case SET_CACHE ->               setCache(embed, (int) options);
                case ERR_PERMISSION ->          errPermission(embed, (List<Set<Permission>>) options);
                case ERR_PERMISSION_THERMO ->   errPermission(embed, (Set<Permission>) options);
                case ERR_INPUT ->               inputError(embed, (String) options);
                case ERR_FIX ->                 error(embed, (List<String>) options);
                case ERR ->                     error(embed, (String) options);
                case DYNAMIC ->                 dynamicEmbed(embed, (List<String>) options);
                default ->                      throw new IllegalArgumentException("Invalid embed type.");
            };
        }
    }

    // ***************************************************************
    // **                           GENERAL                         **
    // ***************************************************************

    private static ThermoEmbed chartHolder(final ThermoEmbed embed, final String serverName) {
        embed.setTitle("Monitor Frequency Chart");
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

    private static ThermoEmbed setPrefix(final ThermoEmbed embed, final String newPrefix) {
        embed.setTitle("Thermostat will now reply to: " + "`" + newPrefix + "` .");
        return embed;
    }

    private static ThermoEmbed getGuide(final ThermoEmbed embed, final String prefix, final String guildName) {
        embed.setTitle("🎯 My prefix in " + guildName + " is: " + "`" + prefix + "`");
        embed.addField("❓ Need help?", "Try `" + prefix + "info`", false);
        embed.addField("💖 Like the bot? Please give it an upvote!",
                "Thank you for your support! Vote for Thermostat with `" + prefix + "vote`", false
        );
        embed.addField("Gateway Ping: ", String.valueOf(Thermostat.thermo.getGatewayPing()), true);
        embed.addField("Status:", Thermostat.thermo.getStatus().toString().trim(), true);
        embed.addField("Total Guilds:", String.valueOf(Thermostat.thermo.getGuilds().size()), true);
        return embed;
    }

    private static ThermoEmbed channelSettings(final ThermoEmbed embed, final SettingsData data) {
        embed.setTitle("Settings for #" + data.channelName + ":");
        embed.setDescription("─────────────────────────────");
        embed.addField("⌚ Min Slowmode:", data.min == 0 ? "**-**" : "**" + data.min + "**", true);
        embed.addField("⌚ Max Slowmode:", data.max == 0 ? "**-**" : "**" + data.max + "**", true);
        embed.addField(":regional_indicator_m: Monitored:", (data.monitor) ? "Yes" : "No", true);
        embed.addField(":regional_indicator_f: Filtered:", (data.filter) ? "Yes" : "No", true);
        embed.addField(":regional_indicator_s: Sensitivity:", String.format("%.5f", data.sensitivity), true);
        embed.addField(":regional_indicator_c: Caching Size:", String.valueOf(data.cachingSize), true);
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

    private static ThermoEmbed actionSuccessful(final ThermoEmbed embed) {
        embed.setTitle("Action was completed successfully.");
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

    private static ThermoEmbed setCache(final ThermoEmbed embed, final int newCachingSize) {
        embed.setTitle("The *Caching Size* for your server has been updated to **"
                + newCachingSize + "**");
        return embed;
    }

    // ***************************************************************
    // **                            HELP                           **
    // ***************************************************************

    private static ThermoEmbed helpInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "info");
        embed.setDescription("Brings up an interactive help menu, " +
                "or specific details about a command if you need to know more");
        embed.addField("Aliases",
                "**info, io, help, hp**",
                false
        );
        embed.addField("Switches",
                "• `-c <command name>` | Select a command to get information about.",
                false
        );
        embed.addField("Example", "`" + prefix + "info -c monitor`", false);
        return embed;
    }

    private static ThermoEmbed helpInvite(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "invite");
        embed.setDescription("Provides an invite to our" +
                " support server, and a link to get Thermostat");
        embed.addField("Aliases",
                "**invite, iv**",
                false
        );
        embed.addField("Example", "`" + prefix + "invite`", false);
        return embed;
    }

    private static ThermoEmbed helpVote(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "vote");
        embed.setDescription("Provides links to voting websites where you" +
                " can vote for Thermostat. Thank you for your support!");
        embed.addField("Aliases",
                "**vote, vo**",
                false
        );
        embed.addField("Example", "`" + prefix + "vote`", false);
        return embed;
    }

    private static ThermoEmbed helpChart(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "chart");
        embed.setDescription("Makes awesome charts from your server's stats");
        embed.addField("Charts: ", "• slowfreq", false);
        embed.addField("Aliases",
                "**chart, ch**",
                false
        );
        embed.addField("Switches",
                "• `--type <command name>` | Select the chart type\n" +
                "• `-c <channels/categories>` | Provide specific channels to be used in the chart",
                false
        );
        embed.addField("Example", "`" + prefix + "chart --type slowfreq`", false);
        return embed;
    }

    private static ThermoEmbed helpCaching(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "setcaching");
        embed.setDescription("Sets the global caching size for your server");
        embed.addField("Aliases",
                "**setcaching, sc**",
                false
        );
        embed.addField("Switches",
                "• `-s <size>` | The caching size to change to" +
                "• `-c <channels/categories>` | Provide specific channels to be used in the chart",
                false
        );
        embed.addField("Example", "`" + prefix + "setcaching -s 20`", false);
        return embed;
    }

    private static ThermoEmbed helpGetMonitor(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "getmonitor");
        embed.setDescription("Shows which channels are currently being monitored" +
                " or filtered in this server");
        embed.addField("Aliases",
                "**getmonitor, gm**",
                false
        );
        embed.addField("Example", "`" + prefix + "getmonitor`", false);
        return embed;
    }

    private static ThermoEmbed helpSettings(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "settings");
        embed.setDescription("Shows details about the configuration of a channel");
        embed.addField("Aliases",
                "**settings, st**",
                false
        );
        embed.addField("Switches",
                "• `-c <channel>` | Select the channel to retrieve settings for",
                false
        );
        embed.addField("Example", "`" + prefix + "getmonitor`", false);
        return embed;
    }

    private static ThermoEmbed helpMonitor(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "monitor");
        embed.setDescription("Adds/Removes text channels to the slowmode monitoring database. " +
                "When a channel is being monitored, slowmode will be automatically adjusted by Thermostat depending " +
                "on the amount of messages currently coming in.");
        embed.addField("Aliases",
                "**settings, st**",
                false
        );
        embed.addField("Switches",
                """
                        • `--on` | Monitor channels
                        • `--off` | Unmonitor channels
                        • `-c <channels/categories>` | Select the channel to perform action for
                        • `--all` | Perform this action for all the channels in your server
                        """,
                false
        );
        embed.addField("Example", "`" + prefix + "monitor --on`", false);
        return embed;
    }

    private static ThermoEmbed helpSensitivity(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "sensitivity");
        embed.setDescription("Sets the sensitivity level for the channel. " +
                "The higher the sensitivity, the easier for Thermostat to initiate slowmode.");
        embed.addField("Aliases",
                "**sensitivity, ss**",
                false
        );
        embed.addField("Switches",
                """
                        • `-s <value>` | Monitor channels
                        • `-c <channels/categories>` | Select the channel to perform action for
                        """,
                false
        );
        embed.addField("Example", "`" + prefix + "sensitivity -s 3`", false);
        return embed;
    }

    private static ThermoEmbed helpSetBounds(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "setbounds");
        embed.setDescription("Sets the upper and lower bounds for the slowmode of the channel. " +
                "This means that when Thermostat adjusts this channel's slowmode, the slowmode will be kept within the " +
                "minimum/maximum limits you have provided.");
        embed.addField("Aliases",
                "**setbounds, sb**",
                false
        );
        embed.addField("Switches",
                """
                        • `-m <minimum>` | Minimum slowmode bound
                        • `-M <maximum>` | Minimum slowmode bound
                        • `-c <channels/categories>` | Select the channel to perform action for
                        """,
                false
        );
        embed.addField("Example", "`" + prefix + "setbounds -m 5 -M 25`", false);
        return embed;
    }

    private static ThermoEmbed helpFilter(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "filter");
        embed.setDescription("[WIP] Enables/Disables curse-word filtering for a channel.");
        embed.addField("Aliases",
                "**filter, ft**",
                false
        );
        embed.addField("Switches",
                """
                        • `--on` | Filter channels
                        • `--off` | Unfilter channels
                        • `-c <channels/categories>` | Select the channel to perform action for
                        • `--all` | Perform this action for all the channels in your server
                        """,
                false
        );
        embed.addField("Example", "`" + prefix + "filter --on`", false);
        return embed;
    }

    private static ThermoEmbed helpPrefix(final ThermoEmbed embed, final String prefix) {
        embed.setTitle(prefix + "prefix");
        embed.setDescription("Manages Thermostat's prefix. Allows up to 5 characters.");
        embed.addField("Aliases",
                "**prefix, px**",
                false
        );
        embed.addField("Switches",
                """
                        • `-p <prefix>` | Prefix to assign
                        • `--reset` | Reset the prefix to the default (th!)
                        """,
                false
        );
        embed.addField("Example", "`" + prefix + "filter --on`", false);
        return embed;
    }

    private static ThermoEmbed getMonitorInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Monitoring Commands");
        embed.setDescription("─────────────────────────");
        embed.addField("monitor", "`" + prefix + "monitor --on/--off -c [channels/categories]`", false);
        embed.addField("getmonitor", "`" + prefix + "getmonitor`", false);
        embed.addField("setbounds", "`" + prefix + "setbounds --m [value] --M [value] -c [channels/categories]`", false);
        embed.addField("settings", "`" + prefix + "settings -c [channel]`", false);
        embed.addField("sensitivity", "`" + prefix + "sensitivity -s [value] -c [channels]`", false);
        embed.setFooter("─────────────────────────────\n🔼 to go back, ❌ to exit");
        return embed;
    }

    private static ThermoEmbed getUtilityInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Utility Commands");
        embed.setDescription("─────────────────────────");
        embed.addField("filter", "`" + prefix + "filter --on/--off -c [channels/categories]`", false);
        embed.setFooter("─────────────────────────────\n🔼 to go back, ❌ to exit");
        return embed;
    }

    private static ThermoEmbed getOtherInfo(final ThermoEmbed embed, final String prefix) {
        embed.setTitle("Other Commands");
        embed.setDescription("─────────────────────────");
        embed.addField("info",  "`" + prefix + "info --type [commandname]`", false);
        embed.addField("prefix","`" + prefix + "prefix -p [prefix]`", false);
        embed.addField("chart", "`" + prefix + "chart --type [type]`", false);
        embed.addField("vote",  "`" + prefix + "vote`", false);
        embed.addField("invite", "`" + prefix + "invite`", false);
        embed.setFooter("─────────────────────────────\n🔼 to go back, ❌ to exit");
        return embed;
    }

    private static ThermoEmbed getInfoSelection(final ThermoEmbed embed) {
        embed.setTitle("Guide Menu  🌡");
        embed.setDescription("""
                Need more help or examples?
                🡆 Try `th!info --type [commandname]` or [visit our wiki](https://github.com/OpenSrcerer/thermostat/wiki)! 🡄
                ─────────────────────────────"""
        );
        embed.addField("⏱ Monitoring", "Commands to help you manage slowmode in channels.", false);
        embed.addField("🔧 Utility", "Useful features to help you moderate your server.", false);
        embed.addField("✨ Other", "Informational commands and other miscellaneous functions.", false);
        embed.addField("❌ Exit", "Exit this menu.", false);
        embed.setFooter("──────────────────────────────────\nReact below to learn more!");
        return embed;
    }

    // ***************************************************************
    // **                            ERROR                          **
    // ***************************************************************

    private static ThermoEmbed errPermission(final ThermoEmbed embed, final List<Set<Permission>> permissions) {
        embed.setTitle("❌ Missing Permissions! Details:");
        if (!permissions.get(0).isEmpty()) {
            StringBuilder missingPerms = new StringBuilder();
            permissions.get(0).forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
            embed.addField("Thermostat lacks these permissions:", missingPerms.toString(), false);
        }
        if (!permissions.get(1).isEmpty()) {
            StringBuilder missingPerms = new StringBuilder();
            permissions.get(1).forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
            embed.addField("You lack these permissions:", missingPerms.toString(), false);
        }
        return embed;
    }

    private static ThermoEmbed errPermission(final ThermoEmbed embed, final Set<Permission> thermoPermissions) {
        embed.setTitle("❌ Missing Permissions! Details:");
        final StringBuilder missingPerms = new StringBuilder();
        thermoPermissions.forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
        embed.addField("Thermostat lacks these permissions:", missingPerms.toString(), false);
        return embed;
    }

    private static ThermoEmbed inputError(final ThermoEmbed embed, final String error) {
        embed.setTitle("You have an error in your input:");
        embed.addField("`" + error + "`", "", false);
        return embed;
    }

    private static ThermoEmbed error(final ThermoEmbed embed, final List<String> error) {
        embed.setTitle("❌ An error has occurred");
        embed.addField("Error details:", error.get(0), false);
        embed.addField("Suggested fix: ", error.get(1), false);
        embed.addField("Support server: https://discord.gg/FnPb4nM", "", false);
        return embed;
    }

    private static ThermoEmbed error(final ThermoEmbed embed, final String error) {
        embed.setTitle("❌ An error has occurred");
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
