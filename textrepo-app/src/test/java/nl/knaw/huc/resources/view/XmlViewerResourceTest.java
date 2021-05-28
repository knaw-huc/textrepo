package nl.knaw.huc.resources.view;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.core.Contents;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.stream.Stream;

import static nl.knaw.huc.resources.TestUtils.getResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
class XmlViewerResourceTest {
  private static final Contents CONTENTS = mock(Contents.class);

  private static final ResourceExtension resource = ResourceExtension
      .builder()
      .addResource(new XmlViewerResource(CONTENTS))
      .build();

  @Nonnull
  @SuppressWarnings("unused") // used as @MethodSource
  private static Stream<Arguments> provideCases() throws IOException {
    final var simple = getResourceAsString("xpath/simple.xml");
    final var complex = getResourceAsString("xpath/namespace.xml");
    return Stream.of(
        Arguments.of("/xpath/%2F%2FCreator", simple, 200, "[\"<Creator>P2PaLA-PRHLT</Creator>\"]"),
        Arguments.of("/xpath/%2F%2FTwice", simple, 200, "[\"<Twice>first</Twice>\",\"<Twice>second</Twice>\"]"),

        Arguments.of("/xpath/_/%2F%2F_:Creator", complex, 200, "[\"<Creator>P2PaLA-PRHLT</Creator>\"]"),
        Arguments.of("/xpath/_/%2F%2F_:Twice", complex, 200,
            "[\"<Twice>firstInDefaultNamespace</Twice>\",\"<Twice>secondInDefaultNamespace</Twice>\"]"),

        Arguments.of("/xpath/_/%2F%2Fxsi:Creator", complex, 200,
            "[\"<xsi:Creator>__xsi_test__P2PaLA-PRHLT</xsi:Creator>\"]"),
        Arguments.of("/xpath/_/%2F%2Fxsi:Twice", complex, 200,
            "[\"<xsi:Twice>firstInSpecificNamespace</xsi:Twice>\"," +
                "\"<xsi:Twice>secondInSpecificNamespace</xsi:Twice>\"]")
    );
  }

  @ParameterizedTest
  @MethodSource("provideCases")
  void testView_returnsExpected_forGivenXPath(String uri, String contents, int statusCode, String expected) {
    when(CONTENTS.asUtf8String()).thenReturn(contents);
    final var response = resource
        .client()
        .target(uri)
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(statusCode);

    final var body = response.readEntity(String.class);
    assertThat(body).isEqualTo(expected);
  }

}