package nl.knaw.huc.service.task.deleter;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.service.task.Task;

public interface DeleteDocumentTaskBuilder {

  DeleteDocumentTaskBuilder forExternalId(String externalId);

  DeleteDocumentTaskBuilder withIndexing(boolean indexing);

  Task<Document> build();
}
