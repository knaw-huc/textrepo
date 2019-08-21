package nl.knaw.huc.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.beans.ConstructorProperties;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;

public class DocumentService {
  private final VersionService versionService;
  private final Supplier<UUID> documentIdGenerator;

  public DocumentService(
      VersionService versionService,
      Supplier<UUID> documentIdGenerator
  ) {
    this.versionService = versionService;
    this.documentIdGenerator = documentIdGenerator;
  }

  public Version addDocument(@Nonnull TextRepoFile file) {
    return versionService.insertNewVersion(documentIdGenerator.get(), file);
  }

  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));
  }

  public static class KeyValue {
    @JsonProperty
    public final String key;
    @JsonProperty
    public final String value;

    @ConstructorProperties({"key", "value"})
    public KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
