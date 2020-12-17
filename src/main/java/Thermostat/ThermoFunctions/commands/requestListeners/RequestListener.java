package thermostat.thermoFunctions.commands.requestListeners;

import net.dv8tion.jda.api.entities.MessageEmbed;
import thermostat.thermoFunctions.entities.RequestType;

/**
 * Used by every class in requestListeners to handle
 * failed and complete requests in a distinct way.
 */
public interface RequestListener {
    void requestComplete();

    void requestFailed();

    RequestType getRequestType();
}
