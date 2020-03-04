package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class FullTextConfiguration extends Configuration {

  @Valid
  @NotNull
  private String mappingFile;

  @Valid
  @NotNull
  private List<String> supportedMimetypes;

  @JsonProperty("mappingFile")
  public String getMappingFile() {
    return mappingFile;
  }

  @JsonProperty("mappingFile")
  public void setMappingFile(String mappingFile) {
    this.mappingFile = mappingFile;
  }

  @JsonProperty("supportedMimetypes")
  public List<String> getSupportedMimetypes() {
    return supportedMimetypes;
  }

  @JsonProperty("supportedMimetypes")
  public void setSupportedMimetypes(List<String> supportedMimetypes) {
    this.supportedMimetypes = supportedMimetypes;
  }
}
