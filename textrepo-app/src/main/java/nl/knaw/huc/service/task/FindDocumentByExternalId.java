package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;

public class FindDocumentByExternalId implements InTransactionProvider<Document> {
  private final String externalId;

  public FindDocumentByExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
  }

  @Override
  public Document executeIn(Handle transaction) {
    return transaction.attach(DocumentsDao.class)
                      .getByExternalId(externalId)
                      .orElseThrow(documentNotFound(externalId));
  }

  private Supplier<NotFoundException> documentNotFound(String externalId) {
    return () -> new NotFoundException("No document found for externalId: " + externalId);
  }

}
