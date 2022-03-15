package nl.knaw.huc.resources.view.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.view.RangeParam;

public class TextLinesResolver extends TextResolver<List<String>> {
  private final RangeParam startParam;
  private final RangeParam endParam;

  public TextLinesResolver(RangeParam startParam, RangeParam endParam) {
    this.startParam = startParam;
    this.endParam = endParam;
  }

  @Override
  @Nonnull
  public List<String> resolve(@Nonnull Contents contents) {
    final String text = contents.asUtf8String();

    final var indexOfFirstLine = 0;
    final var startOffset = startParam.get().orElse(indexOfFirstLine);

    final var lines = text.split(LINEBREAK_MATCHER);
    final var indexOfLastLine = lines.length - 1;
    final var endOffset = endParam.get().orElse(indexOfLastLine);

    checkOffsets(startOffset, endOffset, indexOfLastLine);

    final List<String> fragment = Arrays.asList(lines)
                                        .subList(startOffset, endOffset + 1);

    return Collections.unmodifiableList(fragment);
  }
}
