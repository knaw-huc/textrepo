package nl.knaw.huc.service;

import nl.knaw.huc.db.DocumentFilesDao;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.function.Supplier;

public class JdbiDocumentService {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;

  public JdbiDocumentService(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
  }

  public UUID createDocument(UUID fileId) {
    final var docId = idGenerator.get();
    documentFiles().insert(docId, fileId);
    return docId;
  }

  private DocumentFilesDao documentFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }
}
