package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoContents;
import nl.knaw.huc.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static nl.knaw.huc.api.TextRepoContents.fromContent;

public class DocumentService {
  private final VersionService versionService;
  private final Supplier<UUID> documentIdGenerator;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private MetadataService metadataService;

  public DocumentService(
      VersionService versionService,
      Supplier<UUID> documentIdGenerator,
      MetadataService metadataService) {
    this.versionService = versionService;
    this.documentIdGenerator = documentIdGenerator;
    this.metadataService = metadataService;
  }

  public Version createVersionWithFilenameMetadata(
      byte[] content,
      String filename
  ) {
    final var contents = fromContent(content);
    return addDocument(contents, filename);
  }

  private Version addDocument(@Nonnull TextRepoContents contents, String filename) {
    var version = versionService.insertNewVersion(documentIdGenerator.get(), contents, filename, now());
    metadataService.insert(version.getDocumentUuid(), new MetadataEntry("filename", filename));
    return version;
  }


  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));
  }

}
