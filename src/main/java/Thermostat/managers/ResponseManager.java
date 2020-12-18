package thermostat.managers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.Command;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.function.Consumer;

public final class ResponseManager {
    public static void commandSucceeded(@Nonnull Command command, @Nonnull EmbedBuilder embed) {
        command.getLogger().info("Command with ID [" + command.getId() + "] was successful.");
        Messages.sendMessage(command.getEvent().getChannel(), embed);
    }

    public static void commandSucceeded(@Nonnull Command command, @Nonnull EmbedBuilder embed, @Nonnull InputStream inputStream) {
        command.getLogger().info("Command with ID [" + command.getId() + "] was successful.");
        Messages.sendMessage(command.getEvent().getChannel(), inputStream, embed);
    }
    public static void commandSucceeded(@Nonnull Command command,  @Nonnull EmbedBuilder embed, @Nonnull Consumer<Message> consumer) {
        command.getLogger().info("Command with ID [" + command.getId() + "] was successful.");
        Messages.sendMessage(command.getEvent().getChannel(), embed, consumer);
    }

    public static void commandFailed(@Nonnull Command command, @Nonnull EmbedBuilder embed, @Nonnull String reason) {
        command.getLogger().info("Command with ID [" + command.getId() + "] has failed. Reason:\n" + reason);
        Messages.sendMessage(command.getEvent().getChannel(), embed);
    }

    public static void commandFailed(@Nonnull Command command, @Nonnull EmbedBuilder embed, @Nonnull Throwable throwable) {
        command.getLogger().info("Command with ID [" + command.getId() + "] has failed. Details:\n");
        throwable.printStackTrace();
        Messages.sendMessage(command.getEvent().getChannel(), embed);
    }
}
