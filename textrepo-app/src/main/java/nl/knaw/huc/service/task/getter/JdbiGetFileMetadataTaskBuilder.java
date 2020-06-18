package nl.knaw.huc.service.task.getter;

import nl.knaw.huc.core.FileMetadata;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.FindFileByDocumentIdAndFileType;
import nl.knaw.huc.service.task.GetDocumentMetadata;
import nl.knaw.huc.service.task.GetFileMetadata;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

public class JdbiGetFileMetadataTaskBuilder implements GetFileMetadataTaskBuilder {
  private String externalId;
  private String typeName;
  private Jdbi jdbi;

  public JdbiGetFileMetadataTaskBuilder(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public GetFileMetadataTaskBuilder forExternalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  @Override
  public GetFileMetadataTaskBuilder forType(String typeName) {
    this.typeName = typeName;
    return this;
  }

  @Override
  public Task<FileMetadata> build() {
    return new GetFileMetadataTask();
  }

  private class GetFileMetadataTask implements Task<FileMetadata> {

    @Override
    public FileMetadata run() {
      return jdbi.inTransaction(transaction -> {
        var result = new FileMetadata();

        var doc = new FindDocumentByExternalId(externalId).executeIn(transaction);
        var file = new FindFileByDocumentIdAndFileType(doc.getId(), typeName).executeIn(transaction);
        var metadata = new GetFileMetadata(file.getId()).executeIn(transaction);

        result.setFile(file);
        result.setMetadata(metadata);
        return result;
      });
    }
  }

}
