package nl.knaw.huc.service.task.importfile;

import nl.knaw.huc.service.index.FileIndexer;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import nl.knaw.huc.service.task.indexfile.IndexFileTaskBuilder;
import nl.knaw.huc.service.task.indexfile.JdbiIndexFileTaskBuilder;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class JdbiTaskFactory implements TaskBuilderFactory {
  private final Jdbi jdbi;

  private Supplier<UUID> idGenerator;
  private FileIndexer fileIndexer;

  public JdbiTaskFactory(Jdbi jdbi) {
    this.jdbi = requireNonNull(jdbi);
  }

  public JdbiTaskFactory withIDGenerator(Supplier<UUID> idGenerator) {
    this.idGenerator = idGenerator;
    return this;
  }

  public JdbiTaskFactory withFileIndexer(FileIndexer fileIndexer) {
    this.fileIndexer = fileIndexer;
    return this;
  }

  @Override
  public ImportFileTaskBuilder getDocumentImportBuilder() {
    return new JdbiImportFileTaskBuilder(jdbi, idGenerator);
  }

  @Override
  public IndexFileTaskBuilder getDocumentIndexBuilder() {
    return new JdbiIndexFileTaskBuilder(jdbi, fileIndexer);
  }

}
