package com.social.ecom.order.kafka.producer;

import com.social.ecom.common.utils.Utils;
import com.social.ecom.order.avro.OrderCreate;
import com.social.ecom.order.model.Order.OrderItem;
import com.social.ecom.order.model.Order;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);
    private static final String TOPIC = "order";

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendOrder(Order order) {
        OrderCreate orderValue = buildOrder(order);
        byte[] orderEventValue = Utils.serializeAvroObject(orderValue, OrderCreate.class,logger);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(TOPIC,Integer.toString(order.getCustomerId()), orderEventValue);

        record.headers().add("source", "order-service".getBytes());
        record.headers().add("timestamp",
                String.valueOf(System.currentTimeMillis()).getBytes());

        kafkaTemplate.send(record).whenComplete((sendResult, exception) -> {
            if (exception != null) {
                logger.error("Failed to send order with key {}: {}", order.getCustomerId(), exception.getMessage(), exception);
            } else {
                logger.info("Order sent successfully to topic {} with key {}. Partition: {}, Offset: {}",
                        TOPIC, order.getCustomerId(),
                        sendResult.getRecordMetadata().partition(),
                        sendResult.getRecordMetadata().offset());
            }
        });

    }

    private OrderCreate buildOrder(Order order) {
        ArrayList<com.social.ecom.order.avro.OrderItem> avroOrderItems = getOrderItems(order);
        return OrderCreate.newBuilder()
                .setCustomerId(order.getCustomerId())
                .setItems(avroOrderItems)
                .setTotalAmount(((float) order.getTotalAmount()))
                .build();
    }

    private static ArrayList<com.social.ecom.order.avro.OrderItem> getOrderItems(Order order) {
        ArrayList<com.social.ecom.order.avro.OrderItem> avroOrderItems = new ArrayList<>();

        List<OrderItem> items = order.getItems();
        for (OrderItem item : items) {
            com.social.ecom.order.avro.OrderItem avroOrderItem = new com.social.ecom.order.avro.OrderItem();

            avroOrderItem.setProductId(item.getProductId());
            avroOrderItem.setPrice(((float) item.getPrice()));
            avroOrderItem.setQuantity(item.getQuantity());

            avroOrderItems.add(avroOrderItem);
        }
        return avroOrderItems;
    }
}