package nl.knaw.huc.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class HeaderLinkUtilTest {

  @Test
  public void extractId_shouldExtractUuid() {
    var expected = UUID.randomUUID();
    var extracted = HeaderLinkUtil.extractId("</rest/files/" + expected + ">; rel=\"original\"");
    assertThat(extracted).isEqualTo(expected);
  }

}
