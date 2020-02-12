package nl.knaw.huc.api;

public class FormContents {

  private final String name;
  private final byte[] contents;

  public FormContents(String name, byte[] contents) {
    this.name = name;
    this.contents = contents;
  }

  public String getName() {
    return name;
  }

  public byte[] getContents() {
    return contents;
  }
}
