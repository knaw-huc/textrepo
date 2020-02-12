package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextrepoFile;
import org.jdbi.v3.core.Handle;

import static java.util.Objects.requireNonNull;

public class GetLatestFileContents implements ProvidesInTransaction<Contents> {
  private final TextrepoFile file;

  public GetLatestFileContents(TextrepoFile file) {
    this.file = requireNonNull(file);
  }

  @Override
  public Contents executeIn(Handle transaction) {
    requireNonNull(transaction);
    final var latest = new GetLatestFileVersion(file).executeIn(transaction);
    return new GetVersionContent(latest).executeIn(transaction);
  }

}
