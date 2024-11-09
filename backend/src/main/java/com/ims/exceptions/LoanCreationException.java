package com.ims.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class LoanCreationException extends RuntimeException {
  public LoanCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
