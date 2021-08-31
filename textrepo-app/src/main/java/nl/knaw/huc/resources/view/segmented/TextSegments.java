package nl.knaw.huc.resources.view.segmented;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextSegments {
  @JsonProperty("resource_id")
  public String resourceId;

  @JsonProperty("_ordered_segments")
  public String[] segments;

  @JsonProperty("_anchors")
  public TextAnchor[] anchors;
}
