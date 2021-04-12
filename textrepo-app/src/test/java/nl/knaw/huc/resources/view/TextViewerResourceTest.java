package nl.knaw.huc.resources.view;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.helpers.ContentsHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class TextViewerResourceTest {
  private static final int CONTENT_DECOMPRESSION_LIMIT = 10;
  private static final ContentsHelper CONTENTS_HELPER = new ContentsHelper(CONTENT_DECOMPRESSION_LIMIT);

  private static final Contents CONTENTS = Contents.fromBytes("0123456789abcdef".getBytes(UTF_8));

  public static final ResourceExtension resource = ResourceExtension
      .builder()
      .addResource(new TextViewerResource(CONTENTS, CONTENTS_HELPER))
      .build();

  @Test
  void getChars_usesInclusiveIndexing() {
    var response = resource
        .client()
        .target("/chars/4/8")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    var body = response.readEntity(String.class);
    assertThat(body.length()).isEqualTo(5);
    assertThat(body).isEqualTo(CONTENTS.asUtf8String().substring(4, 9));
  }

  @Test
  void getChars_substringWorksForLowIndex() {
    var response = resource
        .client()
        .target("/chars/0/4")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    var body = response.readEntity(String.class);
    assertThat(body).isEqualTo(CONTENTS.asUtf8String().substring(0, 5));
  }

  @Test
  void getChars_substringWorksForHighIndex() {
    var response = resource
        .client()
        .target("/chars/4/15")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    var body = response.readEntity(String.class);
    assertThat(body).isEqualTo(CONTENTS.asUtf8String().substring(4));
  }

  @Test
  void getChars_understands_full_full_notation() {
    var response = resource
        .client()
        .target("/chars/full/full")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    var body = response.readEntity(String.class);
    assertThat(body).isEqualTo(CONTENTS.asUtf8String());
  }

  @Test
  void getLines() {
  }

  @Test
  void getRange() {
  }
}