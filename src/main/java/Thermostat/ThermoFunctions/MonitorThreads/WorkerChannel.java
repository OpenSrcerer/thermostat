package Thermostat.ThermoFunctions.MonitorThreads;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkerChannel {
    protected String channelId;
    // last 25 messages
    protected List<OffsetDateTime> messageList = new ArrayList<>();

    public WorkerChannel(String channelId) {
        this.channelId = channelId;
    }
}
