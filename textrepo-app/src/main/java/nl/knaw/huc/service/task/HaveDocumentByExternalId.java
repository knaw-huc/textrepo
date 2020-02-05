package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class HaveDocumentByExternalId implements Function<Handle, Document> {
  private final Supplier<UUID> idGenerator;
  private final String externalId;

  private Handle transaction;

  public HaveDocumentByExternalId(Supplier<UUID> idGenerator, String externalId) {
    this.idGenerator = requireNonNull(idGenerator);
    this.externalId = requireNonNull(externalId);
  }

  @Override
  public Document apply(Handle transaction) {
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
