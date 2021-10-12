package nl.knaw.huc.resources.view.segmented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.WebApplicationException;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SegmentedTextRegionTest {

  @Test
  public void testNullInput_isRejected() {
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> new SegmentedTextRegionParam(null))
        .withMessage("HTTP 400 Bad Request");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "a", "a,b,c", "a,b,c,d,e"})
  public void testIllegalInput_isRejected(String input) {
    assertThatExceptionOfType(WebApplicationException.class)
        .isThrownBy(() -> new SegmentedTextRegionParam(input))
        .withMessage("HTTP 400 Bad Request");
  }

  @ParameterizedTest
  @ValueSource(strings = {"sa,ea", "sa,full,ea,full"})
  public void testAnchorOnly_orFullOffset_isParsed(String input) {
    var it = new SegmentedTextRegionParam(input).get();
    assertThat(it.getStartAnchor()).isEqualTo("sa");
    assertThat(it.getEndAnchor()).isEqualTo("ea");

    assertThat(it.getStartOffset()).isEqualTo(OptionalInt.empty());
    assertThat(it.getEndOffset()).isEqualTo(OptionalInt.empty());
  }

  @Test
  public void testAnchorAndOffset_isParsed() {
    var it = new SegmentedTextRegionParam("sa,3,ea,7").get();
    assertThat(it.getStartAnchor()).isEqualTo("sa");
    assertThat(it.getEndAnchor()).isEqualTo("ea");

    assertThat(it.getStartOffset()).isEqualTo(OptionalInt.of(3));
    assertThat(it.getEndOffset()).isEqualTo(OptionalInt.of(7));
  }

}