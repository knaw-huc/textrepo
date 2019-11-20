package nl.knaw.huc.api;

public class FormContents {

  private final String name;
  private final byte[] content;

  public FormContents(String name, byte[] content) {
    this.name = name;
    this.content = content;
  }

  public String getName() {
    return name;
  }

  public byte[] getContent() {
    return content;
  }
}
