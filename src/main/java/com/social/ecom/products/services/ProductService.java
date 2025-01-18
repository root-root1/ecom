package com.social.ecom.products.services;

import com.social.ecom.products.kafka.producer.ProductProducer;
import com.social.ecom.products.model.ProductModel;
import com.social.ecom.products.repository.ProductRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductProducer productProducer;

    public void createProduct(ProductModel model){
        productProducer.sendCreateProduct(model);
        logger.info("Product sent to kafka {}", model.getName());
    }

    public ProductModel getCustomerById(int productId) {
        return productRepository.getProductById(productId);
    }

    public List<ProductModel> getProduct() {
        return productRepository.getProductList();
    }
}
