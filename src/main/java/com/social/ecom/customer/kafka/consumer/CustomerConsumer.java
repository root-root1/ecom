package com.social.ecom.customer.kafka.consumer;

import com.social.ecom.common.utils.Utils;
import com.social.ecom.customer.avro.CreateCustomer;
import com.social.ecom.customer.model.CustomerModel;
import com.social.ecom.customer.repository.CustomerRepository;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomerConsumer {
    private static final Logger logger = LoggerFactory.getLogger(CustomerConsumer.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @KafkaListener(
            topics = "customer",
            groupId = "customer-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumerCustomer(ConsumerRecord<String, byte[]> record) {
        String email = record.key();
        logger.info("Received customer message: {}", email);
        try {
            CustomerModel customerModel = new CustomerModel();
            CreateCustomer avroCustomer = decodeCustomerByte(record.value());
            customerModel.setEmail(email);
            customerModel.setAddress(avroCustomer.getAddress());
            customerModel.setPhone(avroCustomer.getPhone());
            customerModel.setLastName(avroCustomer.getLastName());
            customerModel.setFirstName(avroCustomer.getFirstName());
            customerRepository.saveCustomer(customerModel);
            logger.info("Successfully processed Customer creation: {}", email);

        } catch (Exception e) {
            logger.error("Error processing customer: {}", email, e);
            Utils.HandleProcessingError(record, e, kafkaTemplate, logger);
        }
    }

    private CreateCustomer decodeCustomerByte(byte[] record) throws IOException {
        DatumReader<CreateCustomer> reader = new SpecificDatumReader<>(CreateCustomer.class);
        return reader.read(null, DecoderFactory.get().binaryDecoder(record, null));
    }
}
