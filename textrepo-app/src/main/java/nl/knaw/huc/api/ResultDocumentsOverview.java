package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.DocumentsOverview;

public class ResultDocumentsOverview {
  private final long documentCount;

  private final long hasFile;

  private final long hasMetadata;

  private final long hasBoth;

  private final long hasNone;

  public ResultDocumentsOverview(DocumentsOverview overview) {
    this.documentCount = overview.getDocumentCount();
    this.hasFile = overview.getHasFile();
    this.hasMetadata = overview.getHasMetadata();
    this.hasBoth = overview.getHasBoth();
    this.hasNone = overview.getHasNone();
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

  @JsonProperty
  public long getHasNone() {
    return hasNone;
  }
}
