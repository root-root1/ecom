package com.social.ecom.order.services;

import com.social.ecom.order.kafka.producer.OrderProducer;
import com.social.ecom.order.model.Order;
import com.social.ecom.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer producer;

    public void createOrder(Order order) {
        producer.sendOrder(order);
        logger.info("Order sent to Kafka for this customer id: {}", order.getCustomerId());
    }

    public Order getOrderById(int orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public List<Order> getOrder() {
        return orderRepository.getOrders();
    }
}
