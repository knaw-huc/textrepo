package nl.knaw.huc.service;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class JdbiDocumentService implements DocumentService {
  private final Jdbi jdbi;
  private Supplier<UUID> uuidGenerator;

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
    return documents().insert(document);
  }

  @Override
  public Document update(Document document) {
    return documents().upsert(document);
  }

  @Override
  public void delete(UUID docId) {
    documents().delete(docId);
  }

  /**
   * get all documents filtered by externalId
   */
  @Override
  public Page<Document> getAll(String externalId, PageParams pageParams) {
    externalId = isBlank(externalId) ? null : externalId;
    var docs = documents().getByExternalIdLike(externalId, pageParams);
    var total = documents().countByExternalIdLike(externalId);
    return new Page<>(docs, total, pageParams);
  }

  private DocumentsDao documents() {
    return jdbi.onDemand(DocumentsDao.class);
  }
}
