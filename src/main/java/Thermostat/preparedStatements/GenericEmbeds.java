package thermostat.preparedStatements;

import net.dv8tion.jda.api.EmbedBuilder;
import thermostat.thermostat;

import java.time.Instant;

/**
 * Class for all static embeds.
 */
public abstract class GenericEmbeds {

    public static EmbedBuilder chartHolder(String authorID, String authorAvatarURL, String serverName) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Slowmode Frequency Chart");
        eb.setDescription(serverName);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00ff00);
        return eb;
    }

    public static EmbedBuilder noChannelsEverSlowmoded() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("No channels have ever been given slowmode by Thermostat.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder samePrefix(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("My prefix is already `" + prefix + "` !");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder resetPrefix() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Thermostat's prefix has been reset to `" + thermostat.prefix + "` .");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder setPrefix(String authorID, String authorAvatarURL, String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Thermostat will now reply to: " + "`" + prefix + "` .");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getPrefix(String authorID, String authorAvatarURL, String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("My prefix in this server is: " + "`" + prefix + "`");
        eb.setDescription("You can give me a new one using `" + prefix + "prefix set <prefix>`!\n " + "Reset it to the default using `" + prefix + "prefix reset`!");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder channelSettings(String channelName, String authorID, String authorAvatarURL, int min, int max, float sensitivity, boolean monitor, boolean filter) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Settings for #" + channelName + ":");
        if (min == 0) {
            eb.addField("Min Slowmode:", "**-**", true);
        } else {
            eb.addField("Min Slowmode:", "**" + min + "**", true);
        }

        if (max == 0) {
            eb.addField("Max Slowmode:", "**-**", true);
        } else {
            eb.addField("Max Slowmode:", "**" + max + "**", true);
        }

        if (monitor) {
            eb.addField("Monitored:", "**Yes**", false);
        } else {
            eb.addField("Monitored:", "**No**", false);
        }

        if (filter) {
            eb.addField("Filtered:", "**Yes**", true);
        } else {
            eb.addField("Filtered:", "**No**", true);
        }

        String indicator = "`   ";

        for (float index = 0.5f; index <= sensitivity; index += 0.05f) {
            indicator = indicator.concat(" ");
        }

        indicator = indicator.concat("^ (" + String.format("%.1f", (sensitivity - 1f) * 20f) + ")`");
        eb.addField("Sensitivity:\n `-10 -------------------- 10`\n " + indicator, "", false);

        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getVote(String authorID, String authorAvatarURL) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🎉 Thank you for your continuous support! 🎉");
        eb.setDescription("[@top.gg](https://top.gg/bot/700341788136833065/vote)\n[@Rovel Stars](https://bots.rovelstars.ga/bots/700341788136833065/vote)");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder inviteServer(String authorID, String authorAvatarURL) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Useful Hyperlinks 🔧");
        eb.setDescription("[Join Support Server](https://discord.gg/FnPb4nM)\n[Get Thermostat for your own server](https://top.gg/bot/700341788136833065)");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder allRemoved(String authorID, String authorAvatarURL) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("All channels are no longer being monitored.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00ff00);
        return eb;
    }

    public static EmbedBuilder missedPrompt() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Did not react to prompt in time. Operation cancelled.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder promptEmbed(String authorID, String authorAvatarURL) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Are you sure you want to perform this action? Click the reaction below if you're sure you want to continue.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder noChannels() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("No channels are currently being monitored!");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getMonitor() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("`th!monitor`");
        eb.addField("This command is used to add a channel to the monitoring database. " +
                "You can also input an ID of a category instead, and the text channels in that category will be removed.", "`Syntax: th!monitor/mon/m <channels>/<categories>.`", false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder getMonitorInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🌡┇Monitoring Commands");
        eb.addField("⬆ Menu", "Go back to the Main Menu", false);
        eb.addField(prefix + "monitor", "Syntax: `" + prefix + "monitor <true/false> [channel(s)/category(ies)].`", false);
        eb.addField(prefix + "getmonitor", "Syntax: `" + prefix + "getmonitor.`", false);
        eb.addField(prefix + "setbounds", "Syntax: `" + prefix + "setbounds <min/max> <slowmode> [channel(s)/category(ies)].`", false);
        eb.addField(prefix + "settings", "Syntax: `" + prefix + "settings[channel].`", false);
        eb.addField(prefix + "sensitivity", "Syntax: `" + prefix + "sensitivity <sensitivity> [channel(s)].`", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getUtilityInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🔧┇Utility Commands");
        eb.addField("⬆ Menu", "Go back to the Main Menu", false);
        eb.addField(prefix + "filter", "Syntax: `" + prefix + "filter <charttype>.`", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getOtherInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("ℹ┇Other Commands");
        eb.addField("⬆ Menu", "Go back to the Main Menu", false);
        eb.addField(prefix + "info", "`Syntax:" + prefix + "info.`", false);
        eb.addField("@Thermostat prefix", "Syntax: <@!" + thermostat.thermo.getSelfUser().getId() + "> `prefix.`", false);
        eb.addField(prefix + "chart", "Syntax: `" + prefix + "chart <charttype>.`", false);
        eb.addField(prefix + "vote", "Syntax: `" + prefix + "vote.`", false);
        eb.addField(prefix + "invite", "Syntax: `" + prefix + "invite.`", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getInfoSelection() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Menu");
        eb.addField("🌡 Monitoring", "Commands to help you manage slowmode in channels.", false);
        eb.addField("🔧 Utility", "Useful features to help you moderate your server.", false);
        eb.addField("ℹ Other", "Informational commands that provide other functionalities.", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }
}
