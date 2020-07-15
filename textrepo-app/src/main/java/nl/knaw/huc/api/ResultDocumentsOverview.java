package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.DocumentsOverview;

public class ResultDocumentCounts {
  private final long documentCount;

  private final long hasFile;

  private final long hasMetadata;

  private final long hasBoth;

  private final long orphans;

  public ResultDocumentCounts(DocumentsOverview counts) {
    this.documentCount = counts.getDocumentCount();
    this.hasFile = counts.getHasFile();
    this.hasMetadata = counts.getHasMetadata();
    this.hasBoth = counts.getHasBoth();
    this.orphans = counts.getOrphans();
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
  public long getOrphans() {
    return orphans;
  }
}
