package thermostat.util.entities;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * A class that encapsulates returning values
 * for parseChannelArgument();
 */
public class Arguments {
    public final StringBuilder nonValid;
    public final StringBuilder noText;
    public final ArrayList<String> newArguments;

    public Arguments(@Nonnull StringBuilder nonValid, @Nonnull StringBuilder noText, @Nonnull ArrayList<String> newArguments) {
        this.nonValid = nonValid;
        this.noText = noText;
        this.newArguments = newArguments;
    }
}
