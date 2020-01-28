package nl.knaw.huc.resources.task.jdbi;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

class HaveDocumentByExternalId implements Function<String, Document> {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;

  HaveDocumentByExternalId(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
  }

  @Override
  public Document apply(String externalId) {
    return findDocument(externalId).orElseGet(createNewDocument(externalId));
  }

  private Optional<Document> findDocument(String externalId) {
    return docs().getByExternalId(externalId);
  }

  private Supplier<Document> createNewDocument(String externalId) {
    return () -> {
      final var document = new Document(idGenerator.get(), externalId);
      docs().insert(document);
      return document;
    };
  }

  private DocumentsDao docs() {
    return jdbi.onDemand(DocumentsDao.class);
  }
}
