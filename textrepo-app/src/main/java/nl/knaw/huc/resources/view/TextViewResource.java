package nl.knaw.huc.resources.view;

import nl.knaw.huc.core.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.StringJoiner;

import static java.lang.String.format;

public class TextViewResource {
  private static final String LINEBREAK_MATCHER = "\\R";
  private static final Logger log = LoggerFactory.getLogger(TextViewResource.class);

  private final Contents contents;

  public TextViewResource(Contents contents) {
    this.contents = contents;
  }

  @GET
  @Path("chars/{startOffset}/{endOffset}")
  public Response getChars(
      @PathParam("startOffset") @NotNull RangeParam startParam,
      @PathParam("endOffset") @NotNull RangeParam endParam
  ) {
    log.debug("getChars: startParam=[{}], endParam=[{}]", startParam, endParam);

    final var text = contents.asUtf8String();
    final var indexOfFirstChar = 0;
    final var indexOfLastChar = text.length() - 1;
    final var startOffset = startParam.get().orElse(indexOfFirstChar);
    final var endOffset = endParam.get().orElse(indexOfLastChar);

    checkOffsets(startOffset, endOffset, indexOfLastChar);

    // "endOffset +1" because substring goes to end (exclusive) and we want end (inclusive)
    final var result = text.substring(startOffset, endOffset + 1);

    return Response.ok(result).build();
  }

  @GET
  @Path("lines/{startOffset}/{endOffset}")
  public Response getLines(
      @PathParam("startOffset") @NotNull RangeParam startParam,
      @PathParam("endOffset") @NotNull RangeParam endParam
  ) {
    log.debug("getLines: startParam=[{}], endParam=[{}]", startParam, endParam);

    final var lines = contents.asUtf8String().split(LINEBREAK_MATCHER);
    final var indexOfFirstLine = 0;
    final var indexOfLastLine = lines.length - 1;
    final var startOffset = startParam.get().orElse(indexOfFirstLine);
    final var endOffset = endParam.get().orElse(indexOfLastLine);

    checkOffsets(startOffset, endOffset, indexOfLastLine);

    var joiner = new StringJoiner("\n", "", "\n");
    for (int lineNo = startOffset; lineNo <= endOffset; lineNo++) {
      joiner.add(lines[lineNo]);
    }
    final var result = joiner.toString();

    return Response.ok(result).build();
  }

  @GET
  @Path("range/{startLineOffset}/{startCharOffset}/{endLineOffset}/{endCharOffset}")
  // E.g., from line 3, char 12 (on that line) up to and including line 8, char 4 (on that line)
  public Response getRange(
      @PathParam("startLineOffset") @NotNull RangeParam startLineParam,
      @PathParam("startCharOffset") @NotNull RangeParam startCharParam,
      @PathParam("endLineOffset") @NotNull RangeParam endLineParam,
      @PathParam("endCharOffset") @NotNull RangeParam endCharParam
  ) {
    log.debug("getLines: startLineParam=[{}], startCharParam=[{}], endLineParam=[{}], endCharParam=[{}]",
        startLineParam, startCharParam, endLineParam, endCharParam);

    final var lines = contents.asUtf8String().split(LINEBREAK_MATCHER);
    final var indexOfFirstLine = 0;
    final var startLineOffset = startLineParam.get().orElse(indexOfFirstLine);
    final var indexOfLastLine = lines.length - 1;
    final var endLineOffset = endLineParam.get().orElse(indexOfLastLine);
    checkOffsets(startLineOffset, endLineOffset, indexOfLastLine);

    final var joiner = new StringJoiner("\n", "", "\n");
    for (int lineNo = startLineOffset; lineNo <= endLineOffset; lineNo++) {
      final var curLine = lines[lineNo];
      final String lineToAdd;
      if (lineNo == startLineOffset) {
        final var indexOfFirstChar = 0;
        final var startCharOffset = startCharParam.get().orElse(indexOfFirstChar);
        if (startCharOffset > curLine.length() - 1) {
          throw new BadRequestException(
              format("startCharOffset (%d) > max startLine offset (%d)", startCharOffset, curLine.length() - 1));
        }
        lineToAdd = curLine.substring(startCharOffset);
      } else if (lineNo == endLineOffset) {
        final var indexOfLastChar = curLine.length() - 1;
        final var endCharOffset = endCharParam.get().orElse(indexOfLastChar);
        if (endCharOffset > curLine.length() - 1) {
          throw new BadRequestException(
              format("endCharOffset (%d) > max endLine offset (%d)", endCharOffset, curLine.length() - 1));
        }
        lineToAdd = curLine.substring(0, endCharOffset + 1);
      } else {
        lineToAdd = curLine;
      }
      joiner.add(lineToAdd);
    }

    final var result = joiner.toString();

    return Response.ok(result).build();
  }

  private void checkOffsets(int start, int end, int limit) {
    if (end < start) {
      throw new BadRequestException(
          format("endOffset must be >= startOffset (%d), but is: %d", start, end));
    }

    if (start > limit) {
      throw new BadRequestException(
          format("startOffset is limited by source text; must be <= %d, but is: %d", limit, start));
    }

    if (end > limit) {
      throw new BadRequestException(
          format("endOffset is limited by source text; must be <= %d, but is: %d", limit, end));
    }
  }
}
