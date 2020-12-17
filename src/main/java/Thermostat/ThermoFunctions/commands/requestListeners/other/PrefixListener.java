package thermostat.thermoFunctions.commands.requestListeners.other;

import thermostat.thermoFunctions.commands.requestListeners.RequestListener;
import thermostat.thermoFunctions.entities.RequestType;

public final class PrefixListener implements RequestListener {
    public PrefixListener() {
    }

    @Override
    public void requestComplete() {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public RequestType getRequestType() {
        return RequestType.PREFIX;
    }
}
