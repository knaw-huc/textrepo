package nl.knaw.huc.service.document.metadata;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.db.DocumentMetadataDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.Constraint.DOCUMENTS_METADATA_DOCUMENT_ID_FKEY;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.Constraint.VERSIONS_CONTENTS_SHA;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.violatesConstraint;

public class JdbiDocumentMetadataService implements DocumentMetadataService {
  private final Jdbi jdbi;

  public JdbiDocumentMetadataService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void create(UUID docId, MetadataEntry metadataEntry) {
    metadata().insert(docId, metadataEntry);
  }

  @Override
  public Map<String, String> getByDocId(UUID docId) {
    return metadata().getByDocumentId(docId);
  }

  @Override
  public boolean upsert(UUID docId, MetadataEntry entry) {
    try {
      return metadata().upsert(docId, entry);
    } catch (JdbiException ex) {
      if (violatesConstraint(ex, DOCUMENTS_METADATA_DOCUMENT_ID_FKEY)) {
        throw new NotFoundException(format("No such document: %s", docId));
      } else {
        throw (ex);
      }
    }
  }

  @Override
  public void delete(UUID docId, String key) {
    metadata().delete(docId, key);
  }

  @Override
  public List<UUID> findByMetadataKey(String key) {
    return metadata().findByMetadataKey(key);
  }

  private DocumentMetadataDao metadata() {
    return jdbi.onDemand(DocumentMetadataDao.class);
  }

}
