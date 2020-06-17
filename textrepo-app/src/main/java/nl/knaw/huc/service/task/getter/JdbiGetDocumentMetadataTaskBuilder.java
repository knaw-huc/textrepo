package nl.knaw.huc.service.task.getter;

import nl.knaw.huc.service.task.GetDocumentMetadata;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;

public class JdbiGetDocumentMetadataTaskBuilder implements GetDocumentMetadataTaskBuilder {

  private final Jdbi jdbi;
  private String externalId;

  public JdbiGetDocumentMetadataTaskBuilder(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public GetDocumentMetadataTaskBuilder forExternalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  @Override
  public Task<Map<String, String>> build() {
    return new GetDocumentMetadataTask();
  }

  private class GetDocumentMetadataTask implements Task<Map<String, String>> {

    @Override
    public Map<String, String> run() {
      return jdbi.inTransaction(transaction -> new GetDocumentMetadata(externalId).executeIn(transaction));
    }
  }

}
