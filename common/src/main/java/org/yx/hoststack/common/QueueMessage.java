package org.yx.hoststack.common;

public class QueueMessage<T> {
    String messageType;
    T message;

    public QueueMessage(String messageType, T t) {
        this.messageType = messageType;
        this.message = t;
    }
}
