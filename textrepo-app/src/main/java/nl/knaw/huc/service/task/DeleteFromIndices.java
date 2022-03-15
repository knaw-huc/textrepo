package nl.knaw.huc.service.task;

import java.util.UUID;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.service.index.IndexService;
import org.jdbi.v3.core.Handle;

public class DeleteFromIndices implements InTransactionRunner {

  private final IndexService indexService;
  private final UUID docId;

  public DeleteFromIndices(
      IndexService indexService,
      UUID docId
  ) {
    this.indexService = indexService;
    this.docId = docId;
  }

  @Override
  public void executeIn(Handle transaction) {
    transaction
        .attach(DocumentFilesDao.class)
        .findFilesByDocumentId(docId)
        .forEach(file -> indexService.delete(file.getId()));
  }
}
