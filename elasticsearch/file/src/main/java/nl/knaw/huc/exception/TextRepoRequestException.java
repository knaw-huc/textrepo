package nl.knaw.huc.exception;

public class TextRepoRequestException extends IllegalArgumentException {

  public TextRepoRequestException(String message) {
    super(message);
  }

  public TextRepoRequestException(String message, Exception ex) {
    super(message, ex);
  }
}
