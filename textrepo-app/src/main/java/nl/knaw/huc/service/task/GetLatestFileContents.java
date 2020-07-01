package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.TextRepoFile;
import org.jdbi.v3.core.Handle;

import static java.util.Objects.requireNonNull;

public class GetLatestFileContents implements InTransactionProvider<Contents> {
  private final TextRepoFile file;

  public GetLatestFileContents(TextRepoFile file) {
    this.file = requireNonNull(file);
  }

  @Override
  public Contents executeIn(Handle transaction) {
    requireNonNull(transaction);
    final var latest = new GetLatestFileVersion(file).executeIn(transaction);
    return new GetVersionContent(latest).executeIn(transaction);
  }

}
