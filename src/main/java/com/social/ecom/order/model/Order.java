package com.social.ecom.order.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private double totalAmount;
    private String orderDate;
    private String status;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem implements Serializable {
        private static final long serialVersionUID = 2L;

        private String itemId;
        private int quantity;
        private double price;
    }
}