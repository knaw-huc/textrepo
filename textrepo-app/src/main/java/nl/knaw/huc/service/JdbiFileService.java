package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FilesDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static nl.knaw.huc.core.Contents.fromBytes;

public class JdbiFileService implements FileService {

  private static final Logger log = LoggerFactory.getLogger(JdbiFileService.class);

  private final Jdbi jdbi;
  private final TypeService typeService;
  private final VersionService versionService;
  private final Supplier<UUID> fileIdGenerator;

  private FileMetadataService fileMetadataService;

  public JdbiFileService(
      Jdbi jdbi,
      TypeService typeService,
      VersionService versionService,
      FileMetadataService fileMetadataService, Supplier<UUID> fileIdGenerator) {
    this.jdbi = jdbi;
    this.typeService = typeService;
    this.versionService = versionService;
    this.fileIdGenerator = fileIdGenerator;
    this.fileMetadataService = fileMetadataService;
  }

  public TextRepoFile createFile(
      @Nonnull String type,
      @Nonnull String filename
  ) {
    log.trace("Creating file of type: {}", type);
    final var fileId = fileIdGenerator.get();
    final var typeId = typeService.getId(type);
    files().insert(fileId, typeId);
    fileMetadataService.insert(fileId, new MetadataEntry("filename", filename));
    return new TextRepoFile(fileId, typeId);
  }

  public Version createVersion(TextRepoFile file, byte[] bytes) {
    final var contents = fromBytes(bytes);
    return addFile(contents, file);
  }

  public Version addFile(@Nonnull Contents contents, TextRepoFile file) {
    return versionService.createNewVersion(file.getId(), contents);
  }

  public Version getLatestVersion(@Nonnull UUID fileId) {
    return versionService
        .findLatestVersion(fileId)
        .orElseThrow(() -> new NotFoundException(format("No such file: %s", fileId)));
  }

  @Override
  public TextRepoFile insert(UUID docId, TextRepoFile textRepoFile) {
    textRepoFile.setId(fileIdGenerator.get());
    jdbi.useTransaction(transaction -> {
      var documentFilesDao = transaction.attach(DocumentFilesDao.class);
      var filesDao = transaction.attach(FilesDao.class);
      checkFileByDocAndType(docId, textRepoFile, documentFilesDao);
      filesDao.insert(textRepoFile.getId(), textRepoFile.getTypeId());
      documentFilesDao.insert(docId, textRepoFile.getId());
    });
    return textRepoFile;
  }

  @Override
  public TextRepoFile get(UUID fileId) {
    return files()
        .find(fileId)
        .orElseThrow(() -> new NotFoundException(format("No such file: %s", fileId)));
  }

  @Override
  public UUID getDocumentId(UUID fileId) {
    return documentsFiles()
        .findDocumentId(fileId)
        .orElseThrow(() -> new NotFoundException(format("File %s has no document", fileId)));
  }

  @Override
  public TextRepoFile upsert(UUID docId, TextRepoFile textRepoFile) {
    jdbi.useTransaction(transaction -> {
      var documentFilesDao = transaction.attach(DocumentFilesDao.class);
      var filesDao = transaction.attach(FilesDao.class);
      checkFileByDocAndType(docId, textRepoFile, documentFilesDao);
      filesDao.upsert(textRepoFile);
      documentFilesDao.upsert(docId, textRepoFile.getId());
    });
    return textRepoFile;
  }

  /**
   * @throws BadRequestException when another file exists with same type and docId:
   */
  private void checkFileByDocAndType(UUID docId, TextRepoFile file, DocumentFilesDao documentFilesDao) {
    var found = documentFilesDao.findFile(docId, file.getTypeId());

    if (found.isPresent() && !found.get().getId().equals(file.getId())) {
      throw new BadRequestException(format(
          "File with type [%s] and doc id [%s] already exists",
          file.getTypeId(), docId
      ));
    }
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
