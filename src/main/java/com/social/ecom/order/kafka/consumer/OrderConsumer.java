package com.social.ecom.order.kafka.consumer;

import com.social.ecom.order.avro.OrderCreate;
import com.social.ecom.order.avro.OrderItem;
import com.social.ecom.order.model.Order;
import com.social.ecom.order.repository.OrderRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.social.ecom.common.utils.Utils;

@Component
public class OrderConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @KafkaListener(
            topics = "order",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrder(ConsumerRecord<String, byte[]> record) {
        String customerId = record.key();
        logger.info("Received order message: {}", customerId);
        try {
            OrderCreate createOrder = decodeOrderRecord(record.value());
            Order order = new Order();

            List<Order.OrderItem> items = createOrderItem(createOrder);
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

            logger.info("Successfully processed order: {}", customerId);

        } catch (Exception e) {
            logger.error("Error processing order: {}", customerId, e);
            // Handle error - could be retry or DLQ
            Utils.HandleProcessingError(record, e, kafkaTemplate, logger);
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
