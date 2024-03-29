package nl.knaw.huc.service.task;

import static nl.knaw.huc.helpers.PsqlExceptionHelper.Constraint.VERSIONS_CONTENTS_SHA;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.violatesConstraint;

import nl.knaw.huc.db.ContentsDao;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteContents implements InTransactionRunner {

  private static final Logger log = LoggerFactory.getLogger(DeleteContents.class);

  private final String contentsSha;

  public DeleteContents(String contentsSha) {
    this.contentsSha = contentsSha;
  }

  @Override
  public void executeIn(Handle transaction) {
    final var savepoint = "delete-" + contentsSha;
    transaction.savepoint(savepoint);
    try {
      transaction.attach(ContentsDao.class).delete(contentsSha);
    } catch (JdbiException ex) {
      if (violatesConstraint(ex, VERSIONS_CONTENTS_SHA)) {
        log.debug("Not deleting contents because {} is still in use", contentsSha);
        transaction.rollbackToSavepoint(savepoint);
      } else {
        throw (ex);
      }
    }
  }
}
