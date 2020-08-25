package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Document;

import java.util.List;
import java.util.stream.Stream;

public interface RegisterIdentifiersTaskBuilder {
  RegisterIdentifiersTaskBuilder forExternalIdentifiers(Stream<String> ids);

  Task<List<Document>> build();
}
