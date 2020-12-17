package thermostat.thermoFunctions.commands.requestListeners.informational;

import thermostat.thermoFunctions.commands.requestListeners.RequestListener;
import thermostat.thermoFunctions.entities.RequestType;

public final class GetMonitorListener implements RequestListener {
    public GetMonitorListener() {
    }

    @Override
    public void requestComplete() {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public RequestType getRequestType() {
        return RequestType.GETMONITOR;
    }
}
