package Thermostat.ThermoFunctions;

/**
 *
 */
public abstract class Functions {
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
    public static String parseMention(String mention, String discriminator) {
        if (mention == null) {
            throw new NullPointerException();
        }

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
            retString = "";
        }

        return retString;
    }

    /**
     * Function that converts a string slowmode argument
     * to a usable Integer one.
     * @param slowmode Slowmode argument, in a string.
     * @return Parsed slowmode value.
     */
    public static Integer parseSlowmode(String slowmode) throws NumberFormatException {
        Integer retInteger;
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
}
