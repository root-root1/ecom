package com.social.ecom.products.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String description;
    private Double price;
    private int stockQuantity;
}
