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

    private Long orderId;
    private int customerId;
    private List<OrderItem> items;
    private double totalAmount;
    private String orderDate;
    private String status;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 2L;

        private int productId;
        private int quantity;
        private double price;
    }
}