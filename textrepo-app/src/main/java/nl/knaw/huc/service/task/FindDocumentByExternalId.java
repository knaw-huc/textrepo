package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class FindDocumentByExternalId implements ProvidesInTransaction<Document> {
  private final String externalId;

  private Handle transaction;

  public FindDocumentByExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
  }

  @Override
  public Document exececuteIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    return docs().getByExternalId(externalId).orElseThrow(documentNotFound(externalId));
  }

  private Supplier<NotFoundException> documentNotFound(String externalId) {
    return () -> new NotFoundException("No document found for externalId: " + externalId);
  }

  private DocumentsDao docs() {
    return transaction.attach(DocumentsDao.class);
  }
}
