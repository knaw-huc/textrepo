package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class DeleteFilesForDocument implements InTransactionRunner {

  private static final Logger log = LoggerFactory.getLogger(DeleteFilesForDocument.class);

  private final Document doc;

  public DeleteFilesForDocument(Document doc) {
    this.doc = Objects.requireNonNull(doc);
  }

  @Override
  public void executeIn(Handle transaction) {
    log.debug("Removing all files for document: {}, externalId: [{}]", doc.getId(), doc.getExternalId());
    transaction.attach(DocumentFilesDao.class)
               .findFilesByDocumentId(doc.getId())
               .forEach(f -> new DeleteFile(f).executeIn(transaction));
  }

}
