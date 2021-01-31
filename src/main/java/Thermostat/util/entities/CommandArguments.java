package thermostat.util.entities;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * A class that encapsulates returning values
 * for parseChannelArgument();
 */
public class CommandArguments {
    public final StringBuilder nonValid;
    public final StringBuilder noText;
    public final ArrayList<String> channels;

    public CommandArguments(@Nonnull StringBuilder nonValid, @Nonnull StringBuilder noText, @Nonnull ArrayList<String> channels) {
        this.nonValid = nonValid;
        this.noText = noText;
        this.channels = channels;
    }
}
