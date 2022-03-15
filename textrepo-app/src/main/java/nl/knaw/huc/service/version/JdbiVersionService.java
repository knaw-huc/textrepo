package nl.knaw.huc.service.version;

import static java.lang.String.format;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Page;
import nl.knaw.huc.core.PageParams;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.FilesDao;
import nl.knaw.huc.db.VersionsDao;
import nl.knaw.huc.service.contents.ContentsService;
import nl.knaw.huc.service.index.IndexService;
import nl.knaw.huc.service.task.DeleteVersion;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbiVersionService implements VersionService {

  private static final Logger log = LoggerFactory.getLogger(JdbiVersionService.class);

  private final Jdbi jdbi;
  private final ContentsService contentsService;
  private final Supplier<UUID> uuidGenerator;
  private final IndexService indexService;

  public JdbiVersionService(
      Jdbi jdbi,
      ContentsService contentsService,
      Supplier<UUID> uuidGenerator,
      IndexService indexService
  ) {
    this.jdbi = jdbi;
    this.contentsService = contentsService;
    this.uuidGenerator = uuidGenerator;
    this.indexService = indexService;
  }

  @Override
  public Version createNewVersion(
      @Nonnull UUID fileId,
      @Nonnull Contents contents
  ) {
    var file = files()
        .find(fileId)
        .orElseThrow(() -> new NotFoundException(
            format("Could not create new version: file %s not found", fileId)));
    return createNewVersion(file, contents);
  }

  private Version createNewVersion(
      @Nonnull TextRepoFile file,
      @Nonnull Contents contents
  ) {
    contentsService.addContents(contents);
    var latestVersionContents = contents.asUtf8String();
    var id = uuidGenerator.get();
    var newVersion = new Version(id, file.getId(), contents.getSha224());
    newVersion = versions().insert(newVersion);
    indexService.index(file, latestVersionContents);
    return newVersion;
  }

  @Override
  public Page<Version> getAll(UUID fileId, PageParams pageParams, LocalDateTime createdAfter) {
    var total = versions().countByFileId(fileId, createdAfter);
    if (total == 0) {
      if (jdbi.onDemand(FilesDao.class).find(fileId).isEmpty()) {
        throw new NotFoundException(format("No file with ID %s", fileId));
      }
    }
    var items = versions().findByFileId(fileId, pageParams, createdAfter);
    return new Page<>(items, total, pageParams);
  }

  @Override
  public Version get(UUID id) {
    return versions()
        .find(id)
        .orElseThrow(() -> new NotFoundException(format("Version %s could not be found", id)));
  }


  private static class DeleteVersionResult {
    public boolean deletedIsLatestVersion;
    public Version version;

    public DeleteVersionResult(Version version, boolean deletedIsLatestVersion) {
      this.version = version;
      this.deletedIsLatestVersion = deletedIsLatestVersion;
    }
  }

  @Override
  public void delete(UUID id) {
    var result = jdbi.inTransaction(handle -> {
      var versionsDao = handle.attach(VersionsDao.class);
      var found = versionsDao.find(id);

      if (found.isEmpty()) {
        throw new NotFoundException(format("Could not find version with id %s", id));
      }
      var latestVersion = isLatestVersion(found.get(), versionsDao);
      new DeleteVersion(found.get()).executeIn(handle);
      return new DeleteVersionResult(found.get(), latestVersion);
    });

    if (result.deletedIsLatestVersion) {
      log.debug("Deleted version was latest version: reindex file");
      indexService.index(result.version.getFileId());
    }
  }

  private boolean isLatestVersion(Version version, VersionsDao versionsDao) {
    var latestVersion = versionsDao.findLatestByFileId(version.getFileId());
    return latestVersion.isPresent()
        && latestVersion.get().getId().equals(version.getId());
  }

  private FilesDao files() {
    return jdbi.onDemand(FilesDao.class);
  }

  private VersionsDao versions() {
    return jdbi.onDemand(VersionsDao.class);
  }

  private ContentsDao contents() {
    return jdbi.onDemand(ContentsDao.class);
  }

}
