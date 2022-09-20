package com.wayapaychat.temporalwallet.service;

public interface MessageQueueProducer {
    void send(String topic, Object data);
}
