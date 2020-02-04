package nl.knaw.huc.service.task.indexfile;

import nl.knaw.huc.db.TypeDao;
import nl.knaw.huc.service.index.FileIndexer;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindDocumentFileByType;
import nl.knaw.huc.service.task.GetLatestFileContent;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    return new JdbiIndexFileTask(getTypeId());
  }

  private short getTypeId() {
    return types().find(typeName).orElseThrow(typeNotFound(typeName));
  }

  private TypeDao types() {
    return jdbi.onDemand(TypeDao.class);
  }

  private Supplier<NotFoundException> typeNotFound(String name) {
    return () -> new NotFoundException(String.format("No type found with name: %s", name));
  }

  private class JdbiIndexFileTask implements Task {
    private short typeId;

    private JdbiIndexFileTask(short typeId) {
      this.typeId = typeId;
    }

    @Override
    public void run() {
      jdbi.useTransaction(txn -> {
        final var file =
            new FindDocumentByExternalId(txn)
                .andThen(new FindDocumentFileByType(txn, typeName, typeId))
                .apply(externalId);
        final var contents = new GetLatestFileContent(txn).apply(file);
        indexer.indexFile(file, new String(contents.getContent(), UTF_8));
      });
    }
  }
}
