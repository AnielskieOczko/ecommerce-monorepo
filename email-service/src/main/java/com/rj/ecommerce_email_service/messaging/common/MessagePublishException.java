package com.rj.ecommerce_email_service.messaging.common;

public class MessagePublishException extends RuntimeException {
    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
