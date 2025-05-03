package com.rj.payment_service.service;

import com.rj.payment_service.dto.request.CheckoutSessionRequestDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface PaymentService {

    Session createCheckoutSession(CheckoutSessionRequestDTO request) throws StripeException;
}
