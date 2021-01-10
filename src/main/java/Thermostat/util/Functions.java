package thermostat.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for static functions used by the whole bot.
 */
public abstract class Functions {

    /**
     * Logger used by this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(Functions.class);

    /**
     * A function that's used to grab IDs from Discord message mentions.
     *
     * @param mention       The mention grabbed from a message in its'
     *                      plain form.
     * @param discriminator Used to tell what type of mention it is.
     *                      In this case, it's the symbol after the "<"
     *                      sign on Discord mentions. Refer to
     *                      https://discordapp.com/developers/docs/reference
     *                      under the Message Formatting section for
     *                      each discriminator.
     * @return The ID that the mention contained.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String parseMention(String mention, String discriminator) {
        String retString = mention;

        if (mention.startsWith("<" + discriminator) && mention.endsWith(">"))
            retString = mention.substring(2, mention.length() - 1);

        if (retString.startsWith("!") || retString.startsWith("&"))
            retString = retString.substring(1);

        retString = retString.replaceAll("[^\\d]", "");

        // if the sent value is a number larger than 64 bits
        // the given ID is not a valid snowflake, therefore
        // not used.

        try {
            Long.parseUnsignedLong(retString);
        } catch (NumberFormatException ex) {
            // Returns 0 if arguments is empty due to being
            // parsed so when return value is used directly
            // in functions, they won't throw an exception
            retString = "0";
        }

        return retString;
    }

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
     * Function that converts a string slowmode argument
     * to a usable Integer one.
     *
     * @param slowmode Slowmode argument, in a string.
     * @return Parsed slowmode value.
     */
    public static Integer parseSlowmode(String slowmode) throws NumberFormatException {
        int retInteger;
        // second = 1; minute = 60; hour = 3600
        int multiplyValue = 1;

        if (slowmode.contains("m")) {
            multiplyValue = 60;
        } else if (slowmode.contains("h")) {
            multiplyValue = 3600;
        }

        slowmode = slowmode.replaceAll("[^\\d]", "");
        retInteger = Integer.parseUnsignedInt(slowmode) * multiplyValue;

        return retInteger;
    }

    /**
     * Parses a given time value into a calendar instance.
     * @param time Time argument.
     * @return A calendar with the time argument added.
     * Null if argument cannot be parsed.
     */
    @Nullable
    public static Calendar parseTime(@Nonnull final String time) throws IllegalArgumentException {
        final Pattern p = Pattern.compile("(\\d+)([hmsd])");
        final Matcher m = p.matcher(time);

        long totalMinutes = 0;

        while (m.find())
        {
            final int duration = Integer.parseInt(m.group(1));
            final TimeUnit interval = toTimeUnit(m.group(2));
            final long l = interval.toMinutes(duration);
            totalMinutes += l;
        }

        if (totalMinutes == 0) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, (int) totalMinutes);
        return calendar;
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
     * Converts a string to an int value that signifies a boolean.
     * @param value Value to convert.
     * @return Converted boolean.
     */
    public static int convertToBooleanInteger(final String value) {
        if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) ||
                "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value)) {
            return 1;
        }

        else if ("0".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) ||
                "false".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value)) {
            return 0;
        }

        else {
            return -1;
        }
    }

    /**
     * Creates database entries for guilds/channels if they do not exist.
     * @param guildId ID of the guild to check.
     * @param channelId Channel of the guild to check.
     */
    public static void checkGuildAndChannelThenSet(final String guildId, final String channelId) {
        try {
            // Check if guild and channel are in the database.
            if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = ?", guildId))
                Create.Guild(guildId);
            if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = ?", channelId))
                Create.Channel(guildId, channelId, 0);

        } catch (SQLException ex) {
            lgr.error("SQL Exception while setting guild ID: " + guildId + " and channel ID: " + channelId);
        }
    }

    public static long getCommandId() {
        return System.currentTimeMillis() * ThreadLocalRandom.current().nextLong(10000);
    }
}
