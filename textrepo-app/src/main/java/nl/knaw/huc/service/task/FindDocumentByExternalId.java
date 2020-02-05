package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.function.Function;
import java.util.function.Supplier;

public class FindDocumentByExternalId implements Function<Handle, Document> {
  private final String externalId;

  public FindDocumentByExternalId(String externalId) {
    this.externalId = externalId;
  }

  @Override
  public Document apply(Handle transaction) {
    final var docs = transaction.attach(DocumentsDao.class);
    return docs.getByExternalId(externalId).orElseThrow(documentNotFound(externalId));
  }

  private Supplier<NotFoundException> documentNotFound(String externalId) {
    return () -> new NotFoundException("No document found for externalId: " + externalId);
  }

}
