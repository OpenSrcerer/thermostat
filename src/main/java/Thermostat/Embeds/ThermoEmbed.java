package thermostat.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import thermostat.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public final String commandId;


    /**
     * Create a new ThermoEmbed with specific parameters.
     * @param prefix The prefix for the embed.
     * @param commandId The ID of the command that called the Embed.
     * @param ownerTag The Discord tag of the member that called this embed.
     * @param ownerAvatarUrl The Avatar URL of the member that called this embed.
     */
    public ThermoEmbed(@Nonnull final String prefix, @Nonnull final String commandId,
                       @Nullable final String ownerTag, @Nullable final String ownerAvatarUrl) {
        super();
        this.prefix = prefix;
        this.commandId = commandId;

        setColor(embedColor);
        setThumbnail(Constants.THERMOSTAT_AVATAR_URL);

        String footerLine;
        if (ownerTag == null) {
            footerLine = "Command ID: " + commandId;
        } else {
            footerLine = "Requested by " + ownerTag + "\nCommand ID: " + commandId;
        }

        setTimestamp(Instant.now());

        if (ownerAvatarUrl == null) {
            setFooter(footerLine, Constants.THERMOSTAT_AVATAR_URL);
        } else {
            setFooter(footerLine, ownerAvatarUrl);
        }
    }
}
