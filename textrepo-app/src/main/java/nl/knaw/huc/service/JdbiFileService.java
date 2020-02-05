package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FilesDao;
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

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

  public TextrepoFile createFile(
      @Nonnull String type,
      @Nonnull String filename
  ) {
    logger.trace("creating file of type: {}", type);
    final var fileId = fileIdGenerator.get();
    final var typeId = typeService.getId(type);
    files().create(fileId, typeId);
    metadataService.insert(fileId, new MetadataEntry("filename", filename));
    return new TextrepoFile(fileId, typeId);
  }

  public Version createVersion(TextrepoFile file, byte[] content) {
    final var contents = fromContent(content);
    return addFile(contents, file);
  }

  public Version addFile(@Nonnull Contents contents, TextrepoFile file) {
    return versionService.insertNewVersion(file, contents, now());
  }

  public Version getLatestVersion(@Nonnull UUID fileId) {
    return versionService
        .findLatestVersion(fileId)
        .orElseThrow(() -> new NotFoundException(format("No such file: %s", fileId)));
  }

  /**
   * TODO: check that file type is unique for document
   */
  @Override
  public TextrepoFile create(UUID docId, TextrepoFile file) {
    file.setId(fileIdGenerator.get());
    files().create(file.getId(), file.getTypeId());
    documentsFiles().insert(docId, file.getId());
    return file;
  }

  @Override
  public TextrepoFile get(UUID fileId) {
    return files().find(fileId);
  }

  @Override
  public UUID getDocumentId(UUID fileId) {
    return documentsFiles()
        .findDocumentId(fileId)
        .orElseThrow(() -> new NotFoundException(format("File %s has no document", fileId)));
  }

  /**
   * TODO: check that file type is unique for document
   */
  @Override
  public TextrepoFile upsert(UUID docId, TextrepoFile file) {
    files().upsert(file);
    documentsFiles().upsert(docId, file.getId());
    return file;
  }

  @Override
  public void delete(UUID fileId) {
    files().delete(fileId);
  }

  private FilesDao files() {
    return jdbi.onDemand(FilesDao.class);
  }

  private DocumentFilesDao documentsFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }

}
