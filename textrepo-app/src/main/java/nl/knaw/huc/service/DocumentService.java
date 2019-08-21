package nl.knaw.huc.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.beans.ConstructorProperties;
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

public class DocumentService {
  private final VersionService versionService;
  private final Supplier<UUID> documentIdGenerator;

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

  public List<Version> uploadBatch(InputStream uploadedInputStream) {
    var versions = new ArrayList<Version>();
    var zipInputStream = new ZipInputStream(uploadedInputStream);
    var buffer = new byte[2048];

    ZipEntry entry;
    try {
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (isHiddenFile(entry)) {
          continue;
        }
        TextRepoFile file;
        try (var output = new ByteArrayOutputStream((int) entry.getSize())) {
          int len;
          while ((len = zipInputStream.read(buffer)) > 0) {
            output.write(buffer, 0, len);
          }
          var content = output.toByteArray();
          file = fromContent(content);
        }
        versions.add(addDocument(file));
      }
    } catch (IOException ex) {
      throw new BadRequestException("Zip file could not be read");
    }
    return versions;
  }

  private boolean isHiddenFile(ZipEntry entry) {
    var filename = new File(entry.getName()).getName();
    return filename.startsWith(".");
  }

  public static class KeyValue {
    @JsonProperty
    public final String key;
    @JsonProperty
    public final String value;

    @ConstructorProperties({"key", "value"})
    public KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
