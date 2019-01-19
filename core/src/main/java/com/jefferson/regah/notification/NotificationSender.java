package com.jefferson.regah.notification;

public class NotificationSender {
    private final String subscriberId;
    private final NotificationBus notificationBus;

    public NotificationSender(String subscriberId) {
        this(subscriberId, NotificationBus.getInstance());
    }

    private NotificationSender(String subscriberId, NotificationBus notificationBus) {
        this.subscriberId = subscriberId;
        this.notificationBus = notificationBus;
    }

    public boolean sendMessage(final String message) {
        return notificationBus.sendMessageTo(subscriberId, message);
    }

    public static final NotificationSender NULL_SENDER = new NotificationSender("NONE") {
        @Override
        public boolean sendMessage(String message) {
            return false;
        }
    };
}
