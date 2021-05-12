package nl.knaw.huc.resources.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huc.core.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

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
  @Path("anchor/{startAnchor}/{endAnchor}")
  public List<String> getTextBetweenAnchors(
      @PathParam("startAnchor") @NotBlank String startAnchor,
      @PathParam("endAnchor") @NotBlank String endAnchor
  ) {
    log.debug("Get text between start [{}] and end [{}] anchors", startAnchor, endAnchor);

    return visitSegments(contents, textSegments -> {
      OptionalInt startIndex = OptionalInt.empty();
      OptionalInt endIndex = OptionalInt.empty();
      final var anchors = textSegments.anchors;

      // Warning: O(n) ahead. Because the anchors are just listed in a json array, we're forced
      // to iterate the entire thing; converting it to a hash for this one-time lookup does not
      // help, obviously.
      // Let's at least attempt to find both start and end in the same run.
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

      // As anchors are just Strings, let's be lenient if caller switches up start and end.
      final var start = Math.min(startIndex.getAsInt(), endIndex.getAsInt());
      final var end = Math.max(startIndex.getAsInt(), endIndex.getAsInt());

      final List<String> selectedSegments = new ArrayList<>(end - start + 1);
      IntStream.rangeClosed(start, end).forEach(i -> selectedSegments.add(textSegments.segments[i]));

      return selectedSegments;
    });
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

  static class TextSegments {
    @JsonProperty("resource_id")
    public String resourceId;

    @JsonProperty("_ordered_segments")
    public String[] segments;

    @JsonProperty("_anchors")
    public TextAnchor[] anchors;
  }

  static class TextAnchor {
    @JsonProperty("identifier")
    public String id;

    @JsonProperty("sequence_number")
    public long index;
  }
}
