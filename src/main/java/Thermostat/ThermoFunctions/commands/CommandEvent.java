package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseMention;

public interface CommandEvent {

    void checkPermissions();

    // boolean checkFormat();

    void execute();

    /**
     * @param permissionsToSeek Permissions required by the command.
     * @param memberPermsList Permissions that the Member has.
     * @return Permissions that are needed but not assigned to a Member.
     */
    default @NotNull EnumSet<Permission> findMissingPermissions(EnumSet<Permission> permissionsToSeek, EnumSet<Permission> memberPermsList) {
        memberPermsList.forEach(permissionsToSeek::remove);
        return permissionsToSeek;
    }

    /**
     * @param eventChannel Target guild
     * @param args List of arguments
     * @return a list of target channel IDs, along with
     * two stringbuilders with arguments that were invalid.
     */
    @Nonnull
    default List<?> parseChannelArgument(TextChannel eventChannel, ArrayList<String> args) {

        StringBuilder
                // Channels that could not be found
                nonValid = new StringBuilder(),
                // Channels that are valid, but are not text channels
                noText = new StringBuilder();

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
                args.remove(index);
                --index;

            } else if (channelContainer != null) {
                // firstly creates an immutable list of the channels in the category
                List<TextChannel> TextChannels = channelContainer.getTextChannels();
                // if list is empty add that it is in msg
                if (TextChannels.isEmpty()) {
                    noText.append("<#").append(originalArgument).append("> ");
                }
                // removes category ID from argument ArrayList
                args.remove(index);
                // iterates through every channel and adds its' id to the arg list
                for (TextChannel it : TextChannels) {
                    args.add(0, it.getId());
                }
                --index;
            }

            // removes element from arguments if it's not a valid channel ID
            else if (eventChannel.getGuild().getTextChannelById(args.get(index)) == null) {
                nonValid.append("\"").append(originalArgument).append("\" ");
                args.remove(index);
                --index;
            }
        }

        // if no arguments were valid just add the event channel
        // as the target channel
        if (args.isEmpty()) {
            args.add(eventChannel.getId());
        }

        return Arrays.asList(nonValid, noText, args);
    }
}
