package thermostat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.time.Instant;
import java.util.EnumSet;

/**
 * Class for all static embeds.
 */
public abstract class Embeds {

    public static EmbedBuilder permissionError(EnumSet<Permission> memberPermissions, EnumSet<Permission> thermoPermissions) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "filter [channel(s)] <true/false>```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpFilter(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "filter [channel(s)] <true/false>```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder chartHolder(String authorID, String authorAvatarURL, String serverName) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Slowmode Frequency Chart");
        eb.setDescription(serverName);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, authorAvatarURL);
        eb.setColor(0x00ff00);
        return eb;
    }

    public static EmbedBuilder ioError() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("An I/O Error occurred when trying to fetch the chart, please try again.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
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

    public static EmbedBuilder helpChart(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "chart [channel(s)] <charttype>`");
        eb.addField("Chart Types (Name - CmdName): ", "★ Slowmode Frequency - (slowfreq)", false);
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder invalidSensitivity() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please enter a valid sensitivity value.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder helpSensitivity(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "sensitivity [channel(s)] <sensitivity>\n -10 <= Sensitivity <= 10```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder samePrefix(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("My prefix is already `" + prefix + "` !");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder incorrectPrefix() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("The prefix you have inserted is not valid.");
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

    public static EmbedBuilder insertPrefix() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please insert a prefix.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xffff00);
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

    public static EmbedBuilder channelSettings(String channelName, String authorID, String authorAvatarURL, int max, int min, float sensitivity, boolean monitor, boolean filter) {
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
        eb.setDescription("[Vote for Thermostat on top.gg](https://top.gg/bot/700341788136833065/vote)");
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

    public static EmbedBuilder invalidSlowmode() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please enter a valid slowmode value.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder bothChannelAndSlow() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please specify the channels and then the slowmode.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
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

    public static EmbedBuilder fatalError(String errFix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("If you are seeing this message, a fatal error has occurred. " + errFix + " If that does not fix your issue, please join our support server: https://discord.gg/FnPb4nM");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }

    public static EmbedBuilder fatalError() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("If you are seeing this message, a fatal error has occurred. Try unmonitoring your channels and monitoring them again. If that does not fix your issue, please join our support server: https://discord.gg/FnPb4nM");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
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

    public static EmbedBuilder channelNeverMonitored() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("That channel has never been monitored before!");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder specifyChannels() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please specify the channels you want to configure.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder channelNotFound() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("That channel was not found in this guild.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
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
        eb.addField(prefix + "monitor┇Adds text channels to the slowmode monitoring database.", "Syntax: `" + prefix + "monitor/mn <channels>/<categories>.`", false);
        eb.addField(prefix + "unmonitor┇Removes text channels from the slowmode monitoring database.", "Syntax: `" + prefix + "unmonitor/um <channels>/<categories>.`", false);
        eb.addField(prefix + "getmonitor┇Shows which channels are currently being monitored or filtered in your server.", "Syntax: `" + prefix + "getmonitor/gm.`", false);
        eb.addField(prefix + "unmonitorall┇Stops ALL your channels from being monitored.", "Syntax: `" + prefix + "unmonitorall/ua.`", false);
        eb.addField(prefix + "setminimum┇Sets the lower bound for the slowmode of the channel.", "Syntax: `" + prefix + "setminimum/sm <channels>/<categories> <slowmode>.`", false);
        eb.addField(prefix + "setmaximum┇Sets the upper bound for the slowmode of the channel.", "Syntax: `" + prefix + "setmaximum/sx <channels>/<categories> <slowmode>.`", false);
        eb.addField(prefix + "settings┇Shows details about the configuration of the given channel.", "Syntax: `" + prefix + "settings/st <channel>.`", false);
        eb.addField(prefix + "sensitivity┇Sets the sensitivity level for the channel. Requires a value between -10 and 10, you may use decimal numbers. The higher the sensitivity, the easier for Thermostat to initiate slowmode.", "Syntax: `" + prefix + "sensitivity/ss [channel(s)] <sensitivity>.`", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getUtilityInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🔧┇Utility Commands");
        eb.addField("⬆ Menu", "Go back to the Main Menu", false);
        eb.addField(prefix + "filter┇Enables/Disables curse-word filtering for a channel.", "Syntax: `" + prefix + "filter/ft <charttype>.`", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getOtherInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("ℹ┇Other Commands");
        eb.addField("⬆ Menu", "Go back to the Main Menu", false);
        eb.addField(prefix + "info┇Brings up the main help menu.", "`Syntax:" + prefix + "info/io/help/hp.`", false);
        eb.addField("@Thermostat prefix┇Manages Thermostat's prefix.", "Syntax: <@!" + thermostat.thermo.getSelfUser().getId() + "> `prefix/px.`", false);
        eb.addField(prefix + "chart┇Command that gives informational data about Thermostat's operation in chart form.", "Syntax: `" + prefix + "chart/ch <charttype>.`", false);
        eb.addField(prefix + "filter┇Enables/Disables curse-word filtering for a channel.", "Syntax: `" + prefix + "filter/ft <charttype>.`", false);
        eb.addField(prefix + "vote┇Shows a link to vote for Thermostat on top.gg.", "Syntax: `" + prefix + "vote/vo.`", false);
        eb.addField(prefix + "invite┇Provides an invite link to Thermostat's support server, and the top.gg website.", "Syntax: `" + prefix + "invite/iv.`", false);
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
