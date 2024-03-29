package nl.knaw.huc.service.document.files;

import static java.lang.String.format;

import java.util.UUID;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.DocumentsDao;
import org.jdbi.v3.core.Jdbi;

public class JdbiDocumentFilesService implements DocumentFilesService {

  private final Jdbi jdbi;

  public JdbiDocumentFilesService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Page<TextRepoFile> getFilesByDocumentAndTypeId(UUID docId, Short typeId,
                                                        PageParams pageParams) {
    var total = documentsFiles().countByDocumentAndTypeId(docId, typeId);
    if (total == 0) {
      if (jdbi.onDemand(DocumentsDao.class).get(docId).isEmpty()) {
        throw new NotFoundException(format("No document with id %s and type id %s", docId, typeId));
      }
    }
    var files = documentsFiles().findFilesByDocumentAndTypeId(docId, typeId, pageParams);
    return new Page<>(files, total, pageParams);
  }

  private DocumentFilesDao documentsFiles() {
    return jdbi.onDemand(DocumentFilesDao.class);
  }
}
