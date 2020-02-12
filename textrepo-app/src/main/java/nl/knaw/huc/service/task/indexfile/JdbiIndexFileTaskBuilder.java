package nl.knaw.huc.service.task.indexfile;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.TypesDao;
import nl.knaw.huc.service.index.FileIndexer;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindDocumentFileByType;
import nl.knaw.huc.service.task.GetLatestFileContents;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class JdbiIndexFileTaskBuilder implements IndexFileTaskBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(JdbiIndexFileTaskBuilder.class);

  private final Jdbi jdbi;
  private final FileIndexer indexer;

  private String externalId;
  private String typeName;

  public JdbiIndexFileTaskBuilder(Jdbi jdbi, FileIndexer indexer) {
    this.jdbi = requireNonNull(jdbi);
    this.indexer = requireNonNull(indexer);
  }

  @Override
  public IndexFileTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public IndexFileTaskBuilder withType(String typeName) {
    this.typeName = requireNonNull(typeName);
    return this;
  }

  @Override
  public Task build() {
    if (externalId == null) {
      return new JdbiIndexAllFilesTask(typeName);
    }
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
        final var doc = new FindDocumentByExternalId(externalId).executeIn(txn);
        final var file = new FindDocumentFileByType(doc, typeName).executeIn(txn);
        final var contents = new GetLatestFileContents(file).executeIn(txn);
        final var indexResult = indexer.indexFile(file, contents.asUtf8String());
        indexResult.ifPresent(LOG::warn);
      });
    }
  }

  private class JdbiIndexAllFilesTask implements Task {
    private final Logger logger = LoggerFactory.getLogger(JdbiIndexAllFilesTask.class);

    private final String typeName;

    private int filesAffected = 0;

    private JdbiIndexAllFilesTask(String typeName) {
      this.typeName = typeName;
    }

    @Override
    public void run() {
      indexFilesByType(resolveType());
      logger.info("Total files affected: {}", filesAffected);
    }

    private Short resolveType() {
      return types().find(typeName).orElseThrow(noSuchType(typeName));
    }

    private TypesDao types() {
      return jdbi.onDemand(TypesDao.class);
    }

    private Supplier<NotFoundException> noSuchType(String typeName) {
      return () -> new NotFoundException(String.format("No such type: %s", typeName));
    }

    private void indexFilesByType(Short typeId) {
      jdbi.onDemand(FilesDao.class).foreachByType(typeId, this::indexFile);
    }

    private void indexFile(TextrepoFile file) {
      logger.debug("Indexing file: {}", file.getId());
      jdbi.useTransaction(txn -> {
        final var contents = new GetLatestFileContents(file).executeIn(txn);
        final var indexResult = indexer.indexFile(file, contents.asUtf8String());
        indexResult.ifPresent(LOG::warn);
        filesAffected++;
      });
    }
  }
}
