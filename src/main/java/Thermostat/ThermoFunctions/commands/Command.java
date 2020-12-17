package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import thermostat.managers.CommandManager;
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

public interface Command extends Runnable {

    // boolean checkFormat();

    /**
     * Execute the Command's Discord-side code.
     */
    void run();

    /**
     * Return the specific information
     * about this Command.
     * @return Event with specific case info.
     */
    GuildMessageReceivedEvent getEvent();

    /**
     * Get CommandType of Command.
     * @return type of Command.
     */
    CommandType getType();

    /**
     * Get specific Logger of Command.
     * @return Command's Logger
     */
    Logger getLogger();

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
     * Adds a given Command to the Request Manager queue if
     * permission conditions are met.
     * @param command Command to add to queue.
     */
    @SuppressWarnings("ConstantConditions")
    default void checkPermissionsAndQueue(@Nonnull Command command) {
        GuildMessageReceivedEvent commandEvent = command.getEvent();

        // check main permissions
        commandEvent.getGuild()
                .retrieveMember(thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get member and thermostat's missing permissions, if applicable
                            EnumSet<Permission>
                                    missingMemberPerms = getMissingPermissions(commandEvent.getMember(), commandEvent.getChannel(), command.getType().getMemberPerms()),
                                    missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(), command.getType().getThermoPerms());

                            if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
                                CommandManager.queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() + "/" + commandEvent.getGuild().getId() + "):" +
                                        " [" + missingThermostatPerms.toString() + "] [" + missingMemberPerms.toString() + "]");
                                Messages.sendMessage(commandEvent.getChannel(), ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
                            }
                        }
                );
    }

    /**
     * Adds a Command only related to Thermostat to the
     * Command Manager queue.
     * @param command Command to add to queue.
     */
    default void checkThermoPermissionsAndQueue(@Nonnull Command command) {
        GuildMessageReceivedEvent commandEvent = command.getEvent();

        commandEvent.getGuild()
                .retrieveMember(thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get Thermostat's missing permissions, if applicable
                            EnumSet<Permission> missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(), command.getType().getThermoPerms());

                            if (missingThermostatPerms.isEmpty()) {
                                CommandManager.queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getChannel().getGuild().getName() + "/" + commandEvent.getChannel().getGuild().getId() + "):" +
                                        " [" + missingThermostatPerms.toString() + "]");
                                Messages.sendMessage(commandEvent.getChannel(), ErrorEmbeds.errPermission(missingThermostatPerms));
                            }
                        }
                );
    }

    /**
     * @param member Member to check permissions for
     * @param eventChannel Channel to retrieve overrides from
     * @param requiredPermissions List of permissions that will be checked if member has
     * @return EnumSet of missing permissions
     */
    default @Nonnull EnumSet<Permission> getMissingPermissions(@Nonnull Member member, @Nonnull TextChannel eventChannel, @Nonnull EnumSet<Permission> requiredPermissions) {
        EnumSet<Permission> missingPermissions = EnumSet.noneOf(Permission.class);
        long memberPermissions = computePermissions(member, eventChannel);

        // check if each permission is contained in the permissions long
        for (Permission permission : requiredPermissions) {
            if ((memberPermissions & permission.getRawValue()) != permission.getRawValue()) {
                missingPermissions.add(permission);
            }
        }

        return missingPermissions;
    }

    /**
     * @param member Member to compute permissions for
     * @param channel Channels to get overrides from
     * @return Raw permissions long for given member
     */
    default long computePermissions(@Nonnull Member member, @Nonnull TextChannel channel) {
        return computeOverrides(computeBasePermissions(member), member, channel);
    }

    /**
     * @param member Calculates base permissions based on the
     *               permissions given to @everyone and member's roles
     * @return long with general permissions
     */
    default long computeBasePermissions(@Nonnull Member member) {
        if (member.isOwner()) return Permission.ALL_PERMISSIONS;

        long everyonePermissions = member.getGuild().getPublicRole().getPermissionsRaw();

        for (Role role : member.getRoles()) {
            everyonePermissions |= role.getPermissionsRaw();
        }

        if ((everyonePermissions & Permission.ADMINISTRATOR.getRawValue()) == Permission.ADMINISTRATOR.getRawValue()) {
            return Permission.ALL_PERMISSIONS;
        }

        return everyonePermissions;
    }

    /**
     * @param basePermissions base permissions, see computeBasePermissions
     * @param member Member to compute permissions for
     * @param channel Channel to retrieve overrides from
     * @return permissions long with computed base & channel specific overrides
     */
    default long computeOverrides(long basePermissions, @Nonnull Member member, @Nonnull TextChannel channel) {

        // Administrator overrides everything, so just return that.
        if ((basePermissions & Permission.ADMINISTRATOR.getRawValue()) == Permission.ADMINISTRATOR.getRawValue()) {
            return Permission.ALL_PERMISSIONS;
        }

        long permissions = basePermissions;

        // Everyone overrides
        {
            // Get the @everyone role override for the channel
            PermissionOverride everyoneOverride = channel.getPermissionOverride(channel.getGuild().getPublicRole());

            if (everyoneOverride != null) {
                // pass permissions the given denied and allowed permissions
                permissions &= ~everyoneOverride.getDeniedRaw();
                permissions |= everyoneOverride.getAllowedRaw();
            }
        }

        // Role Overrides
        {
            long allowed = 0, denied = 0;

            // for every role add allowed and denied permissions
            for (Role role : member.getRoles()) {
                PermissionOverride roleOverride = channel.getPermissionOverride(role);

                if (roleOverride != null) {
                    allowed |= roleOverride.getAllowedRaw();
                    denied |= roleOverride.getDeniedRaw();
                }
            }

            permissions &= ~denied;
            permissions |= allowed;
        }

        // Add member specific override
        {
            PermissionOverride memberOverride = channel.getPermissionOverride(member);

            if (memberOverride != null) {
                permissions &= ~memberOverride.getDeniedRaw();
                permissions |= memberOverride.getAllowedRaw();
            }
        }

        return permissions;
    }

    /**
     * @param eventChannel Target guild
     * @param args List of arguments
     * @return a list of target channel IDs, along with
     * two StringBuilders with arguments that were invalid.
     */
    @Nonnull default List<?> parseChannelArgument(TextChannel eventChannel, List<String> args) {

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

    /**
     * Checks if GuildMessageReceivedEvent is valid.
     * An event is valid when its Member object is not null.
     * @return true is event is valid, false otherwise.
     */
    default boolean validateEvent(GuildMessageReceivedEvent event) {
        if (event.getMember() == null)
            return false;

        return !event.getMember().getUser().isBot();
    }
}
