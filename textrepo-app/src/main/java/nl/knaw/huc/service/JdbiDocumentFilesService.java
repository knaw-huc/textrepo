package nl.knaw.huc.service;

import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
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

  @Override
  public Page<TextrepoFile> getFilesByDocumentId(UUID docId, PageParams pageParams) {
    var content = documentsFiles().findFilesByDocumentId(docId, pageParams);
    var total = documentsFiles().countByDocumentId(docId);
    return new Page<>(content, total, pageParams);
  }

  private DocumentFilesDao documentsFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }
}
