package nl.knaw.huc.resources;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtils {
  public static byte[] getResourceFileBits(String resourcePath) throws IOException {
    return IOUtils.toByteArray(TestUtils.class.getClassLoader().getResourceAsStream(resourcePath));
  }

  public static String getResourceFileString(String resourcePath) throws IOException {
    return IOUtils.toString(TestUtils.class.getClassLoader().getResourceAsStream(resourcePath), UTF_8);
  }
}
