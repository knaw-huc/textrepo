package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteVersion implements ProvidesInTransaction<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteVersion.class);

  private final Version version;

  public DeleteVersion(Version version) {
    this.version = version;
  }

  @Override
  public Void executeIn(Handle transaction) {
    LOG.debug("Deleting version: {}", version);
    transaction.attach(VersionsDao.class).delete(version.getId());
    return new DeleteContents(version.getContentsSha()).executeIn(transaction);
  }
}
