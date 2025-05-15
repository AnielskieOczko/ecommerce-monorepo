package com.rj.ecommerce_backend.user.exceptions;

public class AuthorityAlreadyExistsException extends RuntimeException {
  public AuthorityAlreadyExistsException(String message) {
    super(message);
  }
}
