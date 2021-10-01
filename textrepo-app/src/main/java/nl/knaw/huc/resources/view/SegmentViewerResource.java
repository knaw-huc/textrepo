package nl.knaw.huc.resources.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.view.segmented.TextAnchor;
import nl.knaw.huc.resources.view.segmented.TextSegments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("") // Without @Path("") this subresource is not resolved during tests
@Produces(APPLICATION_JSON)
public class SegmentViewerResource {
  private static final Logger log = LoggerFactory.getLogger(SegmentViewerResource.class);

  private final Contents contents;

  public SegmentViewerResource(Contents contents) {
    this.contents = contents;
  }

  @GET
  @Path("index/{startIndex}/{endIndex}")
  public List<String> getTextBetweenIndexAnchors(
      @PathParam("startIndex")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam startParam,
      @PathParam("endIndex")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam endParam
  ) {
    log.debug("getTextBetweenIndexAnchors: startIndex=[{}], endParam=[{}]", startParam, endParam);

    final var resolver = new SegmentResolver(startParam, endParam);
    return visitSegments(contents, resolver::resolve);
  }

  @GET
  @Path("index/{startIndex}/{startCharOffset}/{endIndex}/{endCharOffset}")
  public List<String> getSubstringBetweenIndexAnchors(
      @PathParam("startIndex")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam startIndex,
      @PathParam("startCharOffset")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam startCharOffset,
      @PathParam("endIndex")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam endIndex,
      @PathParam("endCharOffset")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam endCharOffset
  ) {
    log.debug(
        "getSubstringBetweenIndexAnchors: startIndex=[{}], startCharOffset=[{}], endIndex=[{}], endCharOffset=[{}]",
        startIndex, startCharOffset, endIndex, endCharOffset);

    return visitSegments(contents, textSegments -> {
      final var resolver = new SegmentResolver(startIndex, endIndex);

      final var fragment = resolver.resolve(textSegments);
      log.debug("Fragment element count: {}", fragment.size());

      reduceToSubstrings(fragment, startCharOffset, endCharOffset);

      return Collections.unmodifiableList(fragment);
    });
  }

  @GET
  @Path("anchor/{startAnchor}/{endAnchor}")
  public List<String> getTextBetweenNamedAnchors(
      @PathParam("startAnchor") @NotBlank String startAnchor,
      @PathParam("endAnchor") @NotBlank String endAnchor
  ) {
    log.debug("getTextBetweenNamedAnchors: startAnchor=[{}], endAnchor=[{}]", startAnchor, endAnchor);

    return visitSegments(contents, textSegments -> {
      final var anchors = textSegments.anchors;

      final List<String> fragment = getSelectedSegments(startAnchor, endAnchor, textSegments, anchors);
      log.debug("Fragment element count: {}", fragment.size());

      // Return a read-only view on the selection
      return Collections.unmodifiableList(fragment);
    });
  }

  @GET
  @Path("anchor/{startAnchor}/{startCharOffset}/{endAnchor}/{endCharOffset}")
  public List<String> getSubstringBetweenNamedAnchors(
      @PathParam("startAnchor")
      @ApiParam(required = true, example = "anchor-c961d9f2-2289-11ec-a58b-9b7020422d23")
      @NotNull
          String startAnchor,
      @PathParam("startCharOffset")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam startCharOffset,
      @PathParam("endAnchor")
      @ApiParam(required = true, example = "anchor-ca359c9c-2289-11ec-bef5-2ba86650726e")
      @NotNull
          String endAnchor,
      @PathParam("endCharOffset")
      @ApiParam(required = true, example = "0")
      @NotNull
          RangeParam endCharOffset
  ) {
    log.debug("getTextBetweenNamedAnchors: startAnchor=[{}], startCharOffset=[{}], endAnchor=[{}], endCharOffset=[{}]",
        startAnchor, startCharOffset, endAnchor, endCharOffset);

    return visitSegments(contents, textSegments -> {
      final var anchors = textSegments.anchors;
      final var fragment = getSelectedSegments(startAnchor, endAnchor, textSegments, anchors);

      reduceToSubstrings(fragment, startCharOffset, endCharOffset);

      return Collections.unmodifiableList(fragment);
    });
  }

  private void reduceToSubstrings(List<String> fragment, RangeParam startCharOffset, RangeParam endCharOffset) {
    final var first = fragment.get(0);
    final var firstIndex = startCharOffset.get().orElse(0);
    final var replaceFirst = first.substring(firstIndex);
    log.debug("first=[{}], firstIndex=[{}], replaceFirst=[{}]", first, firstIndex, replaceFirst);
    fragment.set(0, replaceFirst);

    final var last = fragment.get(fragment.size() - 1);
    final int lastIndex = endCharOffset.get().orElse(last.length() - 1);
    final var replaceLast = last.substring(0, lastIndex + 1);
    log.debug("last=[{}], lastIndex=[{}], replaceLast=[{}]", last, lastIndex, replaceLast);
    fragment.set(fragment.size() - 1, replaceLast);
  }

  private List<String> getSelectedSegments(String startAnchor, String endAnchor,
                                           TextSegments textSegments, TextAnchor[] anchors) {
    // Warning: O(n) ahead. Because the anchors are just listed in a json array, we're forced
    // to iterate the entire thing; converting it to a hash for this one-time lookup does not
    // help, obviously.
    // Let us at least attempt to find both start and end in the same run.
    OptionalInt startIndex = OptionalInt.empty();
    OptionalInt endIndex = OptionalInt.empty();
    for (var i = 0; i < anchors.length; i++) {
      final var id = anchors[i].id;
      if (startAnchor.equals(id)) {
        startIndex = OptionalInt.of(i);
      }
      if (endAnchor.equals(id)) {
        endIndex = OptionalInt.of(i);
      }

      if (startIndex.isPresent() && endIndex.isPresent()) {
        break; // early exit once both have been found
      }
    }

    if (startIndex.isEmpty()) {
      throw new NotFoundException(String.format("start anchor [%s] not found", startAnchor));
    }

    if (endIndex.isEmpty()) {
      throw new NotFoundException(String.format("end anchor [%s] not found", endAnchor));
    }

    // As anchors are just Strings, let's be lenient if caller switches up 'start' and 'end'.
    final int from = Math.min(startIndex.getAsInt(), endIndex.getAsInt());
    final int upto = Math.max(startIndex.getAsInt(), endIndex.getAsInt()) + 1;

    log.debug("Sublist indexes: from=[{}], upto=[{}]", from, upto);

    // Do not copy elements, but create a 'view' on the selected array elements
    return Arrays.asList(textSegments.segments).subList(from, upto);
  }

  private <T> T visitSegments(Contents contents, Function<TextSegments, T> visitor) {
    final ObjectMapper mapper = new ObjectMapper();
    try {
      final var json = contents.asUtf8String();
      return visitor.apply(mapper.readValue(json, TextSegments.class));
    } catch (JsonProcessingException e) {
      log.debug("failed to parse contents as json: {}", e.toString());
      throw new BadRequestException("contents are not valid json");
    }
  }
}
