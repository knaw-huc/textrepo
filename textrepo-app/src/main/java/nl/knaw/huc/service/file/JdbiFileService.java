package nl.knaw.huc.service.file;

import static java.lang.String.format;

import java.util.UUID;
import java.util.function.Supplier;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.service.index.IndexService;
import org.jdbi.v3.core.Jdbi;

public class JdbiFileService implements FileService {

  private final Jdbi jdbi;
  private final Supplier<UUID> fileIdGenerator;
  private final IndexService indexService;

  public JdbiFileService(
      Jdbi jdbi,
      Supplier<UUID> fileIdGenerator,
      IndexService indexService
  ) {
    this.jdbi = jdbi;
    this.fileIdGenerator = fileIdGenerator;
    this.indexService = indexService;
  }

  @Override
  public TextRepoFile insert(UUID docId, TextRepoFile file) {
    file.setId(fileIdGenerator.get());
    jdbi.useTransaction(transaction -> {
      var documentFilesDao = transaction.attach(DocumentFilesDao.class);
      var filesDao = transaction.attach(FilesDao.class);
      throwBadRequestWhenDocHasFileWithType(docId, file, documentFilesDao);
      filesDao.insert(file.getId(), file.getTypeId());
      documentFilesDao.insert(docId, file.getId());
    });
    indexService.index(file);
    return file;
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
  public TextRepoFile upsert(UUID docId, TextRepoFile file) {
    jdbi.useTransaction(transaction -> {
      var documentFilesDao = transaction.attach(DocumentFilesDao.class);
      var filesDao = transaction.attach(FilesDao.class);
      throwBadRequestWhenDocHasFileWithType(docId, file, documentFilesDao);
      filesDao.upsert(file);
      documentFilesDao.upsert(docId, file.getId());
    });
    indexService.index(file);
    return file;
  }

  /**
   * @throws BadRequestException when document already has file with type
   */
  private void throwBadRequestWhenDocHasFileWithType(UUID docId, TextRepoFile file,
                                                     DocumentFilesDao documentFilesDao) {
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
    indexService.delete(fileId);
  }

  private FilesDao files() {
    return jdbi.onDemand(FilesDao.class);
  }

  private DocumentFilesDao documentsFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }

}
