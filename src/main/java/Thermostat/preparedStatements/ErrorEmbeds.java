package thermostat.preparedStatements;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import thermostat.thermostat;

import java.time.Instant;
import java.util.EnumSet;

public abstract class ErrorEmbeds {
    public static EmbedBuilder errPermission(EnumSet<Permission> thermoPermissions, EnumSet<Permission> memberPermissions) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ Error encountered! Details:");

        if (!thermoPermissions.isEmpty()) {
            StringBuilder missingPerms = new StringBuilder();
            thermoPermissions.forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
            eb.addField("Thermostat lacks these permissions:", missingPerms.toString(), false);
        }
        if (!memberPermissions.isEmpty()) {
            StringBuilder missingPerms = new StringBuilder();
            memberPermissions.forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
            eb.addField("You lack these permissions:", missingPerms.toString(), false);
        }

        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder errFatal(String errFix, String error) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("If you are seeing this message, an error has occurred. Try " + errFix + ". If the problem persists, please join my support server: https://discord.gg/FnPb4nM");
        eb.addField("Error details:", error, false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }

    public static EmbedBuilder errFatal(String error) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("If you are seeing this message, an error has occurred. Please join my support server: https://discord.gg/FnPb4nM");
        eb.addField("Error details:", error, false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
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

    public static EmbedBuilder incorrectPrefix() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("The prefix you have inserted is not valid.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder insertPrefix() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please insert a prefix.");
        eb.setTimestamp(Instant.now());
        eb.setColor(0xffff00);
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

    public static EmbedBuilder specifyChannels() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please specify the channels you want to configure.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }

    public static EmbedBuilder channelNotFound(String channel) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Channel " + channel + " was not found in this guild.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Thermostat", thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }
}
