package nl.knaw.huc.service.task.deleter;

import nl.knaw.huc.service.task.DeleteDocument;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import static java.util.Objects.requireNonNull;

public class JdbiDeleteDocumentTaskBuilder implements DeleteDocumentTaskBuilder {
  private final Jdbi jdbi;

  private String externalId;

  public JdbiDeleteDocumentTaskBuilder(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public DeleteDocumentTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public Task<String> build() {
    return new DeleteDocumentTask(externalId);
  }

  private class DeleteDocumentTask implements Task<String> {
    private final String externalId;

    private DeleteDocumentTask(String externalId) {
      this.externalId = externalId;
    }

    @Override
    public String run() {
      jdbi.useTransaction(transaction -> {
        final var doc = new FindDocumentByExternalId(externalId).executeIn(transaction);
        new DeleteDocument(doc).executeIn(transaction);
      });
      return "";
    }
  }
}
