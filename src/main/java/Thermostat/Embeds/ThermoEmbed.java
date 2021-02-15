package thermostat.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import thermostat.util.Constants;
import thermostat.util.entities.CommandData;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Class to organize embeds better, conforming to Thermostat's
 * standard of retaining specific information.
 */
public class ThermoEmbed extends EmbedBuilder {

    /**
     * Default color for all Embeds matching Thermo's theme.
     */
    private static final int embedColor = 0x2f3136;

    /**
     * Create a new ThermoEmbed with basic parameters.
     */
    public ThermoEmbed() {
        super();

        setColor(embedColor);
        setTimestamp(Instant.now());
    }

    /**
     * Create a new ThermoEmbed with parameters originating from a Command.
     * @param data The parameters from a command.
     */
    @SuppressWarnings("ConstantConditions")
    public ThermoEmbed(@Nonnull final CommandData data) {
        super();

        setColor(embedColor);

        final String footerLine;
        if (data.event.getMember().getUser() == null) {
            footerLine = "🡆 Command ID: " + data.commandId;
        } else {
            footerLine = "🡆 Requested by " + data.event.getMember().getUser().getAsTag() +
                    "\n🡆 Command ID: " + data.commandId;
        }

        setTimestamp(Instant.now());
        if (data.event.getMember().getUser().getAvatarUrl() == null) {
            setFooter(footerLine, Constants.THERMOSTAT_AVATAR_URL);
        } else {
            setFooter(footerLine, data.event.getMember().getUser().getAvatarUrl());
        }
    }
}
