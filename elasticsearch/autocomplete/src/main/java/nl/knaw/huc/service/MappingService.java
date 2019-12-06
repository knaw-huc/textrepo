package nl.knaw.huc.service;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MappingService {

  public String getMapping() {
    try {
      return IOUtils.resourceToString("/mapping.json", UTF_8);
    } catch (IOException ex) {
      throw new RuntimeException("Could not load mapping file", ex);
    }
  }
}
