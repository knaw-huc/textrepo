package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import nl.knaw.huc.core.TextRepoFile;

public class ResultTextRepoFile {

  private final UUID id;
  private final UUID docId;
  private final short typeId;

  public ResultTextRepoFile(UUID docId, TextRepoFile file) {
    this.id = file.getId();
    this.docId = docId;
    this.typeId = file.getTypeId();
  }

  @JsonProperty
  public UUID getId() {
    return id;
  }

  @JsonProperty
  public short getTypeId() {
    return typeId;
  }

  @JsonProperty
  public UUID getDocId() {
    return docId;
  }

}
