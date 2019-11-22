package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.VersionDao;
import nl.knaw.huc.service.index.FileIndexer;
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
  private FileIndexer fileIndexService;
  private List<ElasticCustomFacetIndexer> customFacetIndexers;

  public JdbiVersionService(
      Jdbi jdbi,
      ContentsService contentsService,
      FileIndexer fileIndexService,
      List<ElasticCustomFacetIndexer> customFacetIndexers) {
    this.jdbi = jdbi;
    this.contentsService = contentsService;
    this.fileIndexService = fileIndexService;
    this.customFacetIndexers = customFacetIndexers;
  }

  @Override
  public Optional<Version> findLatestVersion(@Nonnull UUID fileId) {
    return getVersionDao().findLatestByFileUuid(fileId);
  }

  @Override
  public Version insertNewVersion(
      @Nonnull UUID fileId,
      @Nonnull Contents contents,
      @Nonnull String filename,
      @Nonnull LocalDateTime time
  ) {
    contentsService.addContents(contents);

    var latestVersionContent = new String(contents.getContent(), UTF_8);
    fileIndexService.indexFile(fileId, latestVersionContent);
    customFacetIndexers.forEach(indexer -> indexer.indexFile(fileId, latestVersionContent));

    var newVersion = new Version(fileId, time, contents.getSha224());
    getVersionDao().insert(newVersion);
    return newVersion;
  }

  @Override
  public List<Version> getVersions(UUID fileId) {
    return getVersionDao().findByUuid(fileId);
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }
}
