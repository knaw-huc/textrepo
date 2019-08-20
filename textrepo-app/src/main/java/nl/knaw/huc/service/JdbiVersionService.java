package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class JdbiVersionService implements VersionService {
  private final Jdbi jdbi;
  private final FileService fileService;

  public JdbiVersionService(Jdbi jdbi, FileService fileService) {
    this.jdbi = jdbi;
    this.fileService = fileService;
  }

  @Override
  public Optional<Version> findLatestVersion(@Nonnull UUID documentId) {
    return getVersionDao().findLatestByDocumentUuid(documentId);
  }

  @Override
  public Version insertNewVersion(@Nonnull UUID documentId, @Nonnull TextRepoFile file) {
    fileService.addFile(file);
    var newVersion = new Version(documentId, LocalDateTime.now(), file.getSha224());
    getVersionDao().insert(newVersion);
    return newVersion;
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }
}
