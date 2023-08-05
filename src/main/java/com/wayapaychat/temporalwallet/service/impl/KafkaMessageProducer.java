package com.wayapaychat.temporalwallet.service.impl;



import com.google.gson.Gson;
import com.wayapaychat.temporalwallet.service.MessageQueueProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class KafkaMessageProducer implements MessageQueueProducer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> template;
    private final Gson gson;

    @Autowired
    public KafkaMessageProducer(KafkaTemplate<String, Object> template, Gson gson) {
        this.template = template;
        this.gson = gson;
    }


    /**
     * Non Blocking (Async), sends data to kafka
     *
     * @param topic, topic
     * @param event, event
     */
    @Override
    public void send(String topic, Object event) {
        logger.info("Event:: " + event);

        ListenableFuture<SendResult<String, Object>> future = template.send(topic, gson.toJson(event));
        //future.addCallback(new KafkaSendCallback<>() {
        future.addCallback(new ListenableFutureCallback<>() {
            /**
             * Called when the {@link ListenableFuture} completes with success.
             * <p>Note that Exceptions raised by this method are ignored.
             *
             * @param result the result
             */
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                //persist in app event as a successful event
                logger.info("Success:: notification sent to the event queue {}",result);
            }

            /**
             * Called when the send fails.
             *
             * @param ex the exception.
             */
            /*@Override
            public void onFailure(KafkaProducerException ex) {
                //persist in app event as a failed even
            	logger.error("Unable to send message=["
                        + data + "] due to : " + ex.getMessage());
                logger.error("failed to send notification", ex);
            }*/
            @Override
            public void onFailure(Throwable ex) {
                //persist in app event as a failed even
                logger.error("Unable to send message=["
                        + event + "] due to :", ex.getMessage());
                logger.error("Full Error:", ex);

            }
        });
    }
}
