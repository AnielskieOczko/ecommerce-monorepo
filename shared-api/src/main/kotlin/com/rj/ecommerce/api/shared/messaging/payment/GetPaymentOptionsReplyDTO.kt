package com.rj.ecommerce.api.shared.messaging.payment

import com.rj.ecommerce.api.shared.dto.payment.PaymentOptionDTO

// The message the Payment Microservice sends back.
// It contains the list of DTOs the frontend needs.
data class GetPaymentOptionsReply(val options: List<PaymentOptionDTO>)