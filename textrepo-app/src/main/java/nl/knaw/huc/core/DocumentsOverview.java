package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;

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

  public long getDocumentCount() {
    return documentCount;
  }

  public long getHasFile() {
    return hasFile;
  }

  public long getHasMetadata() {
    return hasMetadata;
  }

  public long getHasBoth() {
    return hasBoth;
  }

  public long getOrphans() {
    return documentCount - hasFile - hasMetadata + hasBoth;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("documentCount", documentCount)
        .add("hasFile", hasFile)
        .add("hasMetadata", hasMetadata)
        .add("hasBoth", hasBoth)
        .add("orphans", getOrphans())
        .toString();
  }
}
