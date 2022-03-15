package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import java.util.function.Supplier;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Handle;

public class RegisterIdentifier implements InTransactionProvider<Document> {
  private final String externalId;
  private final Supplier<UUID> idGenerator;

  public RegisterIdentifier(String externalId, Supplier<UUID> idGenerator) {
    this.externalId = requireNonNull(externalId);
    this.idGenerator = idGenerator;
  }

  @Override
  public Document executeIn(Handle transaction) {
    return transaction
        .attach(DocumentsDao.class)
        .register(new Document(idGenerator.get(), externalId));
  }
}
