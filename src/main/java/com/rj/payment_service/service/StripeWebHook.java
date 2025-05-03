package com.rj.payment_service.service;

import com.stripe.model.Charge;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;

public interface StripeWebHook {
    void handleCheckoutSessionCompleted(Session session);
    void handleCheckoutSessionExpired(Session session);
    void handleChargeSucceeded(Charge charge);
    void handleUnknownEvent(String eventType, StripeObject object);
}