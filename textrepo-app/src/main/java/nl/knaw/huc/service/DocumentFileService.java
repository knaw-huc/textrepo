package nl.knaw.huc.service;

import nl.knaw.huc.api.KeyValue;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.util.UUID;

import static java.lang.String.format;

public class DocumentFileService {

  private final FileService fileService;
  private final VersionService versionService;
  private MetadataService metadataService;

  public DocumentFileService(
      FileService fileService,
      VersionService versionService,
      MetadataService metadataService
  ) {
    this.fileService = fileService;
    this.versionService = versionService;
    this.metadataService = metadataService;
  }

  public TextRepoFile getLatestFile(UUID documentId) {

    var version = versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));

    return fileService.getBySha224(version.getFileSha());
  }

  public Version replaceDocumentFile(@Nonnull UUID documentId, @Nonnull TextRepoFile file, String filename) {
    final var currentSha224 = file.getSha224();

    var version = versionService
        .findLatestVersion(documentId)
        .filter(v -> v.getFileSha().equals(currentSha224)) // already the current file for this document
        .orElseGet(() -> versionService.insertNewVersion(documentId, file, filename));

    metadataService.update(new MetadataEntry(version.getDocumentUuid(), "filename", filename));

    return version;
  }

}
