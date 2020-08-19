package nl.knaw.huc.service.document;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
  Optional<Document> get(UUID docId);

  Document create(Document document);

  Document update(Document document);

  void delete(UUID docId);

  long count();

  Page<Document> getAll(String externalId, LocalDateTime createdAfter, PageParams pageParams);
}
