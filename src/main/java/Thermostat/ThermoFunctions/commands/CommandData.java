package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class CommandData {
    private final Guild guild;
    private final TextChannel channel;
    private final Member member;
    private final String prefix;
    private ArrayList<String> arguments;
    private final Message message;

    public CommandData(Guild guild, TextChannel channel, Member member, String prefix, ArrayList<String> arguments, Message message) {
        this.guild = guild;
        this.channel = channel;
        this.member = member;
        this.prefix = prefix;
        this.arguments = arguments;
        this.message = message;
    }

    public @Nonnull Guild guild() {
        return guild;
    }

    public @Nonnull TextChannel channel() {
        return channel;
    }

    public @Nonnull Member member() {
        return member;
    }

    public @Nonnull String prefix() {
        return prefix;
    }

    public @Nonnull ArrayList<String> arguments() {
        return arguments;
    }

    public @Nonnull Message message() {
        return message;
    }

    public void replaceArguments(ArrayList<String> list) {
        this.arguments = list;
    }
}
