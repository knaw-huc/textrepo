package nl.knaw.huc.resources.view.segmented;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public class SegmentedTextRegionParam extends AbstractParam<SegmentedTextRegionParam.SegmentedTextRegion> {
  public SegmentedTextRegionParam(@Nullable String input) {
    super(input);
  }

  public SegmentedTextRegionParam(@Nullable String input, String parameterName) {
    super(input, parameterName);
  }

  @Override
  protected SegmentedTextRegion parse(@Nullable String input) throws Exception {
    if (input == null) {
      throw new IllegalArgumentException("%s must not be null");
    }

    var parts = input.split(",", 5);
    if (parts.length == 5) {
      throw new IllegalArgumentException(
          "%s has too many parts. Must be 'start,end' or 'startAnchor,startOffset,endAnchor,endOffset'");
    }

    if (parts.length == 2) {
      return new SegmentedTextRegion(parts[0], parts[1]);
    }

    if (parts.length == 4) {
      final OptionalInt optStartOffset;
      if ("full".equals(parts[1])) {
        optStartOffset = OptionalInt.empty();
      } else {
        optStartOffset = OptionalInt.of(Integer.parseUnsignedInt(parts[1]));
      }

      final OptionalInt optEndOffset;
      if ("full".equals(parts[3])) {
        optEndOffset = OptionalInt.empty();
      } else {
        optEndOffset = OptionalInt.of(Integer.parseUnsignedInt(parts[3]));
      }

      return new SegmentedTextRegion(parts[0], optStartOffset, parts[2], optEndOffset);
    }

    throw new IllegalArgumentException(
        "%s has too few parts. Must be 'start,end' or 'startAnchor,startOffset,endAnchor,endOffset'");
  }

  public static class SegmentedTextRegion {
    private final String startAnchor;
    private final String endAnchor;

    // Use OptionalInt to hold the 'full' or <int> values, so we can resolve later when context is available
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalInt startOffset;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalInt endOffset;

    private SegmentedTextRegion(String startAnchor, String endAnchor) {
      this.startAnchor = startAnchor;
      this.startOffset = OptionalInt.empty();
      this.endAnchor = endAnchor;
      this.endOffset = OptionalInt.empty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    SegmentedTextRegion(String startAnchor, OptionalInt startOffset,
                        String endAnchor, OptionalInt endOffset) {
      this.startAnchor = startAnchor;
      this.startOffset = startOffset;
      this.endAnchor = endAnchor;
      this.endOffset = endOffset;
    }

    public String getStartAnchor() {
      return startAnchor;
    }

    public OptionalInt getStartOffset() {
      return startOffset;
    }

    public String getEndAnchor() {
      return endAnchor;
    }

    public OptionalInt getEndOffset() {
      return endOffset;
    }
  }
}
