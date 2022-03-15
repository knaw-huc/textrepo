package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;

public class HaveDocumentByExternalId implements InTransactionProvider<Document> {
  private final Supplier<UUID> idGenerator;
  private final String externalId;

  private Handle transaction;

  public HaveDocumentByExternalId(Supplier<UUID> idGenerator, String externalId) {
    this.idGenerator = requireNonNull(idGenerator);
    this.externalId = requireNonNull(externalId);
  }

  @Override
  public Document executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    return findDocument().orElseGet(createNewDocument());
  }

  private Optional<Document> findDocument() {
    return docs().getByExternalId(externalId);
  }

  private Supplier<Document> createNewDocument() {
    return () -> {
      final var document = new Document(idGenerator.get(), externalId);
      docs().insert(document);
      return document;
    };
  }

  private DocumentsDao docs() {
    return transaction.attach(DocumentsDao.class);
  }
}
