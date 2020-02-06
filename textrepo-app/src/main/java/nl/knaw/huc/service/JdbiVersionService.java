package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.VersionsDao;
import nl.knaw.huc.service.index.ElasticCustomIndexer;
import nl.knaw.huc.service.index.FileIndexer;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class JdbiVersionService implements VersionService {
  private final Jdbi jdbi;
  private final ContentsService contentsService;
  private FileIndexer fileIndexService;
  private List<ElasticCustomIndexer> customFacetIndexers;
  private Supplier<UUID> uuidGenerator;

  public JdbiVersionService(
      Jdbi jdbi,
      ContentsService contentsService,
      FileIndexer fileIndexService,
      List<ElasticCustomIndexer> customFacetIndexers,
      Supplier<UUID> uuidGenerator
  ) {
    this.jdbi = jdbi;
    this.contentsService = contentsService;
    this.fileIndexService = fileIndexService;
    this.customFacetIndexers = customFacetIndexers;
    this.uuidGenerator = uuidGenerator;
  }

  @Override
  public Optional<Version> findLatestVersion(@Nonnull UUID fileId) {
    return getVersionDao().findLatestByFileId(fileId);
  }

  @Override
  public Version insertNewVersion(
      @Nonnull TextrepoFile file,
      @Nonnull Contents contents,
      @Nonnull LocalDateTime time
  ) {
    contentsService.addContents(contents);

    var latestVersionContent = contents.asUtf8String();
    fileIndexService.indexFile(file, latestVersionContent);
    customFacetIndexers.forEach(indexer -> indexer.indexFile(file, latestVersionContent));

    var id = uuidGenerator.get();
    var newVersion = new Version(id, file.getId(), time, contents.getSha224());
    getVersionDao().insert(newVersion);
    return newVersion;
  }

  @Override
  public List<Version> getVersions(UUID fileId) {
    return getVersionDao().findByFileId(fileId);
  }

  private VersionsDao getVersionDao() {
    return jdbi.onDemand(VersionsDao.class);
  }
}
