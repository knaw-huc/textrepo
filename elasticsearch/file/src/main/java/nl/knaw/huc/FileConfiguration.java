package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FileConfiguration extends Configuration {

  @Valid
  @NotNull
  private String mappingFile;

  @Valid
  @NotNull
  private String textrepoHost;

  @Valid
  @NotNull
  private String dateFormat;

  @Valid
  @NotNull
  private Integer pageSize;

  @JsonProperty("mappingFile")
  public String getMappingFile() {
    return mappingFile;
  }

  @JsonProperty("mappingFile")
  public void setMappingFile(String mappingFile) {
    this.mappingFile = mappingFile;
  }

  @JsonProperty("textrepoHost")
  public String getTextrepoHost() {
    return textrepoHost;
  }

  @JsonProperty("textrepoHost")
  public void setTextrepoHost(String textrepoHost) {
    this.textrepoHost = textrepoHost;
  }

  @JsonProperty("dateFormat")
  public String getDateFormat() {
    return dateFormat;
  }

  @JsonProperty("dateFormat")
  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  @JsonProperty("pageSize")
  public Integer getPageSize() {
    return pageSize;
  }

  @JsonProperty("pageSize")
  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }
}
