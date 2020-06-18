package nl.knaw.huc.service.task.getter;

import nl.knaw.huc.core.DocumentMetadata;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.GetDocumentMetadata;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

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
  public Task<DocumentMetadata> build() {
    return new GetDocumentMetadataTask();
  }

  private class GetDocumentMetadataTask implements Task<DocumentMetadata> {

    @Override
    public DocumentMetadata run() {
      return jdbi.inTransaction(transaction -> {
        final var doc = new FindDocumentByExternalId(externalId).executeIn(transaction);
        var metadata = new GetDocumentMetadata(doc.getId()).executeIn(transaction);
        var result = new DocumentMetadata();
        result.setDocument(doc);
        result.setMetadata(metadata);
        return result;
      });
    }
  }

}
