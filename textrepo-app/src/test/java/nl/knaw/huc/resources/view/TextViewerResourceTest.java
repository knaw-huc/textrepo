package nl.knaw.huc.resources.view;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.ws.rs.core.Response;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class TextViewerResourceTest {
  private static final int CONTENT_DECOMPRESSION_LIMIT = 10;
  private static final ContentsHelper CONTENTS_HELPER = new ContentsHelper(CONTENT_DECOMPRESSION_LIMIT);

  private static final String EXAMPLE = "0123456789abcdef";
  private static final Contents CONTENTS = Contents.fromBytes(EXAMPLE.getBytes(UTF_8));

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addResource(new TextViewerResource(CONTENTS, CONTENTS_HELPER))
      .build();

  @SuppressWarnings("unused") // used as @MethodSource
  private static Stream<Arguments> provideCasesForChars() {
    return Stream.of(
        Arguments.of("/chars/-1/4", 400, ">= 0"),
        Arguments.of("/chars/-1/full", 400, ">= 0"),
        Arguments.of("/chars/4/16", 400, "<= 15"),
        Arguments.of("/chars/full/16", 400, "<= 15"),
        Arguments.of("/chars/4/0", 400, "endOffset must be >= startOffset"),
        Arguments.of("/chars/8/4", 400, "endOffset must be >= startOffset"),
        Arguments.of("/chars/15/8", 400, "endOffset must be >= startOffset"),
        Arguments.of("/chars/4/8", 200, "45678"),
        Arguments.of("/chars/0/4", 200, "01234"),
        Arguments.of("/chars/4/15", 200, "456789abcdef"),
        Arguments.of("/chars/0/full", 200, EXAMPLE),
        Arguments.of("/chars/full/15", 200, EXAMPLE),
        Arguments.of("/chars/full/full", 200, EXAMPLE)
    );
  }

  @ParameterizedTest
  @MethodSource("provideCasesForChars")
  void getChars_returnsExpectedStatus_and_Contents(String uri, int statusCode, String expected) {
    final var response = get(uri);
    assertThat(response.getStatus()).isEqualTo(statusCode);
    final var body = response.readEntity(String.class);
    if (statusCode == 200) {
      assertThat(body).isEqualTo(expected);
    } else if (statusCode == 400) {
      final var json = JsonPath.parse(body);
      assertThat(json.read("$.code", Integer.class)).isEqualTo(statusCode);
      assertThat(json.<String>read("$.message")).contains(expected);
    }
  }

  private Response get(String uri) {
    return resource.client().target(uri).request().get();
  }

  @Test
  void getLines() {
  }

  @Test
  void getRange() {
  }
}