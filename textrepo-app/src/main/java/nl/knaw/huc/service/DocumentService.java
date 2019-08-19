package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface DocumentService {
  Version addDocument(@Nonnull TextRepoFile file);

  Version replaceDocument(@Nonnull UUID documentId, @Nonnull TextRepoFile file);

  Version getLatestVersion(@Nonnull UUID documentId);
}
