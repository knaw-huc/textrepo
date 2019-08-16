package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.FileDao;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class JdbiDocumentService implements DocumentService {
  private final Jdbi jdbi;
  private final IdGenerator<UUID> documentIdGenerator;

  public JdbiDocumentService(Jdbi jdbi, IdGenerator<UUID> documentIdGenerator) {
    this.jdbi = jdbi;
    this.documentIdGenerator = documentIdGenerator;
  }

  @Override
  public Version addDocument(@Nonnull byte[] content) {
    final var file = TextRepoFile.fromContent(content);
    return insertNewVersion(documentIdGenerator.nextUniqueId(), file);
  }

  @Override
  public Version replaceDocument(@Nonnull UUID documentId, @Nonnull byte[] content) {
    final var replacementFile = TextRepoFile.fromContent(content);

    return findLatest(documentId)
            .filter(withSha224EqualTo(replacementFile)) // already the current version, nothing to do
            .orElseGet(() -> insertNewVersion(documentId, replacementFile));
  }

  @Override
  public Version getLatestVersion(@Nonnull UUID documentId) {
    return findLatest(documentId).orElseThrow(() -> new NotFoundException("No document for uuid: " + documentId));
  }

  private Version insertNewVersion(@Nonnull UUID documentId, @Nonnull TextRepoFile file) {
    getFileDao().insert(file);
    var newVersion = new Version(documentId, LocalDateTime.now(), file.getSha224());
    getVersionDao().insert(newVersion);
    return newVersion;
  }

  private Optional<Version> findLatest(@Nonnull UUID documentId) {
    return getVersionDao().findLatestByDocumentUuid(documentId);
  }

  private Predicate<Version> withSha224EqualTo(@Nonnull TextRepoFile replacementFile) {
    return v -> v.getFileSha().equals(replacementFile.getSha224());
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }

  private FileDao getFileDao() {
    return jdbi.onDemand(FileDao.class);
  }
}
