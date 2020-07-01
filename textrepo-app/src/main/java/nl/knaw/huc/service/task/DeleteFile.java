package nl.knaw.huc.service.task;

import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFile implements InTransactionRunner {

  private static final Logger log = LoggerFactory.getLogger(DeleteFile.class);

  private final TextRepoFile file;

  public DeleteFile(TextRepoFile file) {
    this.file = file;
  }

  @Override
  public void executeIn(Handle transaction) {
    final var fileId = file.getId();
    log.debug("Removing all versions of file {}", fileId);
    transaction.attach(VersionsDao.class)
               .findByFileId(fileId)
               .forEach(v -> new DeleteVersion(v).executeIn(transaction));

    log.debug("Removed all versions of file");
    transaction.attach(FilesDao.class).delete(fileId);
  }
}
