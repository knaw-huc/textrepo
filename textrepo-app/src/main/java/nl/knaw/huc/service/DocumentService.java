package nl.knaw.huc.service;

import nl.knaw.huc.api.KeyValue;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static nl.knaw.huc.api.TextRepoFile.fromContent;

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

  public Version createVersionWithMetadata(
      byte[] content,
      String filename
  ) {
    final var file = fromContent(content);
    return addDocument(file, filename);
  }

  private Version addDocument(@Nonnull TextRepoFile file, String filename) {
    var version = versionService.insertNewVersion(documentIdGenerator.get(), file, filename);
    metadataService.insert(new MetadataEntry(version.getDocumentUuid(), "filename", filename));
    return version;
  }


  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));
  }

}
