package thermostat.thermoFunctions.commands.requestListeners.informational;

import thermostat.thermoFunctions.commands.requestListeners.RequestListener;
import thermostat.thermoFunctions.entities.RequestType;

public final class SettingsListener implements RequestListener {
    public SettingsListener() {
    }

    @Override
    public void requestComplete() {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public RequestType getRequestType() {
        return RequestType.SETTINGS;
    }
}
