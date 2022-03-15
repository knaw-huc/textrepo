package nl.knaw.huc.service.task.finder;

import java.util.UUID;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.service.task.Task;

public interface FindContentsTaskBuilder {
  FindContentsTaskBuilder forExternalId(String externalId);

  FindContentsTaskBuilder withType(String typeName);

  Task<LatestFileContents> build();

  class LatestFileContents {

    private UUID fileId;
    private Contents contents;
    private Type type;

    public UUID getFileId() {
      return fileId;
    }

    public void setFileId(UUID fileId) {
      this.fileId = fileId;
    }

    public Contents getContents() {
      return contents;
    }

    public void setContents(Contents contents) {
      this.contents = contents;
    }

    public Type getType() {
      return type;
    }

    public void setType(Type type) {
      this.type = type;
    }
  }

}
