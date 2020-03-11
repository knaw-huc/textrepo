package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class DeleteDocument implements InTransactionProvider<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteDocument.class);

  private final Document doc;

  public DeleteDocument(Document doc) {
    this.doc = requireNonNull(doc);
  }

  @Override
  public Void executeIn(Handle transaction) {
    new DeleteFilesForDocument(doc).executeIn(transaction);

    LOG.debug("Deleting document: {}, externalId: [{}]", doc.getId(), doc.getExternalId());
    transaction.attach(DocumentsDao.class)
               .delete(doc.getId());
    return null;
  }
}
