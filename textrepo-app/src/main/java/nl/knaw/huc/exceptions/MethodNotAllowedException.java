package nl.knaw.huc.exceptions;

public class MethodNotAllowedException extends IllegalArgumentException {

  public MethodNotAllowedException(String message) {
    super(message);
  }
}
