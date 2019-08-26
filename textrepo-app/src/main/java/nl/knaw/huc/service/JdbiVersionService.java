package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.index.ElasticDocumentIndexer;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JdbiVersionService implements VersionService {
  private final Jdbi jdbi;
  private final FileService fileService;
  private ElasticDocumentIndexer documentIndexService;
  private final MetadataService metadataService;

  public JdbiVersionService(
      Jdbi jdbi,
      FileService fileService,
      ElasticDocumentIndexer documentIndexService,
      MetadataService metadataService) {
    this.jdbi = jdbi;
    this.fileService = fileService;
    this.documentIndexService = documentIndexService;
    this.metadataService = metadataService;
  }

  @Override
  public Optional<Version> findLatestVersion(@Nonnull UUID documentId) {
    return getVersionDao().findLatestByDocumentUuid(documentId);
  }

  @Override
  public Version insertNewVersion(@Nonnull UUID documentId, @Nonnull TextRepoFile file, @Nonnull String filename,
                                  @Nonnull LocalDateTime time) {
    fileService.addFile(file);
    documentIndexService.indexDocument(documentId, new String(file.getContent(), UTF_8));
    var newVersion = new Version(documentId, time, file.getSha224());
    getVersionDao().insert(newVersion);
    return newVersion;
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }
}
