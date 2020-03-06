package nl.knaw.huc.service.task;

import nl.knaw.huc.db.ContentsDao;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.knaw.huc.service.PsqlExceptionService.Constraint.VERSIONS_CONTENTS_SHA;
import static nl.knaw.huc.service.PsqlExceptionService.violatesConstraint;

public class DeleteContents implements ProvidesInTransaction<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteContents.class);

  private final String contentsSha;

  public DeleteContents(String contentsSha) {
    this.contentsSha = contentsSha;
  }

  @Override
  public Void executeIn(Handle transaction) {
    try {
      transaction.attach(ContentsDao.class).delete(contentsSha);
    } catch (JdbiException ex) {
      if (violatesConstraint(ex, VERSIONS_CONTENTS_SHA)) {
        LOG.debug("Not deleting contents because {} is still in use", contentsSha);
      } else {
        throw (ex);
      }
    }
    return null;
  }
}
