package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.resources.view.segmented.TextSegments;

/**
 * Result object for TextSegment view.
 * <p/>
 * Yes, as of Java 14 this can be a record...
 * As soon as DropWizard has Jackson support for serialization of records.
 */
public class ResultTextSegment {
  private final TextSegments segments;
  private final String region;

  public ResultTextSegment(TextSegments segments, String region) {
    this.segments = segments;
    this.region = region;
  }

  @JsonProperty
  @SuppressWarnings("unused")
  public TextSegments getSegments() {
    return segments;
  }

  @JsonProperty
  @SuppressWarnings("unused")
  public String getRegion() {
    return region;
  }
}
