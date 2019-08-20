package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;

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

}
