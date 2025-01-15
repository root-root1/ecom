package com.social.ecom.order.services;

import com.social.ecom.order.kafka.OrderProducer;
import com.social.ecom.order.model.Order;
import com.social.ecom.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer producer;

    public void createOrder(Order order) {
        order.setStatus("Created");
        producer.sendOrder(order);
        orderRepository.saveOrder(order);
    }

    public Order getOrderById(int orderId) {
        return orderRepository.getOrderById(orderId);
    }
}
