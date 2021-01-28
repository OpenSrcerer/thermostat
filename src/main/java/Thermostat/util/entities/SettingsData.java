package thermostat.util.entities;

import okhttp3.internal.annotations.EverythingIsNonNull;

public class SettingsData {
    public final String channelName;
    public final int min;
    public final int max;
    public final float sensitivity;
    public final boolean monitor;
    public final boolean filter;

    @EverythingIsNonNull
    public SettingsData(final String channelName, final int min, final int max,
                        final float sensitivity, final boolean monitor, final boolean filter) {
        this.channelName = channelName;
        this.min = min;
        this.max = max;
        this.sensitivity = sensitivity;
        this.monitor = monitor;
        this.filter = filter;
    }
}
