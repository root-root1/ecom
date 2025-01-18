package com.social.ecom.products.kafka.consumer;

import com.social.ecom.common.utils.Utils;
import com.social.ecom.customer.avro.CreateCustomer;
import com.social.ecom.products.avro.CreateProduct;
import com.social.ecom.products.model.ProductModel;
import com.social.ecom.products.repository.ProductRepository;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProductConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ProductConsumer.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @KafkaListener(
            topics = "products",
            groupId = "product-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void productConsumer(ConsumerRecord<String, byte[]> record) {
        String name = record.key();
        logger.info("Received product message: {}", name);
        try {
            ProductModel productModel = new ProductModel();
            CreateProduct avroProduct = decodeProductByte(record.value());
            productModel.setName(avroProduct.getName());
            productModel.setDescription(avroProduct.getDescription());
            productModel.setPrice((double) avroProduct.getPrice());
            productModel.setStockQuantity(avroProduct.getStockQuantity());

            productRepository.saveProduct(productModel);

            logger.info("Successfully processed order: {}", name);
        } catch (Exception e) {
            logger.error("Error processing product: {}", name, e);
            Utils.HandleProcessingError(record, e, kafkaTemplate, logger);
        }
    }

    private CreateProduct decodeProductByte(byte[] record) throws IOException {
        DatumReader<CreateProduct> reader = new SpecificDatumReader<>(CreateProduct.class);
        return reader.read(null, DecoderFactory.get().binaryDecoder(record, null));
    }
}