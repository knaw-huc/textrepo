package nl.knaw.huc.service.task.deleter;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.service.index.IndexService;
import nl.knaw.huc.service.task.DeleteDocument;
import nl.knaw.huc.service.task.DeleteFromIndices;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import static java.util.Objects.requireNonNull;

public class JdbiDeleteDocumentTaskBuilder implements DeleteDocumentTaskBuilder {

  private final Jdbi jdbi;
  private final IndexService indexService;

  private String externalId;
  private boolean indexing;

  public JdbiDeleteDocumentTaskBuilder(Jdbi jdbi, IndexService indexService) {
    this.jdbi = jdbi;
    this.indexService = indexService;
  }

  @Override
  public DeleteDocumentTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public DeleteDocumentTaskBuilder withIndexing(boolean indexing) {
    this.indexing = indexing;
    return this;
  }

  @Override
  public Task<Document> build() {
    return new DeleteDocumentTask(externalId, indexing, indexService);
  }

  private class DeleteDocumentTask implements Task<Document> {
    private final String externalId;
    private final boolean indexing;
    private final IndexService indexService;

    private DeleteDocumentTask(String externalId, boolean indexing, IndexService indexService) {
      this.externalId = externalId;
      this.indexing = indexing;
      this.indexService = indexService;
    }

    @Override
    public Document run() {
      return jdbi.inTransaction(transaction -> {
        final var doc = new FindDocumentByExternalId(externalId).executeIn(transaction);

        if (indexing) {
          new DeleteFromIndices(indexService, doc.getId()).executeIn(transaction);
        }

        new DeleteDocument(doc).executeIn(transaction);
        return doc;
      });
    }
  }
}
