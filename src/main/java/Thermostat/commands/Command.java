package thermostat.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import thermostat.Messages;
import thermostat.Thermostat;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.mySQL.PreparedActions;
import thermostat.mySQL.DataSource;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static thermostat.util.Functions.parseMention;
import static thermostat.util.PermissionComputer.getMissingPermissions;

public interface Command extends Runnable {

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
     * Gets the specific Command ID of a Command.
     * @return Command's ID
     */
    long getId();

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
            PreparedActions.createChannel(guildId, channelId, 0);
        }
    }

    default void addIfNotInDb(String guildId, List<String> channelIds) throws SQLException {
        for (String channelId : channelIds) {
            if (!DataSource.checkDatabaseForData("SELECT CHANNEL_ID FROM CHANNELS JOIN GUILDS ON " +
                    "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) WHERE CHANNEL_ID = ?", channelId)) {
                PreparedActions.createChannel(guildId, channelId, 0);
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
                .retrieveMember(Thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get member and thermostat's missing permissions, if applicable
                            EnumSet<Permission>
                                    missingMemberPerms = getMissingPermissions(commandEvent.getMember(), commandEvent.getChannel(), command.getType().getMemberPerms()),
                                    missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(), command.getType().getThermoPerms());

                            if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
                                CommandDispatcher.queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() + "/" + commandEvent.getGuild().getId() + "):" +
                                        " " + missingThermostatPerms.toString() + " " + missingMemberPerms.toString() + "");
                                Messages.sendMessage(commandEvent.getChannel(), ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms, command.getId()));
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
                .retrieveMember(Thermostat.thermo.getSelfUser())
                .queue(
                        thermostat -> {
                            // Get Thermostat's missing permissions, if applicable
                            EnumSet<Permission> missingThermostatPerms = getMissingPermissions(thermostat, commandEvent.getChannel(), command.getType().getThermoPerms());

                            if (missingThermostatPerms.isEmpty()) {
                                CommandDispatcher.queueCommand(command);
                            } else {
                                command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() + "/" + commandEvent.getGuild().getId() + "):" +
                                        " " + missingThermostatPerms.toString() + "");
                                Messages.sendMessage(commandEvent.getChannel(), ErrorEmbeds.errPermission(missingThermostatPerms));
                            }
                        }
                );
    }

    /**
     * A class that encapsulates returning values
     * for parseChannelArgument();
     */
    class Arguments {
        public final StringBuilder nonValid;
        public final StringBuilder noText;
        public final ArrayList<String> newArguments;

        public Arguments(@Nonnull StringBuilder nonValid, @Nonnull StringBuilder noText, @Nonnull ArrayList<String> newArguments) {
            this.nonValid = nonValid;
            this.noText = noText;
            this.newArguments = newArguments;
        }
    }

    /**
     * @param eventChannel Target guild
     * @param rawChannels List of arguments
     * @return a list of target channel IDs, along with
     * two StringBuilders with arguments that were invalid.
     */
    @Nonnull default Arguments parseChannelArgument(TextChannel eventChannel, List<String> rawChannels) {
        StringBuilder
                // Channels that could not be found
                nonValid = new StringBuilder(),
                // Channels that are valid, but are not text channels
                noText = new StringBuilder();
        ArrayList<String> newArgs = new ArrayList<>();

        // if no arguments were valid just add the event channel
        // as the target channel
        if (rawChannels.isEmpty()) {
            newArgs.add(eventChannel.getId());
        } else {
            // parses arguments into usable IDs, checks if channels exist
            // up to args.size(), last channel
            for (int index = 0; index < rawChannels.size(); ++index) {

                // The argument gets parsed. If it's a mention, it gets formatted
                // into an ID through the parseMention() function.
                // All letters are removed, thus the usage of the
                // originalArgument string.
                String originalArgument = rawChannels.get(index);
                rawChannels.set(index, parseMention(rawChannels.get(index), "#"));

                // Category holder for null checking
                Category channelContainer = eventChannel.getGuild().getCategoryById(rawChannels.get(index));

                if (rawChannels.get(index).isBlank()) {
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
                else if (eventChannel.getGuild().getTextChannelById(rawChannels.get(index)) == null) {
                    nonValid.append("\"").append(originalArgument).append("\" ");
                } else {
                    newArgs.add(rawChannels.get(index));
                }
            }
        }

        return new Arguments(nonValid, noText, newArgs);
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
