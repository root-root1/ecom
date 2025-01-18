package com.social.ecom.products.kafka.producer;

import com.social.ecom.common.utils.Utils;
import com.social.ecom.products.avro.CreateProduct;
import com.social.ecom.products.model.ProductModel;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductProducer {
    private static final Logger logger = LoggerFactory.getLogger(ProductProducer.class);
    private static final String TOPIC = "products";

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendCreateProduct(ProductModel model) {
        try{
            CreateProduct product = buildProduct(model);
            byte[] productEvent  = Utils.serializeAvroObject(product, CreateProduct.class, logger);
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                    TOPIC,
                    model.getName(),
                    productEvent
            );
            record.headers().add("source", "customer-service".getBytes());
            record.headers().add("timestamp",
                    String.valueOf(System.currentTimeMillis()).getBytes());

            kafkaTemplate.send(record).whenComplete(((sendResult, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send Product with key {}: {}", product.getName(), exception.getMessage(), exception);
                } else {
                    logger.info("Product sent successfully to topic {} with key {}. Partition: {}, Offset: {}",
                            TOPIC,
                            product.getName(),
                            sendResult.getRecordMetadata().partition(),
                            sendResult.getRecordMetadata().offset());
                }
            }));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CreateProduct buildProduct(ProductModel model) {
        CreateProduct product = new CreateProduct();
        product.setName(model.getName());
        product.setPrice(model.getPrice().floatValue());
        product.setDescription(model.getDescription());
        product.setStockQuantity(model.getStockQuantity());
        return product;
    }
}
