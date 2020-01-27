package nl.knaw.huc.resources.task;

import org.jdbi.v3.core.Jdbi;

public class JdbiTaskFactory implements TaskBuilderFactory {
  private final Jdbi jdbi;

  public JdbiTaskFactory(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public TaskBuilder getDocumentImportBuilder() {
    return new JdbiDocumentImportBuilder(jdbi);
  }

}
