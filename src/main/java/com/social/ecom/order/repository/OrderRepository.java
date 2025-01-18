package com.social.ecom.order.repository;

import com.social.ecom.order.exception.ResourceNotFoundException;
import com.social.ecom.order.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> orderRowMapper = new RowMapper<Order>() {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setOrderId(rs.getLong("order_id"));
            order.setCustomerId(rs.getInt("customer_id"));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setOrderDate(rs.getString("order_date"));
            order.setStatus(rs.getString("status"));
            return order;
        }
    };

    public void saveOrder(Order order) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String sql = "insert into order (customer_id, total_amount, order_date) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                order.getCustomerId(),
                order.getTotalAmount(),
                currentDateTime);
        Long orderId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        order.setOrderId(orderId);

        List<Order.OrderItem> orderItems = order.getItems();
//        for (Order.OrderItem orderItem : orderItems) {
            String orderItemSql = "insert into order_item (order_id, product_id, quantity, price) values (?,?,?,?);";
//            jdbcTemplate.update(orderItemSql, order.getOrderId(), orderItem.getProductId(), orderItem.getQuantity(), orderItem.getPrice());
//        }
        jdbcTemplate.batchUpdate(orderItemSql, orderItems, orderItems.size(), (ps, orderItem) -> {
            ps.setLong(1, orderId);
            ps.setLong(2, orderItem.getProductId());
            ps.setInt(3, orderItem.getQuantity());
            ps.setBigDecimal(4, BigDecimal.valueOf(orderItem.getPrice()));
        });
    }

    public Order getOrderById(int orderId) {
        try {
            String sql = "select * from order where order_id = ?";
            return jdbcTemplate.queryForObject(sql, orderRowMapper, orderId);
        } catch (DataAccessException e) {
            throw new ResourceNotFoundException("Order not found");
        }
    }

    public List<Order> getOrders() {
        String sql = "select * from order";
        return jdbcTemplate.query(sql, orderRowMapper);
    }


}
