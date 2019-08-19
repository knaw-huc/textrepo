package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

public class JdbiVersionService implements VersionService {
  private final Jdbi jdbi;
  private final FileService fileService;

  public JdbiVersionService(Jdbi jdbi, FileService fileService) {
    this.jdbi = jdbi;
    this.fileService = fileService;
  }

  @Override
  public Version getLatestVersion(@Nonnull UUID documentId) {
    return findLatest(documentId)
        .orElseThrow(() -> new NotFoundException(format("No version found for document: %s", documentId)));
  }

  @Override
  public Version insertNewVersion(@Nonnull UUID documentId, @Nonnull TextRepoFile file) {
    fileService.addFile(file);
    var newVersion = new Version(documentId, LocalDateTime.now(), file.getSha224());
    getVersionDao().insert(newVersion);
    return newVersion;
  }

  @Override
  public Version replace(@Nonnull UUID documentId, @Nonnull TextRepoFile file) {
    return findLatest(documentId)
        .filter(v -> v.getFileSha().equals(file.getSha224())) // already the current version
        .orElseGet(() -> insertNewVersion(documentId, file));
  }

  private Optional<Version> findLatest(@Nonnull UUID documentId) {
    return getVersionDao().findLatestByDocumentUuid(documentId);
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }
}
