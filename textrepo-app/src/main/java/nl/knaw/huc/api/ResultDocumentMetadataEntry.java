package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ResultDocumentMetadataEntry {

  private final UUID docId;
  private final String key;
  private final String value;

  public ResultDocumentMetadataEntry(
      UUID docId,
      MetadataEntry entry
  ) {
    this.docId = docId;
    this.key = entry.getKey();
    this.value = entry.getValue();
  }

  @JsonProperty
  public UUID getDocId() {
    return docId;
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
