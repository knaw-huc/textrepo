package nl.knaw.huc.service.task.indexfile;

import nl.knaw.huc.service.index.FileIndexer;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindDocumentFileByType;
import nl.knaw.huc.service.task.GetLatestFileContent;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

public class JdbiIndexFileTaskBuilder implements IndexFileTaskBuilder {
  private final Jdbi jdbi;
  private final FileIndexer indexer;

  private String externalId;
  private String typeName;

  public JdbiIndexFileTaskBuilder(Jdbi jdbi, FileIndexer indexer) {
    this.jdbi = jdbi;
    this.indexer = indexer;
  }

  @Override
  public IndexFileTaskBuilder forExternalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  @Override
  public IndexFileTaskBuilder withType(String typeName) {
    this.typeName = typeName;
    return this;
  }

  @Override
  public Task build() {
    return new JdbiIndexFileTask(externalId, typeName);
  }

  private class JdbiIndexFileTask implements Task {
    private final String externalId;
    private final String typeName;

    private JdbiIndexFileTask(String externalId, String typeName) {
      this.externalId = externalId;
      this.typeName = typeName;
    }

    @Override
    public void run() {
      jdbi.useTransaction(txn -> {
        final var doc = new FindDocumentByExternalId(externalId).apply(txn);
        final var file = new FindDocumentFileByType(doc, typeName).apply(txn);
        final var contents = new GetLatestFileContent(file).apply(txn);
        indexer.indexFile(file, contents.asUTF8String());
      });
    }
  }
}
