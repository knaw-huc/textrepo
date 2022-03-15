package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.UUID;
import nl.knaw.huc.db.DocumentMetadataDao;
import org.jdbi.v3.core.Handle;

public class GetDocumentMetadata implements InTransactionProvider<Map<String, String>> {
  private final UUID docId;

  public GetDocumentMetadata(UUID docId) {
    this.docId = requireNonNull(docId);
  }

  @Override
  public Map<String, String> executeIn(Handle transaction) {
    var docMetadataDao = transaction.attach(DocumentMetadataDao.class);
    return docMetadataDao.getMetadataByDocumentId(docId);
  }

}
