package nl.knaw.huc.resources.view.segmented;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public class SegmentedTextRegionParam extends AbstractParam<SegmentedTextRegionParam.TextRegion> {
  private final String input;

  public SegmentedTextRegionParam(@Nullable String input) {
    super(input);
    this.input = input;
  }

  public SegmentedTextRegionParam(@Nullable String input, String parameterName) {
    super(input, parameterName);
    this.input = input;
  }

  public String getInput() {
    return input;
  }

  @Override
  protected TextRegion parse(@Nullable String input) throws Exception {
    if (input != null) {
      var parts = input.split(",", 5);
      if (parts.length == 2) {
        return new TextRegion(parts[0], OptionalInt.empty(), parts[1], OptionalInt.empty());
      }

      if (parts.length == 4) {
        final OptionalInt optStartOffset = "full".equals(parts[1]) ? OptionalInt.empty()
            : OptionalInt.of(Integer.parseUnsignedInt(parts[1]));

        final OptionalInt optEndOffset = "full".equals(parts[3]) ? OptionalInt.empty()
            : OptionalInt.of(Integer.parseUnsignedInt(parts[3]));

        return new TextRegion(parts[0], optStartOffset, parts[2], optEndOffset);
      }
    }

    throw new IllegalArgumentException(
        "Region must be 'startAnchor,endAnchor' or 'startAnchor,startOffset,endAnchor,endOffset'");
  }

  public record TextRegion(String startAnchor, OptionalInt startOffset,
                           String endAnchor, OptionalInt endOffset) {
  }

}
