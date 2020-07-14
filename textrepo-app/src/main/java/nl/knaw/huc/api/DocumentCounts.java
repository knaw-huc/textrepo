package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class DocumentCounts {

  private final long documentCount;

  private final long hasFile;

  private final long hasMetadata;

  private final long hasBoth;

  @ConstructorProperties({"document_count", "has_file", "has_metadata", "has_both"})
  public DocumentCounts(long documentCount, long hasFile, long hasMetadata, long hasBoth) {
    this.documentCount = documentCount;
    this.hasFile = hasFile;
    this.hasMetadata = hasMetadata;
    this.hasBoth = hasBoth;
  }

  @JsonProperty
  public long getDocumentCount() {
    return documentCount;
  }

  @JsonProperty
  public long getHasFile() {
    return hasFile;
  }

  @JsonProperty
  public long getHasMetadata() {
    return hasMetadata;
  }

  @JsonProperty
  public long getHasBoth() {
    return hasBoth;
  }
}
