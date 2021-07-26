package nl.knaw.huc.service.file;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.service.version.VersionService;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;

public class JdbiFileService implements FileService {

  private final Jdbi jdbi;
  private final Supplier<UUID> fileIdGenerator;

  public JdbiFileService(
      Jdbi jdbi,
      Supplier<UUID> fileIdGenerator
  ) {
    this.jdbi = jdbi;
    this.fileIdGenerator = fileIdGenerator;
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
