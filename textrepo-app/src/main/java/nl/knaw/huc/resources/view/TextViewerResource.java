package nl.knaw.huc.resources.view;

import io.swagger.annotations.Api;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.StringJoiner;

import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

/*
 * This class is a Jersey sub-resource. It can be instantiated in other Jersey resources which
 * handle the first part of the URI which is responsible for fetching Contents and selecting
 * which 'view' is requested.
 * <p>
 * So in TextViewerResource we can abstract from whatever it was that yielded the Contents,
 * and what mechanism determined that 'we' should be the object handling the view details.
 * <p>
 * Then, TextViewerResource will take care of the rest of the URI, addressing the following:
 * - …/chars/{startOffset}/{endOffset}
 * - …/lines/{startLine}/{endLine}
 * - …range/{startLine}/startCharOffset}/{endLine}/{endCharOffset}
 *
 * @see nl.knaw.huc.resources.view.ViewBuilderFactory
 * @see nl.knaw.huc.resources.view.ViewVersionResource
 */
@Api(tags = {"versions", "contents", "view"})
@Path("") // Without @Path("") this subresource is not resolved during tests
public class TextViewerResource {
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

    final var result = new PlainTextViewCharsResolver(startParam, endParam)
        .resolve(contents.asUtf8String());

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

    final var result = new PlainTextViewLinesResolver(startParam, endParam)
        .resolve(contents.asUtf8String());

    return asPlainTextAttachment(result, acceptEncoding);
  }

  static abstract class PlainTextViewResolver {
    public static final String LINEBREAK_MATCHER = "\\R";

    public abstract String resolve(String text);

    protected void checkOffsets(int start, int end, int limit) {
      if (end < start) {
        throw new BadRequestException(
            format("endOffset must be >= startOffset (%d), but is: %d", start, end));
      }

      if (start > limit) {
        throw new NotFoundException(
            format("startOffset is limited by source text; must be <= %d, but is: %d", limit, start));
      }

      if (end > limit) {
        throw new NotFoundException(
            format("endOffset is limited by source text; must be <= %d, but is: %d", limit, end));
      }
    }

  }

  static class PlainTextViewCharsResolver extends PlainTextViewResolver {
    private final RangeParam startParam;
    private final RangeParam endParam;

    PlainTextViewCharsResolver(RangeParam startParam, RangeParam endParam) {
      this.startParam = startParam;
      this.endParam = endParam;
    }

    @Override
    @Nonnull
    public String resolve(@Nonnull String text) {
      final var indexOfFirstChar = 0;
      final var startOffset = startParam.get().orElse(indexOfFirstChar);

      final var indexOfLastChar = text.length() - 1;
      final var endOffset = endParam.get().orElse(indexOfLastChar);

      // "endOffset +1" because substring goes to end (exclusive) and we want end (inclusive)
      checkOffsets(startOffset, endOffset, indexOfLastChar);

      return text.substring(startOffset, endOffset + 1);
    }

  }

  static class PlainTextViewLinesResolver extends PlainTextViewResolver {
    private final RangeParam startParam;
    private final RangeParam endParam;

    private PlainTextViewLinesResolver(RangeParam startParam, RangeParam endParam) {
      this.startParam = startParam;
      this.endParam = endParam;
    }

    @Override
    @Nonnull
    public String resolve(@Nonnull String text) {
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

  static class PlainTextViewRangeResolver extends PlainTextViewResolver {
    private final RangeParam startLineParam;
    private final RangeParam startCharParam;
    private final RangeParam endLineParam;
    private final RangeParam endCharParam;

    PlainTextViewRangeResolver(RangeParam startLineParam, RangeParam startCharParam,
                               RangeParam endLineParam, RangeParam endCharParam) {
      this.startLineParam = startLineParam;
      this.startCharParam = startCharParam;
      this.endLineParam = endLineParam;
      this.endCharParam = endCharParam;
    }

    @Nonnull
    public String resolve(@Nonnull String text) {
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
            throw new BadRequestException(format("endCharOffset (%d) > max endLine offset (%d)", endCharOffset,
                indexOfLastChar));
          }
        } else { // on all lines other than startLine
          endCharOffset = indexOfLastChar;
        }

        joiner.add(curLine.substring(startCharOffset, endCharOffset + 1));
      }
      return joiner.toString();
    }
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

    final var resolver = new PlainTextViewRangeResolver(startLineParam, startCharParam, endLineParam, endCharParam);
    final var result = resolver.resolve(contents.asUtf8String());

    return asPlainTextAttachment(result, acceptEncoding);
  }

  private Response asPlainTextAttachment(String contents, String acceptEncoding) {
    return contentsHelper.asAttachment(contents, acceptEncoding)
                         .header(CONTENT_TYPE, TEXT_PLAIN_TYPE)
                         .build();
  }
}
