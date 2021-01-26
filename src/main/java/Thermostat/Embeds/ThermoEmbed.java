package thermostat.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import thermostat.util.Constants;

import java.time.Instant;

/**
 * Class to organize embeds better, conforming to Thermostat's
 * standard of retaining specific information.
 */
public class ThermoEmbed extends EmbedBuilder {

    /**
     * Default color for all Embeds matching Thermo's theme.
     */
    private static final int embedColor = 0x00aeff;

    /**
     * Prefix for this specific Embed.
     */
    public final String prefix;

    /**
     * The ID of the command that called this Embed.
     */
    public String commandId;

    /**
     * Create a new ThermoEmbed with specific parameters.
     * @param prefix The prefix for the embed.
     * @param commandId The ID of the command that called the Embed.
     */
    public ThermoEmbed(String prefix, String commandId) {
        super();
        setColor(embedColor);
        setThumbnail(Constants.THERMOSTAT_AVATAR_URL);
        this.prefix = prefix;
        this.commandId = commandId;
    }

    /**
     * Create a new ThermoEmbed with specific parameters.
     * @param prefix The prefix for the embed.
     * @param ownerTag The Discord tag of the member that called this embed.
     * @param ownerAvatarUrl The Avatar URL of the member that called this embed.
     */
    public ThermoEmbed(String prefix, String ownerTag, String ownerAvatarUrl) {
        super();
        setColor(embedColor);
        setThumbnail(Constants.THERMOSTAT_AVATAR_URL);
        this.prefix = prefix;
        this.commandId = null;
        setFooter("Requested by " + ownerTag, ownerAvatarUrl);
    }


    /**
     * Create a new ThermoEmbed with specific parameters.
     * @param prefix The prefix for the embed.
     * @param commandId The ID of the command that called the Embed.
     * @param ownerTag The Discord tag of the member that called this embed.
     * @param ownerAvatarUrl The Avatar URL of the member that called this embed.
     */
    public ThermoEmbed(String prefix, String commandId, String ownerTag, String ownerAvatarUrl) {
        super();
        setColor(embedColor);
        setThumbnail(Constants.THERMOSTAT_AVATAR_URL);
        this.prefix = prefix;
        this.commandId = commandId;
        setTimestamp(Instant.now());
        setFooter("Requested by " + ownerTag + "\nCommand ID: " + commandId, ownerAvatarUrl);
    }

    /**
     * Set the Command ID of the embed.
     * @param commandId The Command ID to set.
     */
    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }
}
