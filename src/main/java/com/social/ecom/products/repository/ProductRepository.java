package com.social.ecom.products.repository;

import com.social.ecom.order.exception.ResourceNotFoundException;
import com.social.ecom.products.model.ProductModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);

    private RowMapper<ProductModel> productRowMapper = new RowMapper<ProductModel>() {
        @Override
        public ProductModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductModel productModel = new ProductModel();
            productModel.setName(rs.getString("name"));
            productModel.setDescription(rs.getString("description"));
            productModel.setPrice(rs.getDouble("price"));
            productModel.setStockQuantity(rs.getInt("stock_quantity"));
            return productModel;
        }
    };

    public void saveProduct(ProductModel model) {
        String sql = "insert into products (name, description, price, stock_quantity) values (?,?,?,?);";
        jdbcTemplate.update(sql, model.getName(), model.getDescription(), model.getPrice(), model.getStockQuantity());
    }

    public ProductModel getProductById(int productId) {
        try {
            String sql = "select * from products where id = ?";
            return jdbcTemplate.queryForObject(sql, productRowMapper, productId);
        } catch (EmptyResultDataAccessException e) {
            logger.error("No customer found with ID: {}", productId, e);
            throw new ResourceNotFoundException("Customer not found with ID: " + productId);
        }catch (DataAccessException e) {
            logger.error("Database error occurred while fetching product with ID: {}", productId, e);
            throw new RuntimeException("An unexpected error occurred while fetching product details");
        }
    }
    public List<ProductModel> getProductList() {
        try {
            String sql = "select * from customers";
            return jdbcTemplate.query(sql, productRowMapper);
        } catch (DataAccessException e) {
            logger.error(e.getMessage());
            throw new ResourceNotFoundException("Failed to get Product List");
        }
    }
}
