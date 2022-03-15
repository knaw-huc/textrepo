package nl.knaw.huc.resources.view;

import java.util.Arrays;
import java.util.List;
import nl.knaw.huc.resources.view.segmented.TextSegments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SegmentResolver extends RangeResolver<TextSegments, List<String>> {
  private static final Logger log = LoggerFactory.getLogger(SegmentResolver.class);

  private final RangeParam startIndex;
  private final RangeParam endIndex;

  public SegmentResolver(RangeParam startIndex, RangeParam endIndex) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  @Override
  public List<String> resolve(TextSegments source) {
    final var indexOfFirstLine = 0;
    final var startOffset = startIndex.get().orElse(indexOfFirstLine);

    final var indexOfLastLine = source.segments.length - 1;
    final var endOffset = endIndex.get().orElse(indexOfLastLine);

    log.debug("Sublist indexes: from=[{}], upto=[{}]", startOffset, endOffset);

    checkOffsets(startOffset, endOffset, indexOfLastLine);

    // Do not copy elements, but create a 'view' on the selected array elements
    final List<String> fragment = Arrays.asList(source.segments)
                                        .subList(startOffset, endOffset + 1);
    log.debug("Selected element count: {}", fragment.size());

    return fragment;
  }
}
