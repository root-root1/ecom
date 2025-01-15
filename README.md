# E-commerce Event-Driven Architecture with Kafka
## System Overview

### 1. Core Topics and Their Purpose

#### Orders Topic (`ecom.orders`)
- Primary topic for all order-related events
- High durability requirements (replication-factor: 3)
- Partitioned by orderId for scalability
- Retention: 7 days

#### Payments Topic (`ecom.payments`)
- Handles all payment processing events
- Partitioned by orderId to maintain ordering
- Retention: 7 days

#### Shipments Topic (`ecom.shipments`)
- Manages shipment and delivery events
- Partitioned by orderId
- Retention: 7 days

#### Inventory Topic (`ecom.inventory`)
- Real-time inventory updates
- Partitioned by productId
- Retention: 2 days

#### Dead Letter Queue (`ecom.dlq`)
- Stores failed messages for retry/analysis
- Retention: 14 days

### 2. Producer APIs and Events

#### Order Service
**APIs:**
1. Create Order
```
POST /api/orders
Produces: OrderCreatedEvent -> ecom.orders
{
    "orderId": "uuid",
    "customerId": "string",
    "items": [
        {
            "productId": "string",
            "quantity": "integer",
            "price": "decimal"
        }
    ],
    "totalAmount": "decimal",
    "status": "CREATED"
}
```

2. Update Order
```
PUT /api/orders/{orderId}
Produces: OrderUpdatedEvent -> ecom.orders
{
    "orderId": "uuid",
    "status": "string",
    "updateReason": "string"
}
```

3. Cancel Order
```
DELETE /api/orders/{orderId}
Produces: OrderCancelledEvent -> ecom.orders
{
    "orderId": "uuid",
    "cancellationReason": "string",
    "refundRequired": "boolean"
}
```

#### Payment Service
**APIs:**
1. Process Payment
```
POST /api/payments
Produces: PaymentProcessedEvent -> ecom.payments
{
    "paymentId": "uuid",
    "orderId": "uuid",
    "amount": "decimal",
    "status": "COMPLETED|FAILED",
    "paymentMethod": "string",
    "transactionId": "string"
}
```

2. Refund Payment
```
POST /api/payments/{paymentId}/refund
Produces: PaymentRefundedEvent -> ecom.payments
{
    "refundId": "uuid",
    "paymentId": "uuid",
    "orderId": "uuid",
    "amount": "decimal",
    "reason": "string"
}
```

#### Inventory Service
**APIs:**
1. Update Inventory
```
PUT /api/inventory/{productId}
Produces: InventoryUpdatedEvent -> ecom.inventory
{
    "productId": "string",
    "quantityChange": "integer",
    "operation": "ADD|SUBTRACT",
    "reason": "ORDER|RESTOCK|ADJUSTMENT"
}
```

#### Shipment Service
**APIs:**
1. Create Shipment
```
POST /api/shipments
Produces: ShipmentCreatedEvent -> ecom.shipments
{
    "shipmentId": "uuid",
    "orderId": "uuid",
    "carrier": "string",
    "trackingNumber": "string",
    "estimatedDelivery": "date"
}
```

2. Update Shipment Status
```
PUT /api/shipments/{shipmentId}/status
Produces: ShipmentStatusUpdatedEvent -> ecom.shipments
{
    "shipmentId": "uuid",
    "status": "string",
    "location": "string",
    "timestamp": "datetime"
}
```

### 3. Consumer Services and Event Processing

#### Payment Service Consumer
**Consumes from:** `ecom.orders`
**Processes:** OrderCreatedEvent
**Actions:**
- Validates payment information
- Processes payment with payment gateway
- Produces PaymentProcessedEvent
- Updates order status

#### Inventory Service Consumer
**Consumes from:** `ecom.orders`, `ecom.payments`
**Processes:** OrderCreatedEvent, PaymentProcessedEvent
**Actions:**
- Reserves inventory on OrderCreatedEvent
- Confirms inventory deduction on PaymentProcessedEvent
- Releases inventory on failed payments
- Produces InventoryUpdatedEvent

#### Shipment Service Consumer
**Consumes from:** `ecom.payments`
**Processes:** PaymentProcessedEvent
**Actions:**
- Creates shipment for paid orders
- Assigns carrier and generates tracking
- Produces ShipmentCreatedEvent

#### Analytics Service Consumer
**Consumes from:** All topics
**Actions:**
- Aggregates order statistics
- Tracks payment success rates
- Monitors inventory levels
- Generates shipping performance metrics

### 4. Advanced Kafka Features Implementation

#### Exactly-Once Processing
```java
@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, byte[]> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // ... other configs
        return new DefaultKafkaProducerFactory<>(props);
    }
}
```

#### Dead Letter Queue Implementation
```java
@KafkaListener(topics = "ecom.orders")
public void processOrder(ConsumerRecord<String, byte[]> record) {
    try {
        // Process the order
    } catch (Exception e) {
        // Send to DLQ
        kafkaTemplate.send("ecom.dlq", record.key(), record.value());
    }
}
```

#### Consumer Group Strategy
```java
@KafkaListener(
    topics = "ecom.orders",
    groupId = "payment-processing-group",
    concurrency = "3"
)
```

### 5. Avro Schema Definitions

#### OrderCreated.avsc
```json
{
  "type": "record",
  "name": "OrderCreated",
  "namespace": "com.ecommerce.events",
  "fields": [
    {"name": "orderId", "type": "string"},
    {"name": "customerId", "type": "string"},
    {"name": "items", "type": {
      "type": "array",
      "items": {
        "type": "record",
        "name": "OrderItem",
        "fields": [
          {"name": "productId", "type": "string"},
          {"name": "quantity", "type": "int"},
          {"name": "price", "type": "double"}
        ]
      }
    }},
    {"name": "totalAmount", "type": "double"},
    {"name": "orderDate", "type": "long", "logicalType": "timestamp-millis"},
    {"name": "status", "type": "string"}
  ]
}
```

### 6. Monitoring and Metrics

#### Key Metrics to Track
1. Producer Metrics:
    - Record send rate
    - Average batch size
    - Record queue time
    - Record retry rate

2. Consumer Metrics:
    - Consumer lag
    - Record process rate
    - Failed record rate
    - Rebalance rate

#### Prometheus Configuration
```yaml
kafka_metrics:
  - pattern: kafka.producer.*
  - pattern: kafka.consumer.*
  - pattern: kafka.cluster.*
```

### 7. Error Handling and Retry Strategy

#### Retry Configuration
```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, byte[]> 
  kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
      new ConcurrentKafkaListenerContainerFactory<>();
    factory.setRetryTemplate(retryTemplate());
    return factory;
}

@Bean
public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(1000); // 1 second
    
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3);
    
    retryTemplate.setBackOffPolicy(backOffPolicy);
    retryTemplate.setRetryPolicy(retryPolicy);
    
    return retryTemplate;
}
```

### 8. Best Practices and Recommendations

1. Topic Naming Convention:
    - Use dot notation: `domain.entity.action`
    - Example: `ecom.orders.created`

2. Partition Strategy:
    - Use business keys (orderId, customerId)
    - Consider data volume and parallelism needs

3. Message Schema Evolution:
    - Use Schema Registry
    - Maintain backward compatibility
    - Version schemas explicitly

4. Performance Optimization:
    - Batch messages when possible
    - Use appropriate compression (snappy/gzip)
    - Monitor and adjust partition count

5. Security:
    - Enable SSL/TLS
    - Implement SASL authentication
    - Use ACLs for topic access control

### 9. Development and Testing Guidelines

1. Local Development Setup:
```yaml
version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      
  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```

2. Testing Strategies:
    - Use EmbeddedKafka for integration tests
    - Implement consumer driven contract tests
    - Test retry and error handling scenarios

3. Performance Testing:
    - Use Kafka Streams DSL for complex testing
    - Implement metrics collection
    - Test with production-like data volumes

### 10. Execution Flow
```
    Order Creation Flow:
    API Request → OrderService → Kafka Producer → Kafka Topic → Consumer → Database
```
