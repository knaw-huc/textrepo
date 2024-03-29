package nl.knaw.huc.service.document;

import static java.lang.String.format;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.Constraint.DOCUMENTS_EXTERNAL_ID_KEY;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.violatesConstraint;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.ws.rs.BadRequestException;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbiDocumentService implements DocumentService {
  private static final Logger log = LoggerFactory.getLogger(JdbiDocumentService.class);

  private final Jdbi jdbi;
  private final Supplier<UUID> uuidGenerator;

  public JdbiDocumentService(Jdbi jdbi, Supplier<UUID> uuidGenerator) {
    this.jdbi = jdbi;
    this.uuidGenerator = uuidGenerator;
  }

  @Override
  public Optional<Document> get(UUID docId) {
    return documents().get(docId);
  }

  @Override
  public Document create(Document document) {
    document.setId(uuidGenerator.get());
    try {
      return documents().insert(document);
    } catch (JdbiException ex) {
      if (violatesConstraint(ex, DOCUMENTS_EXTERNAL_ID_KEY)) {
        var msg = format("Document not created: external ID [%s] already exists",
            document.getExternalId());
        log.warn(msg);
        throw new BadRequestException(msg);
      } else {
        throw ex;
      }
    }

  }

  @Override
  public Document update(Document document) {
    return documents().upsert(document);
  }

  @Override
  public void delete(UUID docId) {
    documents().delete(docId);
  }

  @Override
  public long count() {
    return documents().count();
  }

  /**
   * Get all documents filtered by externalId.
   */
  @Override
  public Page<Document> getAll(String externalId, LocalDateTime createdAfter,
                               PageParams pageParams) {
    externalId = isBlank(externalId) ? null : externalId;
    var docs = documents().findBy(externalId, createdAfter, pageParams);
    var total = documents().countBy(externalId, createdAfter);
    return new Page<>(docs, total, pageParams);
  }

  private DocumentsDao documents() {
    return jdbi.onDemand(DocumentsDao.class);
  }
}
