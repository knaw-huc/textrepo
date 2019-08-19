package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Supplier;

public class DocumentService {
  private final Supplier<UUID> documentIdGenerator;
  private final VersionService versionService;

  public DocumentService(VersionService versionService, Supplier<UUID> documentIdGenerator) {
    this.versionService = versionService;
    this.documentIdGenerator = documentIdGenerator;
  }

  public Version addDocument(@Nonnull TextRepoFile file) {
    return versionService.insertNewVersion(documentIdGenerator.get(), file);
  }

  public Version replaceDocument(@Nonnull UUID documentId, @Nonnull TextRepoFile file) {
    return versionService.replace(documentId, file);
  }

  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService.getLatestVersion(documentId);
  }

}
