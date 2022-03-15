package nl.knaw.huc.service.task;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetVersionContent implements InTransactionProvider<Contents> {
  private static final Logger log = LoggerFactory.getLogger(GetVersionContent.class);

  private final Version version;

  public GetVersionContent(Version version) {
    this.version = requireNonNull(version);
  }

  @Override
  public Contents executeIn(Handle transaction) {
    return transaction.attach(ContentsDao.class)
                      .findBySha224(version.getContentsSha())
                      .orElseThrow(noContentsFoundForVersion(version));
  }

  private Supplier<NotFoundException> noContentsFoundForVersion(Version latest) {
    return () -> {
      final var message = String.format("No contents found for version of file %s, sha224=%s",
          version.getFileId(), latest.getContentsSha());
      log.warn(message);
      return new NotFoundException(message);
    };
  }
}
