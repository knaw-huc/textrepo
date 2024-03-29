package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDocument implements InTransactionRunner {

  private static final Logger log = LoggerFactory.getLogger(DeleteDocument.class);

  private final Document doc;

  public DeleteDocument(Document doc) {
    this.doc = requireNonNull(doc);
  }

  @Override
  public void executeIn(Handle transaction) {
    new DeleteFilesForDocument(doc).executeIn(transaction);

    log.debug("Deleting document: {}, externalId: [{}]", doc.getId(), doc.getExternalId());
    transaction.attach(DocumentsDao.class)
               .delete(doc.getId());
  }
}
