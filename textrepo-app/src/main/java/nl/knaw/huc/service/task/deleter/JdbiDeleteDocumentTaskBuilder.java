package nl.knaw.huc.service.task.deleter;

import nl.knaw.huc.db.DocumentsDao;
import nl.knaw.huc.service.task.DeleteFilesForDocument;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class JdbiDeleteDocumentTaskBuilder implements DeleteDocumentTaskBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(JdbiDeleteDocumentTaskBuilder.class);
  private final Jdbi jdbi;
  private String externalId;

  public JdbiDeleteDocumentTaskBuilder(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public DeleteDocumentTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public Task<String> build() {
    return new DeleteDocumentTask(externalId);
  }

  private class DeleteDocumentTask implements Task<String> {
    private final String externalId;

    private DeleteDocumentTask(String externalId) {
      this.externalId = externalId;
    }

    @Override
    public String run() {
      return jdbi.inTransaction(transaction -> {
        final var doc = new FindDocumentByExternalId(externalId).executeIn(transaction);
        final var msg = new DeleteFilesForDocument(doc).executeIn(transaction);
        LOG.debug("deleting document {}", doc.getId());
        transaction.attach(DocumentsDao.class).delete(doc.getId());
        return msg;
      });
    }
  }
}
