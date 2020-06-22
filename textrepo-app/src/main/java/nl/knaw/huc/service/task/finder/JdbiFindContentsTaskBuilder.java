package nl.knaw.huc.service.task.finder;

import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindDocumentFileByType;
import nl.knaw.huc.service.task.GetLatestFileVersion;
import nl.knaw.huc.service.task.GetVersionContent;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import java.util.Objects;

public class JdbiFindContentsTaskBuilder implements FindContentsTaskBuilder {
  private final Jdbi jdbi;

  private String externalId;
  private String typeName;

  public JdbiFindContentsTaskBuilder(Jdbi jdbi) {
    this.jdbi = Objects.requireNonNull(jdbi);
  }

  @Override
  public FindContentsTaskBuilder forExternalId(String externalId) {
    this.externalId = Objects.requireNonNull(externalId);
    return this;
  }

  @Override
  public FindContentsTaskBuilder withType(String typeName) {
    this.typeName = Objects.requireNonNull(typeName);
    return this;
  }

  @Override
  public Task<LatestFileContents> build() {
    return new FindContentsTask(externalId, typeName);
  }

  private class FindContentsTask implements Task<LatestFileContents> {

    private final String externalId;
    private final String typeName;

    public FindContentsTask(String externalId, String typeName) {
      this.externalId = externalId;
      this.typeName = typeName;
    }

    @Override
    public LatestFileContents run() {
      return jdbi.inTransaction(txn -> {
        final var doc = new FindDocumentByExternalId(externalId).executeIn(txn);
        final var file = new FindDocumentFileByType(doc, typeName).executeIn(txn);
        final var version = new GetLatestFileVersion(file).executeIn(txn);
        var contents = new GetVersionContent(version).executeIn(txn);
        var result = new LatestFileContents();
        result.setFileId(file.getId());
        result.setTypeId(file.getTypeId());
        result.setContents(contents.getContents());
        return result;
      });
    }
  }

}
