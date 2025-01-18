package com.social.ecom.products.controller;

import com.social.ecom.products.model.ProductModel;
import com.social.ecom.products.services.ProductService;
import com.social.ecom.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${prefix.api}/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct(@RequestBody ProductModel product) {
        productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Created", null));
    }

}
