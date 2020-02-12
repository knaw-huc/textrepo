package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.task.finder.FindFileTaskBuilder;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

public class JdbiFindFileTaskBuilder implements FindFileTaskBuilder {
  private final Jdbi jdbi;

  private UUID fileId;

  public JdbiFindFileTaskBuilder(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public FindFileTaskBuilder forFile(UUID fileId) {
    this.fileId = fileId;
    return this;
  }

  @Override
  public Task<Version> build() {
    return new FindFileTask(fileId);
  }

  private class FindFileTask implements Task<Version> {
    private final UUID fileId;

    public FindFileTask(UUID fileId) {
      this.fileId = fileId;
    }

    @Override
    public Version run() {
      return jdbi.inTransaction(txn -> {
        final var file = new FindFile(fileId).executeIn(txn);
        return new GetLatestFileVersion(file).executeIn(txn);
      });
    }
  }
}
