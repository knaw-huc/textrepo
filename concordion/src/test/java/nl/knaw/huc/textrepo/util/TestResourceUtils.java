package nl.knaw.huc.textrepo.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TestResourceUtils {
  public static String getResourceAsString(String resourcePath) throws IOException {
    var stream = getInputStream(resourcePath);
    return IOUtils.toString(stream, UTF_8);
  }

  private static InputStream getInputStream(String resourcePath) {
    var stream = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
    if(stream == null) {
      throw new RuntimeException(format("Could not find resource [%s]", resourcePath));
    }
    return stream;
  }
}
