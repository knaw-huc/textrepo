package nl.knaw.huc.resources.view;

import io.swagger.annotations.Api;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.StringJoiner;

import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

@Api(tags = {"versions", "contents", "view"})
public class TextViewerResource {
  private static final String LINEBREAK_MATCHER = "\\R";
  private static final Logger log = LoggerFactory.getLogger(TextViewerResource.class);

  private final Contents contents;
  private final ContentsHelper contentsHelper;

  public TextViewerResource(Contents contents, ContentsHelper contentsHelper) {
    this.contents = contents;
    this.contentsHelper = contentsHelper;
  }

  @GET
  @Path("chars/{startOffset}/{endOffset}")
  public Response getChars(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
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

    return asPlainTextAttachment(result, acceptEncoding);
  }

  @GET
  @Path("lines/{startOffset}/{endOffset}")
  public Response getLines(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
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

    final var joiner = new StringJoiner("\n", "", "\n");
    for (int lineNo = startOffset; lineNo <= endOffset; lineNo++) {
      joiner.add(lines[lineNo]);
    }

    return asPlainTextAttachment(joiner.toString(), acceptEncoding);
  }

  @GET
  @Path("range/{startLineOffset}/{startCharOffset}/{endLineOffset}/{endCharOffset}")
  // E.g., from line 3, char 12 (on that line) up to and including line 8, char 4 (on that line)
  public Response getRange(
      @HeaderParam(ACCEPT_ENCODING) String acceptEncoding,
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
    for (int curLineIndex = startLineOffset; curLineIndex <= endLineOffset; curLineIndex++) {
      final var curLine = lines[curLineIndex];
      final String lineToAdd;
      if (curLineIndex == startLineOffset) {
        final var indexOfFirstChar = 0;
        final var startCharOffset = startCharParam.get().orElse(indexOfFirstChar);
        if (startCharOffset > curLine.length() - 1) {
          badRequest("startCharOffset (%d) > max startLine offset (%d)", startCharOffset, curLine.length() - 1);
        }
        lineToAdd = curLine.substring(startCharOffset);
      } else if (curLineIndex == endLineOffset) {
        final var indexOfLastChar = curLine.length() - 1;
        final var endCharOffset = endCharParam.get().orElse(indexOfLastChar);
        if (endCharOffset > curLine.length() - 1) {
          badRequest("endCharOffset (%d) > max endLine offset (%d)", endCharOffset, curLine.length() - 1);
        }
        lineToAdd = curLine.substring(0, endCharOffset + 1);
      } else {
        lineToAdd = curLine;
      }
      joiner.add(lineToAdd);
    }

    return asPlainTextAttachment(joiner.toString(), acceptEncoding);
  }

  private void checkOffsets(int start, int end, int limit) {
    if (end < start) {
      badRequest("endOffset must be >= startOffset (%d), but is: %d", start, end);
    }

    if (start > limit) {
      badRequest("startOffset is limited by source text; must be <= %d, but is: %d", limit, start);
    }

    if (end > limit) {
      badRequest("endOffset is limited by source text; must be <= %d, but is: %d", limit, end);
    }
  }

  private void badRequest(String format, Object... args) {
    throw new BadRequestException(String.format(format, args));
  }

  private Response asPlainTextAttachment(String contents, String acceptEncoding) {
    return contentsHelper.asAttachment(contents, acceptEncoding)
                         .header(CONTENT_TYPE, TEXT_PLAIN_TYPE)
                         .build();
  }
}
