package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Version;

import java.util.UUID;

public class ResultImportDocument {
  private final Document document;
  private final Version version;
  private final boolean isNewVersion;

  public ResultImportDocument(Document document, Version version, boolean isNewVersion) {
    this.document = document;
    this.version = version;
    this.isNewVersion = isNewVersion;
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

  @JsonProperty
  public boolean isNewVersion() {
    return isNewVersion;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("document", document)
                      .add("version", version)
                      .add("isNewVersion", isNewVersion)
                      .toString();
  }
}
