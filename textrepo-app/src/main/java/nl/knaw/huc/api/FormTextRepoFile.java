package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class FormTextRepoFile {

  @NotNull(message = "is mandatory")
  @ApiModelProperty(example = "34739357-eb75-449b-b2df-d3f6289470d6")
  private UUID docId;

  @NotNull(message = "is mandatory")
  @ApiModelProperty(example = "1")
  private Short typeId;

  @JsonCreator
  public FormTextRepoFile(
      @JsonProperty("docId") UUID docId,
      @JsonProperty("typeId") Short typeId
  ) {
    this.docId = docId;
    this.typeId = typeId;
  }

  public UUID getDocId() {
    return docId;
  }

  public short getTypeId() {
    return typeId;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("docId", docId)
        .add("typeId", typeId)
        .toString();
  }
}
