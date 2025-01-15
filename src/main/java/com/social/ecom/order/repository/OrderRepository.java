package com.social.ecom.order.repository;

import com.social.ecom.order.exception.ResourceNotFoundException;
import com.social.ecom.order.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class OrderRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> orderRowMapper = new RowMapper<Order>() {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setOrderId(rs.getString("order_id"));
            order.setCustomerId(rs.getString("customer_id"));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setOrderDate(rs.getString("order_date"));
            order.setStatus(rs.getString("status"));
            return order;
        }
    };

    public void saveOrder(Order order) {
        String sql = "insert into order (order_id, customer_id, total_amount, order_date, status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                order.getOrderId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getOrderDate(),
                order.getStatus());
    }

    public Order getOrderById(int orderId) {
        try {
            String sql = "select * from order where order_id = ?";
            return jdbcTemplate.queryForObject(sql, orderRowMapper, orderId);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Order not found");
        }
    }

    public List<Order> getOrders() {
        String sql = "select * from order";
        return jdbcTemplate.query(sql, orderRowMapper);
    }


}
