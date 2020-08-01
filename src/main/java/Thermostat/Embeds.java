package Thermostat;

import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

/**
 * Class for all static embeds that do not need
 * runtime editing.
 */
public class Embeds {
    public static EmbedBuilder incorrectPrefix() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("The prefix you have inserted is not valid.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder resetPrefix() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Thermostat's prefix has been reset to `" + thermostat.prefix + "`");
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

    public static EmbedBuilder setPrefix(String authorID, String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Thermostat will now reply to: " + "`" + prefix + "`");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getPrefix(String authorID, String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("My prefix in this server is: " + "`" + prefix + "`");
        eb.setDescription("You can give me a new one using `" + prefix + "prefix set <prefix>`!\n " + "Reset it to the default using `" + prefix + "prefix reset`!");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder channelSettings(String channelName, String authorID, int max, int min, boolean monitor) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Settings for #" + channelName + ":");
        if (min == 0) {
            eb.addField("Min Slowmode:", "**-**", true);
        } else {
            eb.addField("Min Slowmode:", "**"+ min + "**", true);
        }

        if (max == 0) {
            eb.addField("Max Slowmode:", "**-**", true);
        } else {
            eb.addField("Max Slowmode:", "**"+ max + "**", true);
        }


        if (monitor) {
            eb.addField("Currently Monitored:", "**Yes**", false);
        } else {
            eb.addField("Currently Monitored:", "**No**", false);
        }

        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getVote(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Vote for Thermostat on top.gg");
        eb.addField("Link:", "https://top.gg/bot/700341788136833065/vote", true);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder inviteServer(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Information about invites:");
        eb.addField("Support Server:", "https://discord.gg/FnPb4nM", false);
        eb.addField("Get Thermostat:", "https://top.gg/bot/700341788136833065", false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder channelRemoved() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("This channel was removed from monitoring due to not " +
                "finding any messages to monitor. If the messages are too old, " +
                "it will not be possible to monitor the channel.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder invalidSlowmode() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please enter a valid slowmode value.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder bothChannelAndSlow() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please specify the channels and then the slowmode.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder insufficientReact(String perm) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Error executing command! Insufficient permissions: `" + perm + "`");
        eb.setDescription("Thermostat needs the `" + perm + "` permission in this channel in order to perform this action.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder insufficientReact() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Error executing command! Insufficient permissions: `ADD_REACTIONS`");
        eb.setDescription("Add the `ADD_REACTIONS` permission to Thermostat and try again.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder simpleInsufficientPerm(String perm) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Unable to perform action! Insufficient permissions: `" + perm + "`");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder insufficientPerm() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Error managing channel! Insufficient permissions: `MANAGE_CHANNELS`");
        eb.setDescription("Channel was removed from monitoring database. Add the `MANAGE_CHANNELS` permission to Thermostat and re-monitor the channel with th!monitor.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder insufficientPerm(String perm) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Error managing channel! Insufficient permissions: `" + perm + "`");
        eb.setDescription("Channel was removed from monitoring database. Add the `" + perm + "` permission to Thermostat and re-monitor the channel with th!monitor.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder allRemoved(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("All channels are no longer being monitored.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00ff00);
        return eb;
    }

    public static EmbedBuilder missedPrompt() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Did not react to prompt in time. Operation cancelled.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder promptEmbed(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Are you sure you want to perform this action? Click the reaction below if you're sure you want to continue.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Requested by " + authorID, thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder fatalError(String errFix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("If you are seeing this message, a fatal error has occurred. " + errFix + " If that does not fix your issue, please join our support server: https://discord.gg/FnPb4nM");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }

    public static EmbedBuilder fatalError() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("If you are seeing this message, a fatal error has occurred. Try unmonitoring your channels and monitoring them again. If that does not fix your issue, please join our support server: https://discord.gg/FnPb4nM");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }

    public static EmbedBuilder userNoPermission() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("You must have the `MANAGE_CHANNELS` permission in order to execute this command.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder noChannels() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("No channels are currently being monitored!");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder channelNeverMonitored() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("That channel has never been monitored before!");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder specifyChannel() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please specify the channel to view its' settings.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder specifyChannels() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please specify the channels you want to configure.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder channelNotFound() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("That channel was not found in this guild.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder getMonitor() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("`th!monitor`");
        eb.addField("This command is used to add a channel to the monitoring database. " +
         "You can also input an ID of a category instead, and the text channels in that category will be removed.", "`Syntax: th!monitor/mon/m <channels>/<categories>.`", false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder getMonitorInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🌡┇Monitoring Commands - Permissions required: `MANAGE_CHANNEL`");
        eb.addField("⬆ Menu", "Go back to the Main Menu", false);
        eb.addField(prefix + "monitor┇Adds text channels to the slowmode monitoring database.", "Syntax: `" + prefix + "monitor/mon/m <channels>/<categories>.`", false);
        eb.addField(prefix + "unmonitor┇Removes text channels from the slowmode monitoring database.", "Syntax: `" + prefix + "unmonitor/unmon/um <channels>/<categories>.`", false);
        eb.addField(prefix + "getmonitor┇Shows which channels are currently being monitored in your server.", "Syntax: `" + prefix + "getmonitor/getmon/gm.`", false);
        eb.addField(prefix + "unmonitorall┇Stops ALL your channels from being monitored.", "Syntax: `" + prefix + "unmonitorall/unmonall/uma.`", false);
        eb.addField(prefix + "setminimum┇Sets the lower bound for the slowmode of the channel.", "Syntax: `" + prefix + "setminimum/setmin/smn <channels>/<categories> <slowmode>.`", false);
        eb.addField(prefix + "setmaximum┇Sets the upper bound for the slowmode of the channel.", "Syntax: `" + prefix + "setmaximum/setmax/smx <channels>/<categories> <slowmode>.`", false);
        eb.addField(prefix + "settings┇Shows details about the configuration of the given channel.", "Syntax: `" + prefix + "settings/s <channel>.`", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getOtherInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("ℹ┇Other Commands");
        eb.addField("⬆ Menu", "Go back to the Main Menu", false);
        eb.addField(prefix + "info┇Brings up the main help menu.", "`Syntax:" + prefix + "info/i/help/h.`", false);
        eb.addField("@Thermostat prefix┇Manages Thermostat's prefix.", "Syntax: <@!" + thermostat.thermo.getSelfUser().getId() + "> `prefix/p.`", false);
        eb.addField(prefix + "vote┇Shows a link to vote for Thermostat on top.gg.", "Syntax: `" + prefix + "vote/v.`", false);
        eb.addField(prefix + "invite┇Provides an invite link to Thermostat's support server, and the top.gg website.", "Syntax: `" + prefix + "invite/server.`", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder getInfoSelection() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Menu");
        eb.addField("🌡 Monitoring", "Commands to help you manage slowmode in channels.", false);
        eb.addField("ℹ Other", "Informational commands that provide other functionalities.", false);
        eb.addField("❌ Exit", "Exit the info menu.", false);
        eb.setColor(0x00aeff);
        return eb;
    }
}
