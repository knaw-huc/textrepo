package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteVersion implements InTransactionRunner {
  private static final Logger log = LoggerFactory.getLogger(DeleteVersion.class);

  private final Version version;

  public DeleteVersion(Version version) {
    this.version = version;
  }

  @Override
  public void executeIn(Handle transaction) {
    log.debug("Deleting version: {}", version);
    transaction.attach(VersionsDao.class).delete(version.getId());
    new DeleteContents(version.getContentsSha()).executeIn(transaction);
  }
}
