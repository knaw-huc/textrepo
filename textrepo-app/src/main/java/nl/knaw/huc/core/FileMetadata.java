package nl.knaw.huc.core;

import java.util.Map;

public class FileMetadata {
  private TextRepoFile file;
  private Map<String, String> metadata;

  public TextRepoFile getFile() {
    return file;
  }

  public void setFile(TextRepoFile file) {
    this.file = file;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}
