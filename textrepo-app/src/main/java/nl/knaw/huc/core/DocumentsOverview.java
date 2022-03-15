package nl.knaw.huc.core;

import com.google.common.base.MoreObjects;
import java.beans.ConstructorProperties;

public class DocumentsOverview {

  private final long documentCount;

  private final long hasFile;

  private final long hasMetadata;

  private final long hasBoth;

  private final long hasNone;

  @ConstructorProperties({"document_count", "has_file", "has_metadata", "has_both"})
  public DocumentsOverview(long documentCount, long hasFile, long hasMetadata, long hasBoth) {
    this.documentCount = documentCount;
    this.hasFile = hasFile;
    this.hasMetadata = hasMetadata;
    this.hasBoth = hasBoth;
    this.hasNone = documentCount - hasFile - hasMetadata + hasBoth;
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

  public long getHasNone() {
    return hasNone;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("documentCount", documentCount)
        .add("hasFile", hasFile)
        .add("hasMetadata", hasMetadata)
        .add("hasBoth", hasBoth)
        .add("hasNone", hasNone)
        .toString();
  }
}
