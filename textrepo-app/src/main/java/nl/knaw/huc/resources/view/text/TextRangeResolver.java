package nl.knaw.huc.resources.view.text;

import static java.lang.String.format;

import java.util.StringJoiner;
import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.view.RangeParam;

public class TextRangeResolver extends TextResolver<String> {
  private final RangeParam startLineParam;
  private final RangeParam startCharParam;
  private final RangeParam endLineParam;
  private final RangeParam endCharParam;

  public TextRangeResolver(RangeParam startLineParam, RangeParam startCharParam,
                           RangeParam endLineParam, RangeParam endCharParam) {
    this.startLineParam = startLineParam;
    this.startCharParam = startCharParam;
    this.endLineParam = endLineParam;
    this.endCharParam = endCharParam;
  }

  @Nonnull
  public String resolve(@Nonnull Contents contents) {
    final String text = contents.asUtf8String();

    final var lines = text.split(LINEBREAK_MATCHER);
    final var indexOfFirstLine = 0;
    final var startLineOffset = startLineParam.get().orElse(indexOfFirstLine);
    final var indexOfLastLine = lines.length - 1;
    final var endLineOffset = endLineParam.get().orElse(indexOfLastLine);
    checkOffsets(startLineOffset, endLineOffset, indexOfLastLine);

    final var joiner = new StringJoiner("\n");

    for (int curLineIndex = startLineOffset; curLineIndex <= endLineOffset; curLineIndex++) {
      final var curLine = lines[curLineIndex];
      final var indexOfFirstChar = 0;
      final var indexOfLastChar = curLine.length() - 1;

      final int startCharOffset;
      if (curLineIndex == startLineOffset) {
        startCharOffset = startCharParam.get().orElse(indexOfFirstChar);
        if (startCharOffset > curLine.length() - 1) {
          throw new BadRequestException(format("startCharOffset (%d) > max startLine offset (%d)",
              startCharOffset, curLine.length() - 1));
        }
      } else { // on all lines other than startLine
        startCharOffset = 0;
      }

      final int endCharOffset;
      if (curLineIndex == endLineOffset) {
        endCharOffset = endCharParam.get().orElse(indexOfLastChar);
        if (endCharOffset > indexOfLastChar) {
          throw new BadRequestException(
              format("endCharOffset (%d) > max endLine offset (%d)", endCharOffset,
                  indexOfLastChar));
        }
      } else { // on all lines other than endLine
        endCharOffset = indexOfLastChar;
      }

      joiner.add(curLine.substring(startCharOffset, endCharOffset + 1));
    }
    return joiner.toString();
  }
}
