package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class ResultVersionMetadataEntry {

  private final UUID versionId;
  private final String key;
  private final String value;

  public ResultVersionMetadataEntry(
      UUID versionId,
      MetadataEntry entry
  ) {
    this.versionId = versionId;
    this.key = entry.getKey();
    this.value = entry.getValue();
  }

  @JsonProperty
  public UUID getVersionId() {
    return versionId;
  }

  @JsonProperty
  public String getKey() {
    return key;
  }

  @JsonProperty
  public String getValue() {
    return value;
  }
}
