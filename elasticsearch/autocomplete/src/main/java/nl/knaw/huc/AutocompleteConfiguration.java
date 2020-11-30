package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class AutocompleteConfiguration extends Configuration {

  @Valid
  @NotNull
  private int minKeywordLength;

  @Valid
  @NotNull
  private String keywordDelimiters;

  @Valid
  @NotNull
  private String mappingFile;

  @Valid
  @NotNull
  private List<MimetypeSubtypesConfiguration> mimetypeSubtypes;

  @JsonProperty("minKeywordLength")
  public int getMinKeywordLength() {
    return minKeywordLength;
  }

  @JsonProperty("minKeywordLength")
  public void setMinKeywordLength(int minKeywordLength) {
    this.minKeywordLength = minKeywordLength;
  }

  @JsonProperty("keywordDelimiters")
  public String getKeywordDelimiters() {
    return keywordDelimiters;
  }

  @JsonProperty("keywordDelimiters")
  public void setKeywordDelimiters(String keywordDelimiters) {
    this.keywordDelimiters = keywordDelimiters;
  }

  @JsonProperty("mappingFile")
  public String getMappingFile() {
    return mappingFile;
  }

  @JsonProperty("mappingFile")
  public void setMappingFile(String mappingFile) {
    this.mappingFile = mappingFile;
  }

  @JsonProperty("mimetypeSubtypes")
  public List<MimetypeSubtypesConfiguration> getMimetypeSubtypes() {
    return mimetypeSubtypes;
  }

  @JsonProperty("mimetypeSubtypes")
  public void setMimetypeSubtypes(List<MimetypeSubtypesConfiguration> mimetypeSubtypes) {
    this.mimetypeSubtypes = mimetypeSubtypes;
  }
}
