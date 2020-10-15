package nl.knaw.huc.resources;

import org.junit.jupiter.api.Test;

import static nl.knaw.huc.resources.HeaderLink.Rel.ORIGINAL;
import static nl.knaw.huc.resources.HeaderLink.Uri.FILE;
import static org.assertj.core.api.Assertions.assertThat;

public class HeaderLinkTest {
  @Test
  public void createLink_shouldUseCorrectUrl() {
    var fileId = "b626a11a-5fdd-4a4b-9351-99724f7d15a5";
    var link = HeaderLink.create(ORIGINAL, FILE, fileId);
    assertThat(link.toString()).isEqualTo("</rest/files/" + fileId + ">; rel=\"original\"");
  }

}
