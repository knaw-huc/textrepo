package nl.knaw.huc.api;

public class ResultException {
  private final int code;
  private final String message;

  public ResultException(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
