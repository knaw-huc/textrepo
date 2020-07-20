package nl.knaw.huc.service.task;

import nl.knaw.huc.db.DocumentMetadataDao;
import org.jdbi.v3.core.Handle;

import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class GetDocumentMetadata implements InTransactionProvider<Map<String, String>> {
  private final UUID docId;

  public GetDocumentMetadata(UUID docId) {
    this.docId = requireNonNull(docId);
  }

  @Override
  public Map<String, String> executeIn(Handle transaction) {
    var docMetadataDao = transaction.attach(DocumentMetadataDao.class);
    return docMetadataDao.getByDocumentId(docId);
  }

}
