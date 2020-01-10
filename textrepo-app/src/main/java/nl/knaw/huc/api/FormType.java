package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.beans.ConstructorProperties;

public class FormType {

  @NotBlank(message = "is mandatory")
  private final String name;

  @NotBlank(message = "is mandatory")
  private final String mimetype;

  @JsonCreator
  public FormType(
      @JsonProperty("name") String name,
      @JsonProperty("mimetype") String mimetype
  ) {
    this.name = name;
    this.mimetype = mimetype;
  }

  public String getName() {
    return name;
  }

  public String getMimetype() {
    return mimetype;
  }
}
