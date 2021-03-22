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
  private static final Logger log = LoggerFactory.getLogger(TextViewResource.class);

  private final Contents contents;

  public TextViewResource(Contents contents) {
    this.contents = contents;
  }

  @GET
  @Path("chars/{startOffset}/{endOffset}")
  public Response getCharRange(
      @PathParam("startOffset") @NotNull int startOffset,
      @PathParam("endOffset") @NotNull int endOffset
  ) {
    log.debug("getCharRange: sha224={}, range=[{}:{}]", contents.getSha224(), startOffset, endOffset);
    checkOffsets(startOffset, endOffset);

    final var text = contents.asUtf8String();
    checkLimits(startOffset, endOffset, text.length() - 1);

    return Response.ok(text.substring(startOffset, endOffset + 1)).build();
  }

  @GET
  @Path("lines/{startOffset}/{endOffset}")
  public Response getLineRange(
      @PathParam("startOffset") @NotNull int startOffset,
      @PathParam("endOffset") @NotNull int endOffset
  ) {
    log.debug("getLineRange: sha224={}, range=[{}:{}]", contents.getSha224(), startOffset, endOffset);
    checkOffsets(startOffset, endOffset);

    var lines = contents.asUtf8String().split("\\R");
    checkLimits(startOffset, endOffset, lines.length - 1);

    var joiner = new StringJoiner("\n", "", "\n");
    for (int lineNo = startOffset; lineNo <= endOffset; lineNo++) {
      joiner.add(lines[lineNo]);
    }

    return Response.ok(joiner.toString()).build();
  }

  private void checkOffsets(int start, int end) {
    if (start < 0) {
      throw new BadRequestException(
          format("startOffset must be >= 0, but is: %d", start));
    }

    if (end < start) {
      throw new BadRequestException(
          format("endOffset must be >= startOffset (%d), but is: %d", start, end));
    }
  }

  private void checkLimits(int start, int end, int limit) {
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
