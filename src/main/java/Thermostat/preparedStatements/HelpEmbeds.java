package thermostat.preparedStatements;

import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public abstract class HelpEmbeds {

    public static EmbedBuilder helpChart(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "chart [channel(s)] <charttype>`");
        eb.addField("Chart Types (Name - CmdName): ", "★ Slowmode Frequency - (slowfreq)", false);
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpGetMonitorList(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "getmonitor`");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpSettings(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "settings [channel]`");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpMonitor(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n `" + prefix + "monitor <true/false> [channel(s)/category(ies)]`");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpSensitivity(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "sensitivity [channel(s)] <sensitivity>\n -10 <= Sensitivity <= 10```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpSetBounds(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "setbounds <min/max> <slowmode> [channel(s)/category(ies)]```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpPrefix(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "prefix <newprefix>```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder helpFilter(String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Usage:\n ```" + prefix + "filter <true/false> [channel(s)]```");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }
}
