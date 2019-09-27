package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.index.DocumentIndexer;
import nl.knaw.huc.service.index.ElasticCustomFacetIndexer;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JdbiVersionService implements VersionService {
  private final Jdbi jdbi;
  private final FileService fileService;
  private DocumentIndexer documentIndexService;
  private List<ElasticCustomFacetIndexer> customFacetIndexers;

  public JdbiVersionService(
      Jdbi jdbi,
      FileService fileService,
      DocumentIndexer documentIndexService,
      List<ElasticCustomFacetIndexer> customFacetIndexers) {
    this.jdbi = jdbi;
    this.fileService = fileService;
    this.documentIndexService = documentIndexService;
    this.customFacetIndexers = customFacetIndexers;
  }

  @Override
  public Optional<Version> findLatestVersion(@Nonnull UUID documentId) {
    return getVersionDao().findLatestByDocumentUuid(documentId);
  }

  @Override
  public Version insertNewVersion(
      @Nonnull UUID documentId,
      @Nonnull TextRepoFile file,
      @Nonnull String filename,
      @Nonnull LocalDateTime time
  ) {
    fileService.addFile(file);

    var latestVersionContent = new String(file.getContent(), UTF_8);
    documentIndexService.indexDocument(documentId, latestVersionContent);
    customFacetIndexers.forEach(indexer -> indexer.indexDocument(documentId, latestVersionContent));

    var newVersion = new Version(documentId, time, file.getSha224());
    getVersionDao().insert(newVersion);
    return newVersion;
  }

  @Override
  public List<Version> getVersions(UUID documentId) {
    return getVersionDao().findByUuid(documentId);
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }
}
