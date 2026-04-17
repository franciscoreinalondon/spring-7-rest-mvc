package com.franciscoreina.spring7.application.kafka.producer;

import com.franciscoreina.spring7.application.kafka.avro.OrderPaidAvro;
import com.franciscoreina.spring7.application.kafka.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class OrderPaidProducer {

    private final KafkaTemplate<String, OrderPaidAvro> kafkaTemplate;

    @EventListener
    public void handle(OrderPaidEvent event) {
        log.info("Send OrderPaid event for orderId={}", event.orderId());

        kafkaTemplate.send("order-paid-topic", event.orderId().toString(), toAvro(event));
    }

    private OrderPaidAvro toAvro(OrderPaidEvent event) {
        return OrderPaidAvro.newBuilder()
                .setOrderId(event.orderId().toString())
                .build();
    }
}
