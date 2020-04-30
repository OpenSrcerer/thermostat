package Thermostat;

import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Class for all static embeds that do not need
 * runtime editing.
 */
public class Embeds {
    public static EmbedBuilder insufficientReact(String perm) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ Error executing command! Insufficient permissions: `" + perm + "`");
        eb.setDescription("Thermostat needs the `" + perm + "` permission in this channel in order to execute this command.");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder insufficientReact() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ Error executing command! Insufficient permissions: `Add Reactions`");
        eb.setDescription("Add the `Add Reactions` permission to Thermostat and try again.");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder insufficientPerm() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ Error managing channel! Insufficient permissions: `Manage Channels`");
        eb.setDescription("Channel was removed from monitoring database. Add the `Manage Channels` permission to Thermostat and re-monitor the channel with th!monitor.");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder insufficientPerm(String perm) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ Error managing channel! Insufficient permissions: `" + perm + "`");
        eb.setDescription("Channel was removed from monitoring database. Add the `" + perm + "` permission to Thermostat and re-monitor the channel with th!monitor.");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder allRemoved() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("ℹ All channels were successfully removed from the monitoring database.");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder missedPrompt() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("ℹ Did not react to prompt in time. Operation cancelled.");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder promptEmbed(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❓ Are you sure you want to perform this action? Click the reaction below if you're sure you want to continue.");
        eb.setDescription("<@" + authorID + ">");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder fatalError() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ If you are seeing this message, a fatal error has occurred. Please contact the bot developer. (Bonkers#6969)");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder userNoPermission(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ You must have the `MANAGE_CHANNELS` permission in order to execute this command.");
        eb.setDescription("<@" + authorID + ">");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder noChannels(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("⚠ No channels are currently being monitored!");
        eb.setDescription("<@" + authorID + ">");
        eb.setColor(0xeb9834);
        return eb;
    }

    public static EmbedBuilder specifyChannels(String authorID) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("⚠ Please specify the channels you want to configure.");
        eb.setDescription("<@" + authorID + ">");
        eb.setColor(0xeb9834);
        return eb;
    }


    public static EmbedBuilder getInfo() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("ℹ Command list for Thermostat ℹ");
        // eb.setImage();
        eb.addField("-------------  Commands -------------", "You need to have the MANAGE_CHANNELS permission to run any of these commands.", false);
        eb.addField("th!monitor", "Syntax: `th!monitor/mon/m <channels>/<categories>`. Adds text channels to the slowmode monitoring database.", false);
        eb.addField("th!unmonitor", "Syntax: `th!unmonitor/unmon/um <channels>/<categories>`. Removes text channels from the slowmode monitoring database.", false);
        eb.addField("th!getmonitor", "Syntax: `th!getmonitor/getmon/gm`. Shows which channels are currently being monitored in your server.", false);
        eb.addField("th!unmonitorall", "Syntax: `th!unmonitorall/unmonall/uma`. Stops ALL your channels from being monitored.", false);
        eb.addField("th!info", "Syntax: `th!info/i`. Shows this message.", false);
        eb.setColor(0xeb9834);
        eb.setFooter("Created by Bonkers#6969");
        return eb;
    }
}
