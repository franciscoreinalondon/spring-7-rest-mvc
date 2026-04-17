package com.franciscoreina.spring7.application.kafka.event;

import java.util.UUID;

public record OrderPaidEvent(UUID orderId) {
}
