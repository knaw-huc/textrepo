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
  private final Supplier<UUID> idGenerator;

  public JdbiDocumentService(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
  }

  @Override
  public Document get(UUID id) {
    return documents().get(id);
  }

  @Override
  public UUID createDocumentByExternalId(UUID fileId, String externalId) {
    final var docId = idGenerator.get();
    try {
      documents().insert(new Document(docId, externalId));
    } catch (JdbiException ex) {
      handleDocExists(externalId, ex);
    }
    documentFiles().insert(docId, fileId);
    return docId;
  }

  private void handleDocExists(String externalId, JdbiException ex) {
    if (ex.getCause() instanceof PSQLException) {
      var cause = (PSQLException) ex.getCause();
      if (cause.getSQLState().equals("23505") &&
          cause.getServerErrorMessage().getConstraint().equals("documents_external_id_key")) {
        throw new BadRequestException(format("Document with external id [%s] already exists", externalId));
      }
    }
    throw ex;
  }

  @Override
  public void addFileToDocument(UUID docId, UUID fileId) {
    documentFiles().insert(docId, fileId);
  }

  @Override
  public TextrepoFile findFileForType(UUID docId, String fileType) {
    return documentFiles()
        .findFile(docId, fileType)
        .orElseThrow(() -> new NotFoundException(format(
            "No %s file found for document %s", fileType, docId
        )));
  }

  @Override
  public Map<String, String> getMetadata(UUID docId) {
    return jdbi.onDemand(MetadataDao.class).getMetadataByDocumentId(docId);
  }

  @Override
  public boolean updateMetadata(UUID docId, MetadataEntry entry) {
    return jdbi.onDemand(MetadataDao.class).updateDocumentMetadata(docId, entry);
  }

  @Override
  public Optional<UUID> findDocumentByExternalId(String externalId) {
    return documentFiles().findDocumentByExternalId(externalId);
  }

  private DocumentsDao documents() {
    return jdbi.onDemand(DocumentsDao.class);
  }

  private DocumentFilesDao documentFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }
}
