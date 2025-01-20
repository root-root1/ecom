package com.social.ecom.order.repository;

import com.social.ecom.order.exception.ResourceNotFoundException;
import com.social.ecom.order.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class OrderRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        Order order = new Order();
        order.setOrderId(rs.getLong("order_id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setOrderDate(rs.getString("order_date"));
        order.setStatus(rs.getString("status"));
        return order;
    };

    public void saveOrder(Order order) {
        // First insert the order
        String sql = "INSERT INTO orders (customer_id, order_date, status) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, order.getCustomerId());
            ps.setString(2, order.getOrderDate());
            ps.setString(3, order.getStatus());
            return ps;
        }, keyHolder);

        // Get the generated order ID
        long orderId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        order.setOrderId(orderId);

        // Insert order items using batch update
        List<Order.OrderItem> orderItems = order.getItems();
        String orderItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(orderItemSql, orderItems, orderItems.size(), (ps, orderItem) -> {
            ps.setLong(1, orderId);
            ps.setLong(2, orderItem.getProductId());
            ps.setInt(3, orderItem.getQuantity());
            ps.setBigDecimal(4, BigDecimal.valueOf(orderItem.getPrice()));
        });
    }

    public Order getOrderById(long orderId) {
        try {
            String sql = "SELECT * FROM orders WHERE order_id = ?";
            return jdbcTemplate.queryForObject(sql, orderRowMapper, orderId);
        } catch (DataAccessException e) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
    }

    public List<Order> getOrders() {
        String sql = "SELECT * FROM orders";
        return jdbcTemplate.query(sql, orderRowMapper);
    }
}