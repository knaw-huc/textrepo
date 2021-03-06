package nl.knaw.huc.resources.view.text;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.view.RangeParam;

import javax.annotation.Nonnull;
import java.util.StringJoiner;

public class TextLinesResolver extends TextResolver {
  private final RangeParam startParam;
  private final RangeParam endParam;

  public TextLinesResolver(RangeParam startParam, RangeParam endParam) {
    this.startParam = startParam;
    this.endParam = endParam;
  }

  @Override
  @Nonnull
  public String resolve(@Nonnull Contents contents) {
    final String text = contents.asUtf8String();
   
    final var indexOfFirstLine = 0;
    final var startOffset = startParam.get().orElse(indexOfFirstLine);

    final var lines = text.split(LINEBREAK_MATCHER);
    final var indexOfLastLine = lines.length - 1;
    final var endOffset = endParam.get().orElse(indexOfLastLine);

    checkOffsets(startOffset, endOffset, indexOfLastLine);

    final var joiner = new StringJoiner("\n", "", "\n");
    for (int lineNo = startOffset; lineNo <= endOffset; lineNo++) {
      joiner.add(lines[lineNo]);
    }

    return joiner.toString();
  }
}
