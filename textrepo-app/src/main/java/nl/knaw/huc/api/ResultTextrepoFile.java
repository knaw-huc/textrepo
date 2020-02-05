package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.core.TextrepoFile;

import java.util.UUID;

public class ResultTextrepoFile {

  private UUID id;
  private final UUID docId;
  private final short typeId;

  public ResultTextrepoFile(UUID docId, TextrepoFile file) {
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
