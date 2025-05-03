package com.rj.payment_service.type;

import com.stripe.model.Charge;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.Getter;

@Getter
public enum StripeEventType {
    CHECKOUT_SESSION_COMPLETED("checkout.session.completed", Session.class),
    CHECKOUT_SESSION_EXPIRED("checkout.session.expired", Session.class),
    CHARGE_SUCCEEDED("charge.succeeded",Charge .class);
    private final String eventName;

    // expected class of the event object type
    private final Class<? extends StripeObject> eventClass;

    StripeEventType(String eventName, Class<? extends StripeObject> eventClass) {
        this.eventName = eventName;
        this.eventClass = eventClass;
    }

    public static StripeEventType fromStripeEventName(String eventName) {
        // values() returns array of all enum constants
        for (StripeEventType eventType : values()) {
            if (eventType.eventName.equals(eventName)) {
                return eventType;
            }
        }
        return null;
    }

}
