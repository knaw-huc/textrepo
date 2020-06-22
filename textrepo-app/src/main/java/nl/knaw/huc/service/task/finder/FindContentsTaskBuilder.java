package nl.knaw.huc.service.task.finder;

import nl.knaw.huc.service.task.Task;

import java.util.UUID;

public interface FindContentsTaskBuilder {
  FindContentsTaskBuilder forExternalId(String externalId);

  FindContentsTaskBuilder withType(String typeName);

  Task<LatestFileContents> build();

  class LatestFileContents {

    private UUID fileId;
    private byte[] contents;
    private Short typeId;

    public UUID getFileId() {
      return fileId;
    }

    public void setFileId(UUID fileId) {
      this.fileId = fileId;
    }

    public byte[] getContents() {
      return contents;
    }

    public void setContents(byte[] contents) {
      this.contents = contents;
    }

    public Short getTypeId() {
      return typeId;
    }

    public void setTypeId(Short typeId) {
      this.typeId = typeId;
    }
  }

}
