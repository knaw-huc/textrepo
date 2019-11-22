package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static nl.knaw.huc.core.Contents.fromContent;

public class FileService {
  private final VersionService versionService;
  private final Supplier<UUID> fileIdGenerator;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private MetadataService metadataService;

  public FileService(
      VersionService versionService,
      Supplier<UUID> fileIdGenerator,
      MetadataService metadataService) {
    this.versionService = versionService;
    this.fileIdGenerator = fileIdGenerator;
    this.metadataService = metadataService;
  }

  public Version createVersionWithFilenameMetadata(
      byte[] content,
      String filename
  ) {
    final var contents = fromContent(content);
    return addFile(contents, filename);
  }

  private Version addFile(@Nonnull Contents contents, String filename) {
    var version = versionService.insertNewVersion(fileIdGenerator.get(), contents, filename, now());
    metadataService.insert(version.getFileUuid(), new MetadataEntry("filename", filename));
    return version;
  }


  public Version getLatestVersion(@Nonnull UUID fileId) {
    return versionService
        .findLatestVersion(fileId)
        .orElseThrow(() -> new NotFoundException(format("No such file: %s", fileId)));
  }

}
