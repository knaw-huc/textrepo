package nl.knaw.huc.service;

import nl.knaw.huc.api.Version;

import java.util.UUID;

public interface DocumentService {
  Version addDocument(byte[] content);

  Version getLatestVersion(UUID documentId);
}
