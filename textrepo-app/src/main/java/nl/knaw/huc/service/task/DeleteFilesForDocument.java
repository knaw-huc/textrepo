package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DeleteFilesForDocument implements ProvidesInTransaction<String> {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteFilesForDocument.class);

  private final Document doc;

  public DeleteFilesForDocument(Document doc) {
    this.doc = Objects.requireNonNull(doc);
  }

  @Override
  public String executeIn(Handle transaction) {
    final StringBuilder buf = new StringBuilder();
    final var documentFilesDao = transaction.attach(DocumentFilesDao.class);
    final var filesDao = transaction.attach(FilesDao.class);
    final var versionsDao = transaction.attach(VersionsDao.class);
    documentFilesDao.findFilesByDocumentId(doc.getId()).forEach(f -> {
      versionsDao.findByFileId(f.getId()).forEach(v -> {
        var msg = String.format("abandoning contents sha %s%n", v.getContentsSha());
        LOG.debug(msg);
        buf.append(msg);
        msg = String.format("deleting version %s for file %s%n", v.getId(), f.getId());
        LOG.debug(msg);
        buf.append(msg);
        versionsDao.delete(v.getId());
      });
      var msg = String.format("deleting file %s for document %s%n", f.getId(), doc.getId());
      LOG.debug(msg);
      buf.append(msg);
      filesDao.delete(f.getId());
    });
    return buf.toString();
  }
}
