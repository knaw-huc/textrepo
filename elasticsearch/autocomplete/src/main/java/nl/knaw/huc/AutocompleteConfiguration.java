package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AutocompleteConfiguration extends Configuration {

  @Valid
  @NotNull
  private int minKeywordLength;

  @Valid
  @NotNull
  private String keywordDelimiters;


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

}
