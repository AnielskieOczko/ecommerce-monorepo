package com.rj.ecommerce_backend.events.payment

import com.rj.ecommerce_backend.order.domain.Order
import org.springframework.context.ApplicationEvent

class PaymentSucceededEvent(source: Any, val order: Order) : ApplicationEvent(source)