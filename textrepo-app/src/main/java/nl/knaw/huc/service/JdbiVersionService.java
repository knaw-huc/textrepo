package nl.knaw.huc.service;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import nl.knaw.huc.service.index.ElasticCustomIndexer;
import nl.knaw.huc.service.index.FileIndexer;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;

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
    return versions().findLatestByFileId(fileId);
  }

  @Override
  public Version createNewVersion(
      @Nonnull UUID fileId,
      @Nonnull Contents contents,
      @Nonnull LocalDateTime time
  ) {
    var file = files()
        .find(fileId)
        .orElseThrow(() -> new NotFoundException(format("Could not create new version: file %s not found", fileId)));
    return createNewVersion(file, contents, time);
  }

  @Override
  public Version createNewVersion(
      @Nonnull TextrepoFile file,
      @Nonnull Contents contents,
      @Nonnull LocalDateTime time
  ) {
    contentsService.addContents(contents);
    var latestVersionContents = contents.asUtf8String();
    fileIndexService.indexFile(file, latestVersionContents);
    customFacetIndexers.forEach(indexer -> indexer.indexFile(file, latestVersionContents));

    var id = uuidGenerator.get();
    var newVersion = new Version(id, file.getId(), time, contents.getSha224());
    versions().insert(newVersion);
    return newVersion;
  }

  @Override
  public List<Version> getAll(UUID fileId) {
    return versions().findByFileId(fileId);
  }

  @Override
  public Version get(UUID id) {
    return versions()
        .find(id)
        .orElseThrow(() -> new NotFoundException(format("Version %s could not be found", id)));
  }

  @Override
  public void delete(UUID id) {
    versions().delete(id);
  }

  private FilesDao files() {
    return jdbi.onDemand(FilesDao.class);
  }

  private VersionsDao versions() {
    return jdbi.onDemand(VersionsDao.class);
  }

}
