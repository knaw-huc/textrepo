package nl.knaw.huc.resources.view.segmented;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class TextAnchor {
  @JsonProperty("identifier")
  public String id;

  @JsonProperty("sequence_number")
  public long index;

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("id", id)
                      .add("index", index)
                      .toString();
  }
}
