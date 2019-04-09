package com.jefferson.regah.notification;

public interface NotificationSender {
    boolean sendMessageTo(String subscriptionId, String message);

    NotificationSender NULL_OBJECT = (x, y) -> true;
}
