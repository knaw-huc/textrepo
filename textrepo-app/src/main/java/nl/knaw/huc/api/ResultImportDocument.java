package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Version;

import java.util.UUID;

public class ResultImportDocument {
  private final Document document;
  private final Version version;

  public ResultImportDocument(Document document, Version version) {
    this.document = document;
    this.version = version;
  }

  @JsonProperty
  public UUID getDocumentId() {
    return document.getId();
  }

  @JsonProperty
  public UUID getFileId() {
    return version.getFileId();
  }

  @JsonProperty
  public UUID getVersionId() {
    return version.getId();
  }

  @JsonProperty
  public String getContentsSha() {
    return version.getContentsSha();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("document", document)
                      .add("version", version)
                      .toString();
  }
}
