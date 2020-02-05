package nl.knaw.huc.service;

import nl.knaw.huc.core.Document;

import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
  Optional<Document> get(UUID docId);

  Document create(Document document);

  Document update(Document document);

  void delete(UUID docId);
}
