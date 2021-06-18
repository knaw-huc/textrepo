package nl.knaw.huc.service.document.files;

import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.util.UUID;

import static java.lang.String.format;

public class JdbiDocumentFilesService implements DocumentFilesService {

  private final Jdbi jdbi;

  public JdbiDocumentFilesService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Page<TextRepoFile> getFilesByDocumentId(UUID docId, PageParams pageParams) {
    var total = documentsFiles().countByDocumentId(docId);
    if (total == 0) {
      if (jdbi.onDemand(DocumentsDao.class).get(docId).isEmpty()) {
        throw new NotFoundException(format("No document with ID %s", docId));
      }
    }
    var files = documentsFiles().findFilesByDocumentId(docId, pageParams);
    return new Page<>(files, total, pageParams);
  }

  private DocumentFilesDao documentsFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }
}
