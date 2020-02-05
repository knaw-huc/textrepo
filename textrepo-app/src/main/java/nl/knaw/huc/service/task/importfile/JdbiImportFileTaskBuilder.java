package nl.knaw.huc.service.task.importfile;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.db.TypeDao;
import nl.knaw.huc.service.task.HaveDocumentByExternalId;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

class JdbiImportFileTaskBuilder implements ImportFileTaskBuilder {
  private final Jdbi jdbi;
  private final Supplier<UUID> documentIdGenerator;
  private final Supplier<UUID> fileIdGenerator;

  private String externalId;
  private String typeName;
  private String filename;
  private byte[] contents;

  public JdbiImportFileTaskBuilder(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = requireNonNull(jdbi);
    this.documentIdGenerator = requireNonNull(idGenerator);
    this.fileIdGenerator = requireNonNull(idGenerator);
  }

  @Override
  public ImportFileTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public ImportFileTaskBuilder withType(String typeName) {
    this.typeName = requireNonNull(typeName);
    return this;
  }

  @Override
  public ImportFileTaskBuilder forFilename(String name) {
    this.filename = requireNonNull(name);
    return this;
  }

  @Override
  public ImportFileTaskBuilder withContents(byte[] contents) {
    this.contents = requireNonNull(contents);
    return this;
  }

  @Override
  public Task build() {
    return new JdbiImportDocumentTask(externalId, typeName, filename, getContents());
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
    private final String typeName;
    private final String filename;
    private final Contents contents;

    private JdbiImportDocumentTask(String externalId, String typeName, String filename, Contents contents) {
      this.externalId = externalId;
      this.typeName = typeName;
      this.filename = filename;
      this.contents = contents;
    }

    @Override
    public void run() {
      jdbi.useTransaction(txn -> {
        final var doc = new HaveDocumentByExternalId(documentIdGenerator, externalId).apply(txn);
        final var file = new HaveFileForDocumentByType(fileIdGenerator, doc, typeName).apply(txn);
        final var entry = new SetFileProvenance(file, filename).apply(txn);
        final var version = new SetCurrentFileContents(file, contents).apply(txn);
      });
    }
  }
}
