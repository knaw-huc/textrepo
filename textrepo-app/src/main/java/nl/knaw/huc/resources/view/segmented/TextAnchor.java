package nl.knaw.huc.resources.view.segmented;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextAnchor {
  @JsonProperty("identifier")
  public String id;
  
  @JsonProperty("sequence_number")
  public long index;
}
