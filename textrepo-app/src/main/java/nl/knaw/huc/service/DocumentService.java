package nl.knaw.huc.service;

import nl.knaw.huc.api.KeyValue;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static nl.knaw.huc.api.TextRepoFile.fromContent;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

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

  public Version addDocument(@Nonnull TextRepoFile file, @Nonnull List<KeyValue> metadata) {
    return versionService.insertNewVersion(documentIdGenerator.get(), file, metadata);
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


  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));
  }

  public List<Version> addZippedDocuments(InputStream uploadedInputStream) {
    var versions = new ArrayList<Version>();

    var zipInputStream = new ZipInputStream(uploadedInputStream);
    var buffer = new byte[2048];

    ZipEntry entry;
    try {
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (skipEntry(entry)) {
          continue;
        }
        logger.info("add zipped file [{}]", entry.getName());
        versions.add(handleEntry(zipInputStream, buffer, entry.getName()));
      }
    } catch (IllegalArgumentException | IOException ex) {
      throw new BadRequestException("Zip could not be processed", ex);
    }

    return versions;
  }

  private boolean skipEntry(ZipEntry entry) {
    if (isHiddenFile(entry)) {
      logger.info("skip hidden file [{}]", entry.getName());
      return true;
    }
    if (entry.isDirectory()) {
      logger.info("skip directory [{}]", entry.getName());
      return true;
    }
    return false;
  }

  private Version handleEntry(
      ZipInputStream zis,
      byte[] buffer,
      String filename
  ) throws IOException {
    byte[] content;
    try (var output = new ByteArrayOutputStream()) {
      int len;
      while ((len = zis.read(buffer)) > 0) {
        output.write(buffer, 0, len);
      }
      content = output.toByteArray();
    }
    return createVersionWithMetadata(content, filename);
  }

  private boolean isHiddenFile(ZipEntry entry) {
    var filename = new File(entry.getName()).getName();
    return filename.startsWith(".");
  }

}
