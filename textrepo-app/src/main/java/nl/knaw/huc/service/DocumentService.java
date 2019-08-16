package nl.knaw.huc.service;

import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface DocumentService {
  Version addDocument(@Nonnull byte[] content);

  Version replaceDocument(@Nonnull UUID documentId, @Nonnull byte[] content);

  Version getLatestVersion(@Nonnull UUID documentId);
}
