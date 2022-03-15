package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;

public class ResultImportDocument {
  private final Document document;
  private final TextRepoFile file;
  private final Version version;
  private final boolean isNewVersion;
  private boolean indexed;

  public ResultImportDocument(
      Document document,
      TextRepoFile file,
      Version version,
      boolean isNewVersion
  ) {
    this.document = document;
    this.file = file;
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
  @ApiModelProperty(position = 1)
  public Short getTypeId() {
    return file.getTypeId();
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

  public void setIndexed(boolean indexed) {
    this.indexed = indexed;
  }

  @JsonProperty
  @ApiModelProperty(position = 5)
  public boolean getIndexed() {
    return indexed;
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
