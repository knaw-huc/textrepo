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
    if (startOffset < 0) {
      throw new BadRequestException(format("startOffset must be >= 0, but is: %d", startOffset));
    }
    if (endOffset < startOffset) {
      throw new BadRequestException(
          format("endOffset must be >= startOffset (%d), but is: %d", startOffset, endOffset));
    }
    final var text = contents.asUtf8String();
    final var maxOffset = text.length() - 1;
    if (startOffset >= text.length()) {
      throw new BadRequestException(
          format("startOffset must be <= %d, but is: %d", maxOffset, startOffset)
      );
    }
    if (endOffset >= text.length()) {
      throw new BadRequestException(
          format("endOffset must be <= %d, but is: %d", maxOffset, endOffset)
      );
    }
    return Response.ok(text.substring(startOffset, endOffset + 1)).build();
  }

  @GET
  @Path("lines/{startOffset}/{endOffset}")
  public Response getLineRange(
      @PathParam("startOffset") @NotNull int startOffset,
      @PathParam("endOffset") @NotNull int endOffset
  ) {
    log.debug("getLineRange: sha224={}, range=[{}:{}]", contents.getSha224(), startOffset, endOffset);
    if (startOffset < 0) {
      throw new BadRequestException(format("startOffset must be >= 0, but is: %d", startOffset));
    }
    if (endOffset < startOffset) {
      throw new BadRequestException(
          format("endOffset must be >= startOffset (%d), but is: %d", startOffset, endOffset));
    }
    var lines = contents.asUtf8String().split("\\R");
    if (startOffset >= lines.length || endOffset >= lines.length) {
      throw new BadRequestException(
          format("Requested line interval [%d:%d] exceeds source text line count: %d",
              startOffset, endOffset, lines.length)
      );
    }
    var joiner = new StringJoiner("\n");  // XXX: this may change whatever unicode newline there was before
    for (int lineNo = startOffset; lineNo <= endOffset; lineNo++) {
      joiner.add(lines[lineNo]);
    }
    return Response.ok(joiner.toString()).build();
  }
}
