package com.social.ecom.customer.controller;

import com.social.ecom.customer.model.CustomerModel;
import com.social.ecom.customer.services.CustomerService;
import com.social.ecom.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${prefix.api}/customer")
public class Customer {
    private final String TOPIC = "customer-log";
    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse> createCustomer(@RequestBody CustomerModel customerModel) {
        customerService.createCustomer(customerModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Created", null));
    }

    @GetMapping("/{customerId}/get")
    public ResponseEntity<ApiResponse> getCustomerById(@PathVariable int customerId) {
        try {
            CustomerModel customer = customerService.getCustomerById(customerId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Found", customer));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse("Customer not found with ID: " + customerId, null));
        } catch (DataAccessException e) {
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("An unexpected error occurred while fetching customer details", null));
        }
    }
}
