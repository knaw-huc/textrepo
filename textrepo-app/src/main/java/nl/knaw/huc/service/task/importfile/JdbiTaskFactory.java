package nl.knaw.huc.service.task.importfile;

import nl.knaw.huc.service.index.FileIndexer;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import nl.knaw.huc.service.task.indexfile.IndexFileTaskBuilder;
import nl.knaw.huc.service.task.indexfile.JdbiIndexFileTaskBuilder;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.function.Supplier;

public class JdbiTaskFactory implements TaskBuilderFactory {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;
  private final FileIndexer fileIndexer;

  public JdbiTaskFactory(Jdbi jdbi, Supplier<UUID> idGenerator, FileIndexer fileIndexer) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
    this.fileIndexer = fileIndexer;
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
