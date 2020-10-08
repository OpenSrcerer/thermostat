package thermostat.preparedStatements;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;

public abstract class DynamicEmbeds {

    public static EmbedBuilder dynamicEmbed(List<String> options, User user) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(0x00aeff);

        // Elements are grouped two by two:
        // index = Description; index + 1 = dynamic slowmode value
        for (int index = 0; index < options.size(); index += 2) {
            if (!options.get(index + 1).isEmpty()) {
                builder.addField(options.get(index), options.get(index + 1), false);
            }
        }

        builder.setTimestamp(Instant.now());
        builder.setFooter("Requested by " + user.getAsTag(), user.getAvatarUrl());

        return builder;
    }
}
