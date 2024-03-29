package nl.knaw.huc.resources.view.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.TestUtils;
import org.junit.jupiter.api.Test;

class NamespaceAwareXpathResolverTest {

  @Test
  void testResolver_findsNothing_whenNotInFile() throws IOException {
    final var sut = new NamespaceAwareXpathResolver("_", "//NowhereToBeFound");
    final var res = sut.resolve(namespacedContents());
    assertThat(res).isEmpty();
  }

  @Test
  void testResolver_findsOne_whenSearchingDefaultNamespace() throws IOException {
    final var sut = new NamespaceAwareXpathResolver("defNS", "//defNS:Creator");
    final var res = sut.resolve(namespacedContents());
    assertThat(res.size()).isEqualTo(1);
    assertThat(res).containsExactly("<Creator>P2PaLA-PRHLT</Creator>");
  }

  @Test
  void testResolver_findsAll_whenSearchingDefaultNamespace() throws IOException {
    final var sut = new NamespaceAwareXpathResolver("defNS", "//defNS:Twice");
    final var res = sut.resolve(namespacedContents());
    assertThat(res.size()).isEqualTo(2);
    assertThat(res).containsExactly(
        "<Twice>firstInDefaultNamespace</Twice>",
        "<Twice>secondInDefaultNamespace</Twice>");
  }

  @Test
  void testResolver_findsOne_whenSearchingImplicitNamespaceFromSource() throws IOException {
    final var sut = new NamespaceAwareXpathResolver("_", "//xsi:Creator");
    final var res = sut.resolve(namespacedContents());
    assertThat(res.size()).isEqualTo(1);
    assertThat(res).containsExactly("<xsi:Creator>__xsi_test__P2PaLA-PRHLT</xsi:Creator>");
  }

  @Test
  void testResolver_findsAll_whenSearchingImplicitNamespaceFromSource() throws IOException {
    final var sut = new NamespaceAwareXpathResolver("_", "//xsi:Twice");
    final var res = sut.resolve(namespacedContents());
    assertThat(res.size()).isEqualTo(2);
    assertThat(res).containsExactly(
        "<xsi:Twice>firstInSpecificNamespace</xsi:Twice>",
        "<xsi:Twice>secondInSpecificNamespace</xsi:Twice>");
  }

  private Contents namespacedContents() throws IOException {
    final var bytes = TestUtils.getResourceAsBytes("xpath/namespace.xml");
    return Contents.fromBytes(bytes);
  }

}