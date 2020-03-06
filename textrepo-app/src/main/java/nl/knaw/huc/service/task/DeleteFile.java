package nl.knaw.huc.service.task;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DeleteFile implements ProvidesInTransaction<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteFile.class);

  private final TextrepoFile file;

  private Handle transaction;

  public DeleteFile(TextrepoFile file) {
    this.file = file;
  }

  @Override
  public Void executeIn(Handle transaction) {
    this.transaction = Objects.requireNonNull(transaction);

    final var fileId = file.getId();
    LOG.debug("Removing all versions of file {}", fileId);
    transaction.attach(VersionsDao.class)
               .findByFileId(fileId)
               .forEach(this::deleteVersion);

    LOG.debug("Deleting file: {}", file);
    transaction.attach(FilesDao.class).delete(fileId);
    return null;
  }

  private void deleteVersion(Version version) {
    new DeleteVersion(version).executeIn(transaction);
  }
}
