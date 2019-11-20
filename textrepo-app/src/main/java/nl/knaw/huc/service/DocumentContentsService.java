package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoContents;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

import java.util.UUID;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

public class DocumentContentsService {

  private final ContentsService contentsService;
  private final VersionService versionService;
  private MetadataService metadataService;

  public DocumentContentsService(
      ContentsService contentsService,
      VersionService versionService,
      MetadataService metadataService
  ) {
    this.contentsService = contentsService;
    this.versionService = versionService;
    this.metadataService = metadataService;
  }

  public TextRepoContents getLatestFile(UUID documentId) {

    var version = versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));

    return contentsService.getBySha224(version.getContentsSha());
  }

  public Version replaceDocumentContents(
      @Nonnull UUID documentId,
      @Nonnull TextRepoContents contents,
      String filename
  ) {
    final var currentSha224 = contents.getSha224();

    var version = versionService
        .findLatestVersion(documentId)
        .filter(v -> v.getContentsSha().equals(currentSha224)) // already the current file for this document
        .orElseGet(() -> versionService.insertNewVersion(documentId, contents, filename, now()));

    metadataService.update(documentId, new MetadataEntry("filename", filename));

    return version;
  }

}
