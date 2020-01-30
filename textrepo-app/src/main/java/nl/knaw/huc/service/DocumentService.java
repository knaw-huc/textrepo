package nl.knaw.huc.service;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;

import javax.ws.rs.BadRequestException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
  Document get(UUID doc);

  Document create(Document document);

  Document update(Document document);

  void delete(Document document);
}
