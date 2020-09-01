package thermostat.thermoFunctions.monitorThreads;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkerChannel {
    private final String channelId;
    // last 25 messages
    protected final List<OffsetDateTime> messageList = new ArrayList<>();

    public WorkerChannel(String channelId) {
        this.channelId = channelId;
    }

    @Nonnull
    public String getChannelId() {
        return channelId;
    }
}
