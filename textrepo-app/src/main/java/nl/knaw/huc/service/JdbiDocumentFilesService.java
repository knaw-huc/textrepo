package nl.knaw.huc.service;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;

public class JdbiDocumentFilesService implements DocumentFilesService {

  private final Jdbi jdbi;

  public JdbiDocumentFilesService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  private DocumentFilesDao documentsFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }

  @Override
  public List<TextrepoFile> getFilesByDocumentId(UUID docId) {
    return documentsFiles().findFilesByDocumentId(docId);
  }
}
