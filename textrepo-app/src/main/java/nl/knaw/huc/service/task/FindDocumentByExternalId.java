package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FindDocumentByExternalId implements Function<String, Document> {
  private final Handle transaction;

  public FindDocumentByExternalId(Handle transaction) {
    this.transaction = transaction;
  }

  @Override
  public Document apply(String externalId) {
    return findDocument(externalId).orElseThrow(documentNotFound(externalId));
  }

  private Optional<Document> findDocument(String externalId) {
    return docs().getByExternalId(externalId);
  }

  private Supplier<NotFoundException> documentNotFound(String externalId) {
    return () -> new NotFoundException("No document found for externalId: " + externalId);
  }

  private DocumentsDao docs() {
    return transaction.attach(DocumentsDao.class);
  }
}
