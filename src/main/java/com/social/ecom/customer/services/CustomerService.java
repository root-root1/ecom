package com.social.ecom.customer.services;


import com.social.ecom.customer.kafka.producer.CustomerProducer;
import com.social.ecom.customer.model.CustomerModel;
import com.social.ecom.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;


@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerProducer producer;

    public void createCustomer(CustomerModel customerModel) {
        producer.sendCreateCustomer(customerModel);
        logger.info("Customer sent to kafka {}", customerModel.getEmail());
    }

    public CustomerModel getCustomerById(int customerId) {
        return customerRepository.getCustomerById(customerId);
    }

    public List<CustomerModel> getCustomers() {
        return customerRepository.getCustomerList();
    }


}
