package thermostat.thermoFunctions.commands.requestListeners.monitoring;

import thermostat.thermoFunctions.commands.requestListeners.RequestListener;
import thermostat.thermoFunctions.entities.RequestType;

public final class SetBoundsListener implements RequestListener {
    public SetBoundsListener() {
    }

    @Override
    public void requestComplete() {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public RequestType getRequestType() {
        return RequestType.SETBOUNDS;
    }
}
