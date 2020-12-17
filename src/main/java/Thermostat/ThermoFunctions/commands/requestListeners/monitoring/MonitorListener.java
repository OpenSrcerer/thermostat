package thermostat.thermoFunctions.commands.requestListeners.monitoring;

import thermostat.thermoFunctions.commands.requestListeners.RequestListener;
import thermostat.thermoFunctions.entities.RequestType;

public final class MonitorListener implements RequestListener {
    public MonitorListener() {
    }

    @Override
    public void requestComplete() {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public RequestType getRequestType() {
        return RequestType.MONITOR;
    }
}
