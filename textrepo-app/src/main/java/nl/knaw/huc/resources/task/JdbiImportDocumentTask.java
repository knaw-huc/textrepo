package nl.knaw.huc.resources.task;

import nl.knaw.huc.core.Version;

import java.util.function.Function;

class JdbiImportDocumentTask implements Task {
  private final Function<String, Version> task;
  private final String externalId;

  public JdbiImportDocumentTask(Function<String, Version> task, String externalId) {
    this.task = task;
    this.externalId = externalId;
  }

  @Override
  public void run() {
    task.apply(externalId);
  }
}
