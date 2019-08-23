package nl.knaw.huc.service;

public class ExistsException extends Exception {

  public ExistsException() {
    super("Version exists");
  }
}
