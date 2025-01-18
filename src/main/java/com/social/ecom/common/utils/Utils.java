package com.social.ecom.common.utils;

import com.social.ecom.customer.avro.CreateCustomer;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {
    public static void HandleProcessingError(ConsumerRecord<String, byte[]> record, Exception e, KafkaTemplate<String, byte[]> kafkaTemplate, Logger logger) {
        // Implement retry logic or send to DLQ
        String dlqTopic = "ecom.orders.dlq";
        try {
            ProducerRecord<String, byte[]> dlqRecord = new ProducerRecord<>(dlqTopic, record.key(), record.value());
            // Add error information in headers
            dlqRecord.headers().add("error", e.getMessage().getBytes());
            dlqRecord.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());

            kafkaTemplate.send(dlqRecord).whenComplete((sendResult, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send order with key {}: {} and {}", record.key(), exception.getMessage(), sendResult.getRecordMetadata().timestamp(), exception);
                } else {
                    logger.info("Order sent successfully to topic {} with key {}. Partition: {}, Offset: {}",
                            dlqTopic, record.key(),
                            sendResult.getRecordMetadata().partition(),
                            sendResult.getRecordMetadata().offset());
                }
            });
            logger.info("Message sent to DLQ: {}", record.key());
        } catch (Exception dlqEx) {
            logger.error("Failed to send to DLQ: {}", record.key(), dlqEx);
        }
    }

    public static <T> byte[] serializeAvroObject(T data, Class<T> clazz, Logger logger) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            DatumWriter<T> writer = new SpecificDatumWriter<>(clazz);
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);

            writer.write(data, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            // Log the exception (use a logging framework like SLF4J or Log4j)
            logger.error("Serialization failed: {}", e.getMessage(), e);

            // Re-throw the exception as a custom or runtime exception
            throw new RuntimeException("Failed to serialize object of type " + clazz.getName(), e);
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error during serialization: {}", e.getMessage(), e);

            // Wrap in a RuntimeException or a custom exception
            throw new RuntimeException("Unexpected error while serializing object of type " + clazz.getName(), e);
        }
    }

}
