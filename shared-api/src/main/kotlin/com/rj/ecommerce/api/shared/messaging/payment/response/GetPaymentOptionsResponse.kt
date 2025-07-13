package com.rj.ecommerce.api.shared.messaging.payment.response

import com.rj.ecommerce.api.shared.dto.payment.response.PaymentOptionDetails


// The message the Payment Microservice sends back.
// It contains the list of DTOs the frontend needs.
data class GetPaymentOptionsResponse(val options: List<PaymentOptionDetails>)