package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MimetypeSubtypesConfiguration {

  @Valid
  @NotNull
  private String mimetype;

  @Valid
  @NotNull
  private List<String> subtypes;

  @JsonProperty("subtypes")
  public List<String> getSubtypes() {
    return subtypes;
  }

  @JsonProperty("subtypes")
  public void setSubtypes(List<String> subtypes) {
    this.subtypes = subtypes;
  }

  @JsonProperty("mimetype")
  public String getMimetype() {
    return mimetype;
  }

  @JsonProperty("mimetype")
  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }
}
