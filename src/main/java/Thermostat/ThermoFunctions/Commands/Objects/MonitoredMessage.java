package Thermostat.ThermoFunctions.Commands.Objects;

import Thermostat.Embeds;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.concurrent.*;

public class MonitoredMessage {
    private String messageId;
    private String messageOwner;
    private ScheduledFuture<?> destructionTimer = null;
    private MenuType menu;

    // Arraylist for all the currently monitored messages
    // associated with events
    public static ArrayList<MonitoredMessage> monitoredMessages = new ArrayList<>();
    // executorservice that handles timed destruction
    // of messages from monitored message arrays
    public static ScheduledExecutorService executorService;

    static {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public MonitoredMessage(String messageId, String messageOwner, MenuType menu) {
        this.messageId = messageId;
        this.messageOwner = messageOwner;
        this.menu = menu;
    }

    public void setMenuType(MenuType menu) {
        this.menu = menu;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageOwner() {
        return messageOwner;
    }

    public MenuType getMenuType() {
        return menu;
    }

    public void resetDestructionTimer(TextChannel channel) {

        if (destructionTimer != null) {
            this.destructionTimer.cancel(true);
        }
        this.destructionTimer = executorService.schedule(() -> {
            Messages.deleteMessage(channel, this.messageId);
            monitoredMessages.remove(this);
            if (this.getMenuType() == MenuType.UNMONITORALL) {
                Messages.sendMessage(channel, Embeds.missedPrompt());
            }
        }, 100, TimeUnit.SECONDS);
    }

    public void invalidate() {
        this.destructionTimer.cancel(true);
    }
}