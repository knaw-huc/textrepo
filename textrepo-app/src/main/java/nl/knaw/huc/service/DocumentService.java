package nl.knaw.huc.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class DocumentService {
  private final MetadataService metadataService;
  private final VersionService versionService;
  private final Supplier<UUID> documentIdGenerator;

  public DocumentService(MetadataService metadataService,
                         VersionService versionService,
                         Supplier<UUID> documentIdGenerator) {
    this.metadataService = metadataService;
    this.versionService = versionService;
    this.documentIdGenerator = documentIdGenerator;
  }

  public Version addDocument(@Nonnull TextRepoFile file) {
    return versionService.insertNewVersion(documentIdGenerator.get(), file);
  }

  public void addMetadata(@Nonnull UUID documentId, @Nonnull String key, @Nonnull String value) {
    metadataService.insert(new MetadataEntry(documentId, key, value));
  }

  public void addMetadata(@Nonnull UUID documentId, @Nonnull KeyValue metadata) {
    addMetadata(documentId, metadata.key, metadata.value);
  }

  public void addMetadata(@Nonnull UUID documentId, @Nonnull List<KeyValue> metadata) {
    var entries = metadata.stream().map(kv -> new MetadataEntry(documentId, kv.key, kv.value)).collect(toList());
    metadataService.bulkInsert(entries);
  }

  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));
  }

  public Version replaceDocumentFile(@Nonnull UUID documentId, @Nonnull TextRepoFile file) {
    final var currentSha224 = file.getSha224();
    return versionService
        .findLatestVersion(documentId)
        .filter(v -> v.getFileSha().equals(currentSha224)) // already the current file for this document
        .orElseGet(() -> versionService.insertNewVersion(documentId, file));
  }

  public List<KeyValue> getMetadata(UUID documentId) {
    return metadataService
        .find(documentId)
        .stream()
        .map(entry -> new KeyValue(entry.getKey(), entry.getValue()))
        .collect(toList());
  }

  public static class KeyValue {
    @JsonProperty
    private final String key;
    @JsonProperty
    private final String value;

    @ConstructorProperties({"key", "value"})
    public KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
