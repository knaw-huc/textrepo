package nl.knaw.huc.service.task;

import nl.knaw.huc.db.DocumentMetadataDao;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class GetDocumentMetadata implements InTransactionProvider<Map<String, String>> {
  private static final Logger log = LoggerFactory.getLogger(GetDocumentMetadata.class);
  private final String externalId;

  public GetDocumentMetadata(String externalId) {
    this.externalId = requireNonNull(externalId);
  }

  @Override
  public Map<String, String> executeIn(Handle transaction) {

    var docDao = transaction.attach(DocumentsDao.class);
    var docMetadataDao = transaction.attach(DocumentMetadataDao.class);

    var docId = docDao
        .getByExternalId(this.externalId)
        .orElseThrow(noDocumentFoundForExternalId())
        .getId();
    return docMetadataDao
        .getByDocumentId(docId);
  }

  private Supplier<NotFoundException> noDocumentFoundForExternalId() {
    return () -> {
      var message = format("No document found with external ID [%s]", externalId);
      log.warn(message);
      return new NotFoundException(message);
    };
  }

}
