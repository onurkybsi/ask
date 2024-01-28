package org.kybprototyping.ask;

public class AskException extends RuntimeException {

  public AskException(String message) {
    super(message);
  }

  public AskException(String message, Throwable cause) {
    super(message, cause);
  }

}
