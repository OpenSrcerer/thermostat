package thermostat.preparedStatements;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import thermostat.Thermostat;

import java.time.Instant;
import java.util.EnumSet;

public abstract class ErrorEmbeds {
    public static EmbedBuilder errPermission(EnumSet<Permission> thermoPermissions, EnumSet<Permission> memberPermissions, long commandId) {
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
        eb.setFooter("Command ID: " + commandId, Thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder errPermission(EnumSet<Permission> thermoPermissions) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ Error encountered! Details:");

        StringBuilder missingPerms = new StringBuilder();
        thermoPermissions.forEach(permission -> missingPerms.append(permission.getName()).append("\n"));
        eb.addField("Thermostat lacks these permissions:", missingPerms.toString(), false);

        eb.setTimestamp(Instant.now());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder inputError(String error, long commandId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("You have an error in your input:");
        eb.addField("`" + error + "`", "", false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Command ID: " + commandId, Thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }

    public static EmbedBuilder error(String error, String errFix, long commandId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ An error has occurred. ❌");
        eb.addField("Error details:", error, false);
        eb.addField("Suggested fix: ", errFix, false);
        eb.addField("Support server: https://discord.gg/FnPb4nM", "", false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Command ID: " + commandId, Thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }

    public static EmbedBuilder error(String error, long commandId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ An error has occurred. ❌");
        eb.addField("Error details:", error, false);
        eb.addField("Support server: https://discord.gg/FnPb4nM", "", false);
        eb.setTimestamp(Instant.now());
        eb.setFooter("Command ID: " + commandId, Thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }

    public static EmbedBuilder error(String error) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ An error has occurred. ❌");
        eb.addField("Error details:", error, false);
        eb.addField("Support server: https://discord.gg/FnPb4nM", "", false);
        eb.setTimestamp(Instant.now());
        eb.setFooter(Thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0x36393f);
        return eb;
    }


    public static EmbedBuilder incorrectPrefix(long commandId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("The prefix you have inserted is not valid.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Command ID: " + commandId, Thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xff0000);
        return eb;
    }

    public static EmbedBuilder insertPrefix(long commandId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Please insert a prefix.");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Command ID: " + commandId, Thermostat.thermo.getSelfUser().getAvatarUrl());
        eb.setColor(0xffff00);
        return eb;
    }
}
