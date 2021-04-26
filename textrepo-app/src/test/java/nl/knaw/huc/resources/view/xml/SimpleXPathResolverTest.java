package nl.knaw.huc.resources.view.xml;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class SimpleXPathResolverTest {

  @Test
  void testSimpleResolver_findsExcerpt_whenFoundInFile() throws IOException {
    final var bytes = TestUtils.getResourceAsBytes("xpath/simple.xml");
    final var contents = Contents.fromBytes(bytes);
    System.err.printf("Contents: %s%n%n", contents);
  }

}