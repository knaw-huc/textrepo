package nl.knaw.huc.service.task;

import java.util.List;
import java.util.stream.Stream;
import nl.knaw.huc.core.Document;

public interface RegisterIdentifiersTaskBuilder {
  RegisterIdentifiersTaskBuilder forExternalIdentifiers(Stream<String> ids);

  Task<List<Document>> build();
}
