package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static nl.knaw.huc.service.PsqlExceptionService.Constraint.VERSIONS_CONTENTS_SHA;
import static nl.knaw.huc.service.PsqlExceptionService.violatesConstraint;

public class DeleteFilesForDocument implements ProvidesInTransaction<String> {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteFilesForDocument.class);

  private final Document doc;

  public DeleteFilesForDocument(Document doc) {
    this.doc = Objects.requireNonNull(doc);
  }

  @Override
  public String executeIn(Handle transaction) {
    final var buf = new StringBuilder();
    final var documentFilesDao = transaction.attach(DocumentFilesDao.class);
    final var filesDao = transaction.attach(FilesDao.class);
    final var versionsDao = transaction.attach(VersionsDao.class);
    final var contentsDao = transaction.attach(ContentsDao.class);
    documentFilesDao.findFilesByDocumentId(doc.getId()).forEach(f -> {
      versionsDao.findByFileId(f.getId()).forEach(v -> {
        var msg = String.format("deleting version %s for file %s%n", v.getId(), f.getId());
        LOG.debug(msg);
        buf.append(msg);
        versionsDao.delete(v.getId());
        try {
          contentsDao.delete(v.getContentsSha());
        } catch (JdbiException e) {
          if (violatesConstraint(e, VERSIONS_CONTENTS_SHA)) {
            LOG.info("Not deleting contents of version {} because sha {} is still in use", v.getId(),
                v.getContentsSha());
          } else {
            throw (e);
          }
        }
      });
      var msg = String.format("deleting file %s for document %s%n", f.getId(), doc.getId());
      LOG.debug(msg);
      buf.append(msg);
      filesDao.delete(f.getId());
    });
    return buf.toString();
  }
}
