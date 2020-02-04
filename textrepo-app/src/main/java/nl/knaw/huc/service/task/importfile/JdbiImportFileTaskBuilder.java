package nl.knaw.huc.service.task.importfile;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.db.TypeDao;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

class JdbiImportFileTaskBuilder implements ImportFileTaskBuilder {
  private final Jdbi jdbi;
  private final Supplier<UUID> documentIdGenerator;
  private final Supplier<UUID> fileIdGenerator;

  private String externalId;
  private String typeName;
  private String filename;
  private byte[] contents;

  public JdbiImportFileTaskBuilder(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.documentIdGenerator = idGenerator;
    this.fileIdGenerator = idGenerator;
  }

  @Override
  public ImportFileTaskBuilder forExternalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  @Override
  public ImportFileTaskBuilder withType(String typeName) {
    this.typeName = typeName;
    return this;
  }

  @Override
  public ImportFileTaskBuilder forFilename(String name) {
    this.filename = name;
    return this;
  }

  @Override
  public ImportFileTaskBuilder withContents(byte[] contents) {
    this.contents = contents;
    return this;
  }

  @Override
  public Task build() {
    return new JdbiImportDocumentTask(externalId, getTypeId(), filename, getContents());
  }

  private short getTypeId() {
    return types().find(typeName).orElseThrow(typeNotFound(typeName));
  }

  private Contents getContents() {
    return Contents.fromContent(contents);
  }

  private TypeDao types() {
    return jdbi.onDemand(TypeDao.class);
  }

  private Supplier<NotFoundException> typeNotFound(String name) {
    return () -> new NotFoundException(String.format("No type found with name: %s", name));
  }

  private class JdbiImportDocumentTask implements Task {
    private final String externalId;
    private final short typeId;
    private final String filename;
    private final Contents contents;

    private JdbiImportDocumentTask(String externalId, short typeId, String filename, Contents contents) {
      this.externalId = externalId;
      this.typeId = typeId;
      this.filename = filename;
      this.contents = contents;
    }

    @Override
    public void run() {
      jdbi.useTransaction(txn ->
          new HaveDocumentByExternalId(txn, documentIdGenerator)
              .andThen(new HaveFileForDocumentByType(txn, fileIdGenerator, typeId))
              .andThen(new SetFileProvenance(txn, filename))
              .andThen(new SetCurrentFileContents(txn, contents))
              .apply(externalId));
    }
  }
}
