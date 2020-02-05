package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.function.Function;
import java.util.function.Supplier;

public class GetLatestFileContent implements Function<Handle, Contents> {
  private static final Logger LOG = LoggerFactory.getLogger(GetLatestFileContent.class);

  private final TextrepoFile file;

  public GetLatestFileContent(TextrepoFile file) {
    this.file = file;
  }

  @Override
  public Contents apply(Handle transaction) {
    final var versions = transaction.attach(VersionDao.class);
    final var latest = versions.findLatestByFileId(file.getId())
                               .orElseThrow(noLatestVersionFound(file));

    final var contents = transaction.attach(ContentsDao.class);
    return contents.findBySha224(latest.getContentsSha())
                   .orElseThrow(noContentsFoundForVersion(file, latest));
  }

  private Supplier<NotFoundException> noLatestVersionFound(TextrepoFile file) {
    return () -> {
      final var message = String.format("No latest version found for file: %s", file.getId());
      LOG.warn(message);
      return new NotFoundException(message);
    };
  }

  private Supplier<NotFoundException> noContentsFoundForVersion(TextrepoFile file, Version latest) {
    return () -> {
      final var message = String.format("No contents found for Latest version of file %s, sha224=%s",
          file.getId(), latest.getContentsSha());
      LOG.warn(message);
      return new NotFoundException(message);
    };
  }

}
