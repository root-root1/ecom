package com.social.ecom.order.controller;

import com.social.ecom.order.exception.ResourceNotFoundException;
import com.social.ecom.order.model.Order;
import com.social.ecom.order.services.OrderService;
import com.social.ecom.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${prefix.api}/orders")
public class OrderController {

    private final String TOPIC = "order-log";
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestBody Order order) {
        orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Order created successfully!", null));
    }

    @GetMapping("/{orderId}/get")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable int orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Found!", order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

//    @GetMapping("/list")
//    public ResponseEntity<ApiResponse> getOrdersList() {
//        List<Order> orders = orderService.
//    }
}
