package nl.knaw.huc.resources.view.segmented;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class TextSegments {
  @JsonProperty("resource_id")
  public String resourceId;

  @JsonProperty("_ordered_segments")
  public String[] segments;

  @JsonProperty("_anchors")
  public TextAnchor[] anchors;

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("resourceId", resourceId)
                      .add("segments", segments)
                      .add("anchors", anchors)
                      .toString();
  }
}
