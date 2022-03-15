package nl.knaw.huc.resources.view.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.TestUtils;
import org.junit.jupiter.api.Test;

class SimpleXpathResolverTest {

  @Test
  void testSimpleResolver_findsNothing_whenNotInFile() throws IOException {
    final var sut = new SimpleXpathResolver("//NowhereToBeFound");
    final var result = sut.resolve(someSimpleContents());
    assertThat(result).isEmpty();
  }

  @Test
  void testSimpleResolver_findsExcerpt_whenFoundInFile() throws IOException {
    final var sut = new SimpleXpathResolver("//Creator");
    final var result = sut.resolve(someSimpleContents());
    assertThat(result.size()).isEqualTo(1);
    assertThat(result).containsExactly("<Creator>P2PaLA-PRHLT</Creator>");
  }

  @Test
  void testSimpleResolver_findsAll_whenMultiplesInFile() throws IOException {
    final var sut = new SimpleXpathResolver("//Twice");
    final var result = sut.resolve(someSimpleContents());
    assertThat(result.size()).isEqualTo(2);
    assertThat(result).contains("<Twice>first</Twice>", "<Twice>second</Twice>");
  }

  private Contents someSimpleContents() throws IOException {
    final var bytes = TestUtils.getResourceAsBytes("xpath/simple.xml");
    return Contents.fromBytes(bytes);
  }

}