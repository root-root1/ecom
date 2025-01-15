package com.social.ecom.order.kafka;

import com.social.ecom.common.utils.Utils;
import com.social.ecom.order.model.Order;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);
    private static final String TOPIC = "order";

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private Utils utils;

    public void sendOrder(Order order) {
        try {
            Order orderEventValue = buildOrder(order);
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(TOPIC, order.getOrderId(), );

            kafkaTemplate.send(record).whenComplete((sendResult, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send order with key {}: {}", order.getOrderId(), exception.getMessage(), exception);
                } else {
                    logger.info("Order sent successfully to topic {} with key {}. Partition: {}, Offset: {}",
                            TOPIC, order.getOrderId(),
                            sendResult.getRecordMetadata().partition(),
                            sendResult.getRecordMetadata().offset());
                }
            });

        } catch (IOException e) {
            logger.error("Error serializing the order: {}", e.getMessage(), e);
        }
    }
    private Order buildOrder(Order order) {
        return new CreateOrder
    }
}