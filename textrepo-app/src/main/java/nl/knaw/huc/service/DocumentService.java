package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import java.io.ByteArrayOutputStream;
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

  public Version addDocument(@Nonnull TextRepoFile file) {
    return versionService.insertNewVersion(documentIdGenerator.get(), file);
  }

  public Version getLatestVersion(@Nonnull UUID documentId) {
    return versionService
        .findLatestVersion(documentId)
        .orElseThrow(() -> new NotFoundException(format("No such document: %s", documentId)));
  }

  public List<Version> addZippedDocuments(InputStream uploadedInputStream) {
    var versions = new ArrayList<Version>();

    var zipInputStream = new ZipInputStream(uploadedInputStream);
    var buffer = new ByteArrayOutputStream();

    ZipEntry entry;
    try {
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (skipEntry(entry)) {
          continue;
        }
        logger.info("add zipped file [{}]", entry.getName());
        zipInputStream.transferTo(buffer);
        versions.add(addDocument(fromContent(buffer.toByteArray())));
        buffer.reset();
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

  private boolean isHiddenFile(ZipEntry entry) {
    var name = entry.getName();
    int base = 1 + name.lastIndexOf('/'); // Zip always uses / as the dir separator
    return base < name.length() && name.charAt(base) == '.';
  }

}
