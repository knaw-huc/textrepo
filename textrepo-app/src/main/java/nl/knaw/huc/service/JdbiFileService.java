package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.FileDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static nl.knaw.huc.core.Contents.fromContent;

public class JdbiFileService implements FileService {
  private static final Logger LOGGER = LoggerFactory.getLogger(JdbiFileService.class);

  private final Jdbi jdbi;
  private final TypeService typeService;
  private final VersionService versionService;
  private final Supplier<UUID> fileIdGenerator;

  private MetadataService metadataService;

  public JdbiFileService(
      Jdbi jdbi,
      TypeService typeService,
      VersionService versionService,
      MetadataService metadataService, Supplier<UUID> fileIdGenerator) {
    this.jdbi = jdbi;
    this.typeService = typeService;
    this.versionService = versionService;
    this.fileIdGenerator = fileIdGenerator;
    this.metadataService = metadataService;
  }

  public UUID createFile(@Nonnull String type) {
    JdbiFileService.LOGGER.trace("creating file of type: {}", type);
    final var fileId = fileIdGenerator.get();
    final var typeId = typeService.get(type);
    files().create(fileId, typeId);
    return fileId;
  }

  public Version createVersionWithFilenameMetadata(UUID fileId, byte[] content, String filename) {
    final var contents = fromContent(content);
    return addFile(contents, fileId, filename);
  }

  public Version addFile(@Nonnull Contents contents, UUID fileId, String filename) {
    var version = versionService.insertNewVersion(fileId, contents, now());
    metadataService.insert(version.getFileId(), new MetadataEntry("filename", filename));
    return version;
  }

  public Version getLatestVersion(@Nonnull UUID fileId) {
    return versionService
        .findLatestVersion(fileId)
        .orElseThrow(() -> new NotFoundException(format("No such file: %s", fileId)));
  }

  private FileDao files() {
    return jdbi.onDemand(FileDao.class);
  }
}
