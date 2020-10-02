package thermostat.preparedStatements;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;

public abstract class DynamicEmbeds {

    public static EmbedBuilder dynamicEmbed(List<String> options, User user) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(0xffff00);

        // Elements are grouped two by two:
        // index = Description; index + i = dynamic slowmode value
        for (int index = 0; index < options.size(); index += 2) {
            if (!options.get(1).isEmpty()) {
                builder.addField(options.get(0), options.get(1), false);
            }
        }

        builder.setTimestamp(Instant.now());
        builder.setFooter("Requested by " + user.getAsTag(), user.getAvatarUrl());

        return builder;
    }
}
