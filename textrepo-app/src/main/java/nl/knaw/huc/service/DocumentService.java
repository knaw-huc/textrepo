package nl.knaw.huc.service;

import nl.knaw.huc.api.KeyValue;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static nl.knaw.huc.api.TextRepoFile.fromContent;

public class DocumentService {
  private final VersionService versionService;
  private final Supplier<UUID> documentIdGenerator;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public DocumentService(
      VersionService versionService,
      Supplier<UUID> documentIdGenerator
  ) {
    this.versionService = versionService;
    this.documentIdGenerator = documentIdGenerator;
  }

  public Version createVersionWithMetadata(
      byte[] content,
      String filepath
  ) {
    final var file = fromContent(content);
    var metadata = new ArrayList<KeyValue>();
    metadata.add(new KeyValue("filename", new File(filepath).getName()));
    return addDocument(file, metadata);
  }

  public Version addDocument(@Nonnull TextRepoFile file, @Nonnull List<KeyValue> metadata) {
    return versionService.insertNewVersion(documentIdGenerator.get(), file, metadata);
  }


  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));
  }

}
