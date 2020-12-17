package thermostat.thermoFunctions.commands.requestListeners.monitoring;

import thermostat.thermoFunctions.commands.requestListeners.RequestListener;
import thermostat.thermoFunctions.entities.RequestType;

public final class SensitivityListener implements RequestListener {
    public SensitivityListener() {
    }

    @Override
    public void requestComplete() {

    }

    @Override
    public void requestFailed() {

    }

    @Override
    public RequestType getRequestType() {
        return RequestType.SENSITIVITY;
    }
}
