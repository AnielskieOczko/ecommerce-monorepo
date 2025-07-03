package com.rj.ecommerce_backend.events.payment

import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.context.ApplicationEvent

class PaymentFailedEvent(source: Any, val order: Order, val response: PaymentResponseDTO) : ApplicationEvent(source)