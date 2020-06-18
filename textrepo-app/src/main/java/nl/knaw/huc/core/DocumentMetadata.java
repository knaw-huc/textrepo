package nl.knaw.huc.core;

import java.util.Map;

/**
 * Document with its metadata
 */
public class DocumentMetadata {

  private Document document;
  private Map<String, String> metadata;

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}
