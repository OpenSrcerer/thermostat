package thermostat.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import thermostat.Thermostat;
import thermostat.commands.Command;
import thermostat.dispatchers.MenuDispatcher;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Class for utility functions that do not fit in any specific
 * category.
 */
public abstract class MiscellaneousFunctions {
    @Nullable
    public static Member getMemberFromCache(final @Nonnull String guildId, final @Nonnull String member) {
        Guild guild = Thermostat.thermo.getGuildCache().getElementById(guildId);
        if (guild == null) {
            return null;
        }

        List<Member> memberList;
        memberList = guild.getMembersByName(member, true);
        if (memberList.isEmpty()) {
            memberList = guild.getMembersByNickname(member, true);
        }
        if (memberList.isEmpty()) {
            memberList = guild.getMembersByEffectiveName(member, true);
        }

        if (!memberList.isEmpty()) {
            return memberList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Match a String to a TimeUnit.
     * @param c String to match.
     * @return TimeUnit that matches string.
     */
    public static TimeUnit toTimeUnit(@Nonnull final String c)
    {
        return switch (c) {
            case "s" -> TimeUnit.SECONDS;
            case "m" -> TimeUnit.MINUTES;
            case "h" -> TimeUnit.HOURS;
            case "d" -> TimeUnit.DAYS;
            default -> throw new IllegalArgumentException(String.format("%s is not a valid code [smhd]", c));
        };
    }

    /**
     * Get a numerical value depending on the
     * nullability of on/off switch lists.
     */
    public static int getMonitorValue(@Nullable final List<String> onSwitch, @Nullable final List<String> offSwitch) {
        if (onSwitch != null) {
            return 1;
        } else if (offSwitch != null) {
            return 0;
        }

        // Impossible
        throw new IllegalArgumentException("onSwitch and offSwitch were null.");
    }

    /**
     * Parses an array of Discord IDs into a String to be used in a query.
     * @param ids IDs to parse.
     * @return A parsed string, usable in a query.
     */
    public static String toQueryString(@Nonnull final List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            builder.append("'").append(id).append("'").append(", ");
        }
        String returnString = builder.toString();

        // Strip the last comma and space
        return returnString.substring(0, returnString.length() - 2);
    }

    /**
     * @return A pseudorandom identifier for a command that was run.
     */
    public static long getCommandId() {
        return System.currentTimeMillis() * ThreadLocalRandom.current().nextLong(10000);
    }

    /**
     * @return A Consumer that creates a new ReactionMenu.
     */
    public static Consumer<Message> addNewMenu(final MenuType type, final Command command) {
        return message -> {
            message.addReaction("☑").queue();
            MenuDispatcher.addMenu(type, message.getId(), command);
        };
    }

    /**
     * Calculates an average of the delay time between
     * each message.
     * @param messageTimes A list containing Discord sent message OffsetDateTimes.
     * @return A long value, with the average time.
     */
    public static long calculateAverageTime(List<OffsetDateTime> messageTimes) {
        long sum = 0;

        if (messageTimes.isEmpty())
            return 0;

        for (int index = 0; index < messageTimes.size() - 1; ++index) {
            sum += ChronoUnit.MILLIS.between(messageTimes.get(index + 1), messageTimes.get(index));
        }
        return sum / messageTimes.size();
    }
}
