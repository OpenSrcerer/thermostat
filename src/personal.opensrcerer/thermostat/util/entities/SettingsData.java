package thermostat.util.entities;

import okhttp3.internal.annotations.EverythingIsNonNull;

/**
 * A class that encapsulates the information necessary to be
 * sent to the user after the call of a SettingsCommand.
 * @see thermostat.commands.informational.SettingsCommand
 * @see thermostat.embeds.Embeds
 */
public class SettingsData {
    public final String channelName;
    public final int min;
    public final int max;
    public final int cachingSize;
    public final float sensitivity;
    public final boolean monitor;
    public final boolean filter;

    @EverythingIsNonNull
    public SettingsData(String channelName, int min, int max, int cachingSize,
                        float sensitivity, boolean monitor, boolean filter)
    {
        this.channelName = channelName;
        this.min = min;
        this.max = max;
        this.cachingSize = cachingSize;
        this.sensitivity = sensitivity;
        this.monitor = monitor;
        this.filter = filter;
    }
}
