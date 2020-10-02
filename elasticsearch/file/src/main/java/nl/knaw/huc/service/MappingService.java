package nl.knaw.huc.service;

import nl.knaw.huc.FileConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MappingService {

  private final FileConfiguration config;

  public MappingService(FileConfiguration config) {
    this.config = config;
  }

  public String getMapping() {
    try {
      return Files.readString(Paths.get(config.getMappingFile()), UTF_8);
    } catch (IOException ex) {
      throw new RuntimeException("Could not load mapping file", ex);
    }
  }
}
