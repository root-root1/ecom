package com.social.ecom.customer.kafka.producer;

import com.social.ecom.common.utils.Utils;
import com.social.ecom.customer.avro.CreateCustomer;
import com.social.ecom.customer.model.CustomerModel;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class CustomerProducer {
    private static final Logger logger = LoggerFactory.getLogger(CustomerProducer.class);
    private static final String TOPIC = "customer";

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendCreateCustomer(CustomerModel model) {
        try {
            CreateCustomer customer = buildCustomer(model);
            byte[] customerEvent = Utils.serializeAvroObject(customer, CreateCustomer.class, logger);

            ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                    TOPIC,
                    customer.getEmail(),
                    customerEvent
            );

            record.headers().add("source", "customer-service".getBytes());
            record.headers().add("timestamp",
                    String.valueOf(System.currentTimeMillis()).getBytes());

            kafkaTemplate.send(record).whenComplete((sendResult, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send customer with key {}: {}", customer.getEmail(), exception.getMessage(), exception);
                } else {
                    logger.info("Customer sent successfully to topic {} with key {}. Partition: {}, Offset: {}",
                            TOPIC,
                            customer.getEmail(),
                            sendResult.getRecordMetadata().partition(),
                            sendResult.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            logger.error("Error serializing the customer: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send customer message", e);
        }
    }


    private CreateCustomer buildCustomer(CustomerModel model) {
        CreateCustomer customer = new CreateCustomer();
        customer.setFirstName(model.getFirstName());
        customer.setLastName(model.getLastName());
        customer.setEmail(model.getEmail());
        customer.setPhone(model.getPhone());
        customer.setAddress(model.getAddress());
        return customer;
    }
}
