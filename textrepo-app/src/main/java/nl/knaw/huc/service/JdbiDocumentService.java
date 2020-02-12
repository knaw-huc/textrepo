package nl.knaw.huc.service;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

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
    documents().insert(document);
    return document;
  }

  @Override
  public Document update(Document document) {
    documents().upsert(document);
    return document;
  }

  @Override
  public void delete(UUID docId) {
    documents().delete(docId);
  }

  /**
   * get all documents filtered by externalId
   * At the moment externalId
   */
  @Override
  public List<Document> getAll(String externalId) {
    return documents().getByExternalIdLike(externalId);
  }

  private DocumentsDao documents() {
    return jdbi.onDemand(DocumentsDao.class);
  }
}
