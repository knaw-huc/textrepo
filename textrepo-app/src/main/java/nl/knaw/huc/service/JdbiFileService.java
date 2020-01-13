package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
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

  public TextrepoFile createFile(@Nonnull String type) {
    JdbiFileService.LOGGER.trace("creating file of type: {}", type);
    final var fileId = fileIdGenerator.get();
    final var typeId = typeService.getId(type);
    files().create(fileId, typeId);
    return new TextrepoFile(fileId, typeId);
  }

  public Version createVersionWithFilenameMetadata(TextrepoFile file, byte[] content, String filename) {
    final var contents = fromContent(content);
    return addFile(contents, file, filename);
  }

  public Version addFile(@Nonnull Contents contents, TextrepoFile file, String filename) {
    var version = versionService.insertNewVersion(file, contents, now());
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
