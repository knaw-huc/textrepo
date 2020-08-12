package nl.knaw.huc.service.task;

import nl.knaw.huc.service.index.Indexer;
import nl.knaw.huc.service.task.deleter.DeleteDocumentTaskBuilder;
import nl.knaw.huc.service.task.deleter.JdbiDeleteDocumentTaskBuilder;
import nl.knaw.huc.service.task.finder.FindContentsTaskBuilder;
import nl.knaw.huc.service.task.finder.JdbiFindContentsTaskBuilder;
import nl.knaw.huc.service.task.getter.GetDocumentMetadataTaskBuilder;
import nl.knaw.huc.service.task.getter.GetFileMetadataTaskBuilder;
import nl.knaw.huc.service.task.getter.JdbiGetDocumentMetadataTaskBuilder;
import nl.knaw.huc.service.task.getter.JdbiGetFileMetadataTaskBuilder;
import nl.knaw.huc.service.task.importer.ImportFileTaskBuilder;
import nl.knaw.huc.service.task.importer.JdbiImportFileTaskBuilder;
import nl.knaw.huc.service.task.indexer.IndexFileTaskBuilder;
import nl.knaw.huc.service.task.indexer.JdbiIndexFileTaskBuilder;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class JdbiTaskFactory implements TaskBuilderFactory {
  private final Jdbi jdbi;
  private Supplier<UUID> idGenerator;
  private List<Indexer> indexers;

  public JdbiTaskFactory(Jdbi jdbi, List<Indexer> indexers) {
    this.jdbi = requireNonNull(jdbi);
    this.indexers = requireNonNull(indexers);
  }

  public JdbiTaskFactory withIdGenerator(Supplier<UUID> idGenerator) {
    this.idGenerator = requireNonNull(idGenerator);
    return this;
  }

  @Override
  public ImportFileTaskBuilder getDocumentImportBuilder() {
    return new JdbiImportFileTaskBuilder(jdbi, idGenerator);
  }

  @Override
  public IndexFileTaskBuilder getDocumentIndexBuilder() {
    return new JdbiIndexFileTaskBuilder(jdbi, indexers);
  }

  @Override
  public FindContentsTaskBuilder getContentsFinderBuilder() {
    return new JdbiFindContentsTaskBuilder(jdbi);
  }

  @Override
  public DeleteDocumentTaskBuilder getDocumentDeleteBuilder() {
    return new JdbiDeleteDocumentTaskBuilder(jdbi);
  }

  @Override
  public GetDocumentMetadataTaskBuilder getDocumentMetadataGetter() {
    return new JdbiGetDocumentMetadataTaskBuilder(jdbi);
  }

  @Override
  public GetFileMetadataTaskBuilder getFileMetadataGetter() {
    return new JdbiGetFileMetadataTaskBuilder(jdbi);
  }
}
