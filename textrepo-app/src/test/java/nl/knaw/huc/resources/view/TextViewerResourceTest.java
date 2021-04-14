package nl.knaw.huc.resources.view;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class TextViewerResourceTest {
  private static final int CONTENT_DECOMPRESSION_LIMIT = 10;
  private static final ContentsHelper CONTENTS_HELPER = new ContentsHelper(CONTENT_DECOMPRESSION_LIMIT);

  private static final String CHARS = "0123456789abcdef";
  private static final String LINE = CHARS + "\n";
  private static final String LINES = LINE + LINE;

  // Note that we construct CONTENTS to be WITHOUT trailing '\n'
  // Yet we DO expect the '/lines' view to a return trailing '\n'
  // This test is written to expect that normalised behaviour.
  private static final Contents CONTENTS = Contents.fromBytes((LINE + CHARS).getBytes(UTF_8));

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addResource(new TextViewerResource(CONTENTS, CONTENTS_HELPER))
      .build();

  @SuppressWarnings("unused") // used as @MethodSource
  private static Stream<Arguments> provideCases() {
    return Stream.of(
        Arguments.of("/chars/-1/4", 400, ">= 0, or 'full'"),
        Arguments.of("/chars/-1/full", 400, ">= 0, or 'full'"),
        Arguments.of("/chars/4/1000", 404, "must be <="),
        Arguments.of("/chars/full/1000", 404, "must be <= 32"),
        Arguments.of("/chars/4/0", 400, "endOffset must be >= startOffset"),
        Arguments.of("/chars/8/4", 400, "endOffset must be >= startOffset"),
        Arguments.of("/chars/15/8", 400, "endOffset must be >= startOffset"),
        Arguments.of("/chars/0/0", 200, "0"),
        Arguments.of("/chars/15/15", 200, "f"),
        Arguments.of("/chars/32/32", 200, "f"),
        Arguments.of("/chars/33/33", 404, "must be <= 32"),
        Arguments.of("/chars/4/8", 200, "45678"),
        Arguments.of("/chars/0/4", 200, "01234"),
        Arguments.of("/chars/21/32", 200, "456789abcdef"),
        Arguments.of("/chars/0/full", 200, CHARS + "\n" + CHARS),
        Arguments.of(format("/chars/full/%d", (CHARS + "\n" + CHARS).length() - 1), 200, CHARS + "\n" + CHARS),
        Arguments.of("/chars/full/full", 200, CHARS + "\n" + CHARS),

        Arguments.of("/lines/-1/4", 400, ">= 0, or 'full'"),
        Arguments.of("/lines/-1/full", 400, ">= 0, or 'full'"),
        Arguments.of("/lines/4/0", 400, "endOffset must be >= startOffset"),
        Arguments.of("/lines/8/4", 400, "endOffset must be >= startOffset"),
        Arguments.of("/lines/15/8", 400, "endOffset must be >= startOffset"),
        Arguments.of("/lines/0/2", 404, "must be <= 1"),
        Arguments.of("/lines/2/4", 404, "must be <= 1"),
        Arguments.of("/lines/0/0", 200, LINE),
        Arguments.of("/lines/1/1", 200, LINE),
        Arguments.of("/lines/0/1", 200, LINES),
        Arguments.of("/lines/0/full", 200, LINES),
        Arguments.of("/lines/full/1", 200, LINES),
        Arguments.of("/lines/full/full", 200, LINES),

        Arguments.of("/range/0/0/0/15", 200, CHARS),
        Arguments.of("/range/0/0/0/0", 200, "0"),
        Arguments.of("/range/0/15/0/15", 200, "f"),
        Arguments.of("/range/1/0/1/15", 200, CHARS),
        Arguments.of("/range/0/0/1/15", 200, LINE + CHARS),
        Arguments.of("/range/full/full/full/full", 200, LINE + CHARS)
    );
  }

  @ParameterizedTest
  @MethodSource("provideCases")
  void getTextView_returnsExpectedStatus_and_Contents(String uri, int statusCode, String expected) {
    final var response = resource
        .client()
        .target(uri)
        .request()
        .get();

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

}