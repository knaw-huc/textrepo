package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.DocumentsDao;
import nl.knaw.huc.db.MetadataDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.postgresql.util.PSQLException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;

public class JdbiDocumentService implements DocumentService {
  private final Jdbi jdbi;
  private Supplier<UUID> uuidGenerator;

  public JdbiDocumentService(Jdbi jdbi, Supplier<UUID> uuidGenerator) {
    this.jdbi = jdbi;
    this.uuidGenerator = uuidGenerator;
  }

  @Override
  public Document get(UUID id) {
    return documents().get(id);
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
  public void delete(Document document) {
    documents().delete(document.getId());
  }

  private DocumentsDao documents() {
    return jdbi.onDemand(DocumentsDao.class);
  }
}
