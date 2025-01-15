package com.social.ecom.order.kafka.consumer;

import com.social.ecom.order.avro.OrderCreate;
import com.social.ecom.order.avro.OrderItem;
import com.social.ecom.order.model.Order;
import com.social.ecom.order.repository.OrderRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<Integer, byte[]> kafkaTemplate;

    @KafkaListener(
            topics = "orders",
            groupId = "order-processing-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrder(ConsumerRecord<Integer, byte[]> record, Acknowledgment ack) {
        Integer orderId = record.key();
        logger.info("Received order message: {}", orderId);
        try{
            OrderCreate createOrder = decodeOrderRecord(record.value());
            Order order = new Order();

            List<Order.OrderItem> items = createOrderItem(createOrder);

            order.setOrderId(createOrder.getOrderId());
            order.setOrderDate(createOrder.getOrderDate());
            order.setStatus(createOrder.getStatus());
            order.setTotalAmount(createOrder.getTotalAmount());
            order.setCustomerId(createOrder.getCustomerId());
            order.setItems(items);

            orderRepository.saveOrder(order);

            // Produce to next topic if needed
            if ("PENDING".equals(order.getStatus())) {
            // produceToPaymentTopic(order);
            }

            // Manual acknowledgment
            // ack.acknowledge();

            logger.info("Successfully processed order: {}", orderId);

        }catch (Exception e) {
            logger.error("Error processing order: {}", orderId, e);
            // Handle error - could be retry or DLQ
            handleProcessingError(record, e);
        }
    }

    private void handleProcessingError(
            ConsumerRecord<Integer, byte[]> record,
            Exception e) {
        // Implement retry logic or send to DLQ
        String dlqTopic = "ecom.orders.dlq";
        try {
            ProducerRecord<Integer, byte[]> dlqRecord =
                    new ProducerRecord<>(dlqTopic, record.key(), record.value());
            // Add error information in headers
            dlqRecord.headers().add("error", e.getMessage().getBytes());
            dlqRecord.headers().add("timestamp",
                    String.valueOf(System.currentTimeMillis()).getBytes());

            kafkaTemplate.send(dlqRecord);
            logger.info("Message sent to DLQ: {}", record.key());
        } catch (Exception dlqEx) {
            logger.error("Failed to send to DLQ: {}", record.key(), dlqEx);
        }
    }

    private static OrderCreate decodeOrderRecord(byte[] order) throws IOException {
        return OrderCreate.fromByteBuffer(ByteBuffer.wrap(order));
    }

    private static ArrayList<Order.OrderItem> createOrderItem(OrderCreate order) {
        ArrayList<Order.OrderItem> avroOrderItems = new ArrayList<>();

        List<OrderItem> items = order.getItems();
        for (OrderItem item : items) {
            Order.OrderItem OrderItem = new Order.OrderItem();

            OrderItem.setProductId(item.getProductId());
            OrderItem.setPrice(item.getPrice());
            OrderItem.setQuantity(item.getQuantity());

            avroOrderItems.add(OrderItem);
        }
        return avroOrderItems;
    }
}
