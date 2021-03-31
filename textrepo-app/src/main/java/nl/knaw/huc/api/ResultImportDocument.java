package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
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
  @ApiModelProperty(position = 1)
  public UUID getFileId() {
    return version.getFileId();
  }

  @JsonProperty
  @ApiModelProperty(position = 2)
  public UUID getVersionId() {
    return version.getId();
  }

  @JsonProperty
  @ApiModelProperty(position = 3, value = "hash value of version contents",
      example = "4177ad5c5ababb0d56005cad513e9854735ed8979a0c404a73f3e9c7")
  public String getContentsSha() {
    return version.getContentsSha();
  }

  @JsonProperty
  @ApiModelProperty(position = 4, value = "true iff this version was created in this request")
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
