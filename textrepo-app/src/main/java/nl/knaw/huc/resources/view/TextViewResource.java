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
  public Response getCharRange(
      @PathParam("startOffset") @NotNull RangeParam startParam,
      @PathParam("endOffset") @NotNull RangeParam endParam
  ) {
    log.debug("getCharRange: sha224={}, range=[{}:{}]", contents.getSha224(), startParam, endParam);

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
  public Response getLineRange(
      @PathParam("startOffset") @NotNull RangeParam startParam,
      @PathParam("endOffset") @NotNull RangeParam endParam
  ) {
    log.debug("getLineRange: sha224={}, range=[{}:{}]", contents.getSha224(), startParam, endParam);

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
