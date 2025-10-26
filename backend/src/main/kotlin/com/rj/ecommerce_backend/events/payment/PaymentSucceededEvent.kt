package com.rj.ecommerce_backend.events.payment

import com.rj.ecommerce.api.shared.messaging.payment.response.PaymentInitiationResponse
import com.rj.ecommerce_backend.api.shared.messaging.payment.response.PaymentInitiationResponse
import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.context.ApplicationEvent

class PaymentSucceededEvent(source: Any, val order: Order) : ApplicationEvent(source)