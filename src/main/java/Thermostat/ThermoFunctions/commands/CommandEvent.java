package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseMention;

public interface CommandEvent {

    // boolean checkFormat();

    void execute();

    /**
     * Adds channel to database if it's not found.
     * @param guildId Id of guild.
     * @param channelId Id of channel.
     * @throws SQLException If something goes wrong
     * in the SQL transaction.
     */
    default void addIfNotInDb(String guildId, String channelId) throws SQLException {
        if (!DataSource.checkDatabaseForData("SELECT CHANNEL_ID FROM CHANNELS JOIN GUILDS ON " +
                "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) WHERE CHANNEL_ID = ?", channelId)) {
            Create.Channel(guildId, channelId, 0);
        }
    }

    default void addIfNotInDb(String guildId, List<String> channelIds) throws SQLException {
        for (String channelId : channelIds) {
            if (!DataSource.checkDatabaseForData("SELECT CHANNEL_ID FROM CHANNELS JOIN GUILDS ON " +
                    "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) WHERE CHANNEL_ID = ?", channelId)) {
                Create.Channel(guildId, channelId, 0);
            }
        }

    }

    /**
     * Executes a given CommandEvent if permission conditions are met.
     * @param eventMember Member that initiated the command.
     * @param eventChannel Channel where command was initiated.
     * @param lgr Logger for every command ran.
     */
    default void checkPermissionsAndExecute(@Nonnull CommandType commandType, @Nonnull Member eventMember, @Nonnull TextChannel eventChannel, @Nonnull Logger lgr) {
        // check main permissions
        eventChannel.getGuild()
                .retrieveMember(thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get member and thermostat missing permissions
                            EnumSet<Permission>
                                    missingMemberPerms = findMissingPermissions(commandType.getMemberPerms(), eventMember.getPermissions()),
                                    missingThermostatPerms = findMissingPermissions(commandType.getThermoPerms(), thermostat.getPermissions());

                            if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
                                execute();
                            } else {
                                lgr.info("Missing permissions on (" + eventChannel.getGuild().getName() + "/" + eventChannel.getGuild().getId() + "):" +
                                        " [" + missingThermostatPerms.toString() + "] [" + missingMemberPerms.toString() + "]");
                                Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
                            }
                        }
                );
    }

    default void checkPermissionsAndExecute(@Nonnull CommandType commandType, @Nonnull TextChannel eventChannel, @Nonnull Logger lgr) {
        eventChannel.getGuild()
                .retrieveMember(thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get Thermostat's missing permissions
                            EnumSet<Permission> missingThermostatPerms = findMissingPermissions(commandType.getThermoPerms(), thermostat.getPermissions());
                            missingThermostatPerms.addAll(checkThermostatOverrides(eventChannel, commandType.getThermoPerms()));

                            if (missingThermostatPerms.isEmpty()) {
                                execute();
                            } else {
                                lgr.info("Missing permissions on (" + eventChannel.getGuild().getName() + "/" + eventChannel.getGuild().getId() + "):" +
                                        " [" + missingThermostatPerms.toString() + "]");
                                Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms));
                            }
                        }
                );
    }
    
    private EnumSet<Permission> checkThermostatOverrides(@Nonnull TextChannel eventChannel, @Nonnull EnumSet<Permission> requiredCmdPerms) {
        // check permission overrides for denies
        Member thermo = eventChannel.getGuild().getSelfMember();
        PermissionOverride override = eventChannel.getPermissionOverride(thermo);

        return findDenyOverrides(requiredCmdPerms, override.getDenied();
    }

    /**
     * @param permissionsRequired Permissions required by the command.
     * @param permissionsGiven Permissions that the Member has.
     * @return Permissions that are needed to execute a specific
     * command but that the Member does not have.
     */
    private static @Nonnull EnumSet<Permission> findMissingPermissions(EnumSet<Permission> permissionsRequired, EnumSet<Permission> permissionsGiven) {
        for (Permission permission : permissionsGiven) {
            permissionsRequired.removeIf(permission::equals);
        }
        return permissionsRequired;
    }

    /**
     * @param permissionsRequired List of permissions required by a given command.
     * @param permissionsDenied Permissions denied by an override.
     * @return Required permissions if they have been denied by the override.
     */
    private static @Nonnull EnumSet<Permission> findDenyOverrides(EnumSet<Permission> permissionsRequired, EnumSet<Permission> permissionsDenied) {
        EnumSet<Permission> reqPermissionsDenied = EnumSet.noneOf(Permission.class);

        for (Permission permission : permissionsDenied) {
            if (permissionsRequired.contains(permission)) {
                reqPermissionsDenied.add(permission);
            }
        }
        return reqPermissionsDenied;
    }

    /**
     * @param eventChannel Target guild
     * @param args List of arguments
     * @return a list of target channel IDs, along with
     * two StringBuilders with arguments that were invalid.
     */
    @Nonnull default List<?> parseChannelArgument(TextChannel eventChannel, ArrayList<String> args) {

        StringBuilder
                // Channels that could not be found
                nonValid = new StringBuilder(),
                // Channels that are valid, but are not text channels
                noText = new StringBuilder();
        ArrayList<String> newArgs = new ArrayList<>();

        // if no arguments were valid just add the event channel
        // as the target channel
        if (args.isEmpty()) {
            newArgs.add(eventChannel.getId());
        } else {
            // parses arguments into usable IDs, checks if channels exist
            // up to args.size(), last channel
            for (int index = 0; index < args.size(); ++index) {

                // The argument gets parsed. If it's a mention, it gets formatted
                // into an ID through the parseMention() function.
                // All letters are removed, thus the usage of the
                // originalArgument string.
                String originalArgument = args.get(index);
                args.set(index, parseMention(args.get(index), "#"));

                // Category holder for null checking
                Category channelContainer = eventChannel.getGuild().getCategoryById(args.get(index));

                if (args.get(index).isBlank()) {
                    nonValid.append("\"").append(originalArgument).append("\" ");

                } else if (channelContainer != null) {
                    // firstly creates an immutable list of the channels in the category
                    List<TextChannel> TextChannels = channelContainer.getTextChannels();
                    // if list is empty add that it is in msg
                    if (TextChannels.isEmpty()) {
                        noText.append("<#").append(originalArgument).append("> ");
                    }
                    // iterates through every channel and adds its' id to the arg list
                    for (TextChannel it : TextChannels) {
                        newArgs.add(0, it.getId());
                    }
                }

                // removes element from arguments if it's not a valid channel ID
                else if (eventChannel.getGuild().getTextChannelById(args.get(index)) == null) {
                    nonValid.append("\"").append(originalArgument).append("\" ");
                }

                else {
                    newArgs.add(args.get(index));
                }
            }
        }

        return Arrays.asList(nonValid, noText, newArgs);
    }
}
