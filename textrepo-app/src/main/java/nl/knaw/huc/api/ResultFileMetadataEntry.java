package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ResultFileMetadataEntry {

  private final UUID fileId;
  private final String key;
  private final String value;

  public ResultFileMetadataEntry(
      UUID fileId,
      MetadataEntry entry
  ) {
    this.fileId = fileId;
    this.key = entry.getKey();
    this.value = entry.getValue();
  }

  @JsonProperty
  public UUID getFileId() {
    return fileId;
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
