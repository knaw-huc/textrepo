package nl.knaw.huc.resources.view.text;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.view.RangeParam;

import javax.annotation.Nonnull;

public class TextCharsResolver extends TextResolver<String> {
  private final RangeParam startParam;
  private final RangeParam endParam;

  public TextCharsResolver(RangeParam startParam, RangeParam endParam) {
    this.startParam = startParam;
    this.endParam = endParam;
  }

  @Override
  @Nonnull
  public String resolve(@Nonnull Contents contents) {
    final String text = contents.asUtf8String();

    final var indexOfFirstChar = 0;
    final var startOffset = startParam.get().orElse(indexOfFirstChar);

    final var indexOfLastChar = text.length() - 1;
    final var endOffset = endParam.get().orElse(indexOfLastChar);

    // "endOffset +1" because substring goes to end (exclusive) and we want end (inclusive)
    checkOffsets(startOffset, endOffset, indexOfLastChar);

    return text.substring(startOffset, endOffset + 1);
  }

}
