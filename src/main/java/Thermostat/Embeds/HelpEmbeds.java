package thermostat.embeds;

import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public final class HelpEmbeds {
    public static EmbedBuilder expandedHelpInfo(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "info [command]`");
        eb.setDescription("**Aliases: " + prefix + "help/hp/io** ⇨ Brings up an interactive help menu, or specific details about a command if you need to know more." +
                " Arguments surrounded by <> are mandatory, and ones surrounded by [] are optional.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpInvite(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "chart <charttype> [channel(s)/category(ies)]`");
        eb.setDescription("**Alias: " + prefix + "iv** ⇨ Provides an invite link to Thermostat's support server, and the top.gg website.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpVote(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "chart <charttype> [channel(s)/category(ies)]`");
        eb.setDescription("**Alias: " + prefix + "vo** ⇨ Provides links to voting websites where you can vote for Thermostat. Thank you for your support!");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpChart(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "chart <charttype> [channel(s)/category(ies)]`");
        eb.setDescription("**Alias: " + prefix + "ch** ⇨ Command that gives informational data about Thermostat's operation in chart form.");
        eb.addField("Chart Types (Name - CmdName): ", "★ Slowmode Frequency - (slowfreq)", false);
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpGetMonitor(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "getmonitor`");
        eb.setDescription("**Alias: " + prefix + "gm** ⇨ Shows which channels are currently being monitored or filtered in your server.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpSettings(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "settings [channel]`");
        eb.setDescription("**Alias: " + prefix + "st** ⇨ Shows details about the configuration of the given channel, " +
                "such as if it's currently being monitored or filtered, the bounds you have provided, etc.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpMonitor(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "monitor <on/off> [channel(s)/category(ies)]`");
        eb.setDescription("**Alias: " + prefix + "mn** ⇨ Adds/Removes text channels to the slowmode monitoring database. " +
                "When a channel is being monitored, slowmode will be automatically adjusted by Thermostat depending " +
                "on the amount of messages currently coming in.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpSensitivity(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "sensitivity <sensitivity> [channel(s)/category(ies)]```");
        eb.setDescription("**Alias: " + prefix + "ss** ⇨ Sets the sensitivity level for the channel. " +
                "**Requires a value between -10 and 10, you may use decimal numbers.** " +
                "The higher the sensitivity, the easier for Thermostat to initiate slowmode.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpSetBounds(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "setbounds <min/max> <slowmode> [channel(s)/category(ies)]```");
        eb.setDescription("**Alias: " + prefix + "sb** ⇨ Sets the upper and lower bounds for the slowmode of the channel. " +
                "This means that when Thermostat adjusts this channel's slowmode, the slowmode will be kept within the " +
                "minimum/maximum limits you have provided.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpPrefix(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "prefix <newprefix/reset>```");
        eb.setDescription("**Alias: " + prefix + "px** ⇨ Manages Thermostat's prefix. You can change it to pretty much anything. " +
                "Note: You can call this command by using @Thermostat instead of the prefix.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder expandedHelpFilter(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "filter <on/off> [channel(s)/category(ies)]```");
        eb.setDescription("**Alias: " + prefix + "fi** ⇨ [WIP] Enables/Disables curse-word filtering for a channel.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder helpSensitivity(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "sensitivity <sensitivity> [channel(s)/category(ies)]```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder helpSetBounds(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "setbounds <min/max> <slowmode> [channel(s)/category(ies)]```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }

    public static EmbedBuilder helpFilter(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "filter <on/off> [channel(s)/category(ies)]```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0x00aeff);
        return eb;
    }
}
