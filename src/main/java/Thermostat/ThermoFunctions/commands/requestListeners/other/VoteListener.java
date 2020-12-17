package thermostat.thermoFunctions.commands.requestListeners.other;

import net.dv8tion.jda.api.entities.MessageEmbed;
import thermostat.thermoFunctions.commands.requestListeners.RequestListener;
import thermostat.thermoFunctions.entities.RequestType;

public final class VoteListener implements RequestListener {
    public VoteListener() {
    }

    @Override
    public void requestComplete() {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public RequestType getRequestType() {
        return RequestType.VOTE;
    }
}
