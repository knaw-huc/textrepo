package nl.knaw.huc.service;

import nl.knaw.huc.api.TextRepoContents;
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
  private final ContentsService contentsService;
  private DocumentIndexer documentIndexService;
  private List<ElasticCustomFacetIndexer> customFacetIndexers;

  public JdbiVersionService(
      Jdbi jdbi,
      ContentsService contentsService,
      DocumentIndexer documentIndexService,
      List<ElasticCustomFacetIndexer> customFacetIndexers) {
    this.jdbi = jdbi;
    this.contentsService = contentsService;
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
      @Nonnull TextRepoContents contents,
      @Nonnull String filename,
      @Nonnull LocalDateTime time
  ) {
    contentsService.addContents(contents);

    var latestVersionContent = new String(contents.getContent(), UTF_8);
    documentIndexService.indexDocument(documentId, latestVersionContent);
    customFacetIndexers.forEach(indexer -> indexer.indexDocument(documentId, latestVersionContent));

    var newVersion = new Version(documentId, time, contents.getSha224());
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
