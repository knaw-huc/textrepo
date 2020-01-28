package nl.knaw.huc.resources.task.jdbi;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

class GetOrCreateDocument implements Function<String, Document> {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;

  GetOrCreateDocument(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
  }

  @Override
  public Document apply(String externalId) {
    return docs().getByExternalId(externalId)
                 .orElseGet(() -> createDocument(externalId));
  }

  private Document createDocument(String externalId) {
    final var document = new Document(idGenerator.get(), externalId);
    docs().insert(document);
    return document;
  }

  private DocumentsDao docs() {
    return jdbi.onDemand(DocumentsDao.class);
  }
}
