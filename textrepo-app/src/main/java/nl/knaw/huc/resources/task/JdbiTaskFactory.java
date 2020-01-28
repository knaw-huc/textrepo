package nl.knaw.huc.resources.task;

import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.function.Supplier;

public class JdbiTaskFactory implements TaskBuilderFactory {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;

  public JdbiTaskFactory(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
  }

  @Override
  public TaskBuilder getDocumentImportBuilder() {
    return new JdbiDocumentImportBuilder(jdbi, idGenerator);
  }

}
