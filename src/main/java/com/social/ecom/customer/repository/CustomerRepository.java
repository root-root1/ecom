package com.social.ecom.customer.repository;

import com.social.ecom.customer.model.CustomerModel;
import com.social.ecom.order.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CustomerRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(CustomerRepository.class);


    private RowMapper<CustomerModel> customerRowMapper = new RowMapper<CustomerModel>() {
        @Override
        public CustomerModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            CustomerModel customerModel = new CustomerModel();
            customerModel.setFirstName(rs.getString("first_name"));
            customerModel.setLastName(rs.getString("last_name"));
            customerModel.setPhone(rs.getString("phone"));
            customerModel.setEmail(rs.getString("email"));
            customerModel.setAddress(rs.getString("address"));
            return customerModel;
        }
    };

    public void saveCustomer(CustomerModel customer) {
        String sql = "insert into customers (first_name, last_name, email, phone, address) values (?,?,?,?,?)";
        jdbcTemplate.update(sql, customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhone(), customer.getAddress());
    }

    public CustomerModel getCustomerById(int customerId) {
        try {
            String sql = "select * from customers where customer_id = ?";
            return jdbcTemplate.queryForObject(sql, customerRowMapper, customerId);
        } catch (DataAccessException e) {
            logger.error(e.getMessage());
            throw new ResourceNotFoundException("Customer not found");

        }
    }
    public List<CustomerModel> getCustomerList() {
        try {
            String sql = "select * from customers";
            return jdbcTemplate.query(sql, customerRowMapper);
        } catch (DataAccessException e) {
            logger.error(e.getMessage());
            throw new ResourceNotFoundException("Database error");

        }
    }
}
