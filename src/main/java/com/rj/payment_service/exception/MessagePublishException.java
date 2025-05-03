package com.rj.payment_service.exception;

public class MessagePublishException extends RuntimeException {
    public MessagePublishException(String message, Throwable cause) {
        super(message);
    }
}
