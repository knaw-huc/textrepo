package nl.knaw.huc.service.task.importer;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.HaveDocumentByExternalId;
import nl.knaw.huc.service.task.HaveFileForDocumentByType;
import nl.knaw.huc.service.task.ProvidesInTransaction;
import nl.knaw.huc.service.task.SetCurrentFileContents;
import nl.knaw.huc.service.task.SetFileProvenance;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static nl.knaw.huc.core.Contents.fromBytes;

public class JdbiImportFileTaskBuilder implements ImportFileTaskBuilder {
  private final Jdbi jdbi;
  private final Supplier<UUID> documentIdGenerator;
  private final Supplier<UUID> fileIdGenerator;
  private final Supplier<UUID> versionIdGenerator;

  private String externalId;
  private String typeName;
  private String filename;
  private byte[] contents;
  private boolean allowNewDocument;

  public JdbiImportFileTaskBuilder(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = requireNonNull(jdbi);
    this.documentIdGenerator = requireNonNull(idGenerator);
    this.fileIdGenerator = requireNonNull(idGenerator);
    this.versionIdGenerator = requireNonNull(idGenerator);
  }

  @Override
  public ImportFileTaskBuilder allowNewDocument(boolean allowNewDocument) {
    this.allowNewDocument = allowNewDocument;
    return this;
  }

  @Override
  public ImportFileTaskBuilder forExternalId(String externalId) {
    this.externalId = requireNonNull(externalId);
    return this;
  }

  @Override
  public ImportFileTaskBuilder withTypeName(String typeName) {
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
  public Task<Version> build() {
    final ProvidesInTransaction<Document> documentFinder;
    if (allowNewDocument) {
      documentFinder = new HaveDocumentByExternalId(documentIdGenerator, externalId);
    } else {
      documentFinder = new FindDocumentByExternalId(externalId);
    }
    return new JdbiImportDocumentTask(documentFinder, typeName, filename, fromBytes(contents));
  }

  private class JdbiImportDocumentTask implements Task<Version> {
    private final ProvidesInTransaction<Document> documentFinder;
    private final String typeName;
    private final String filename;
    private final Contents contents;

    private JdbiImportDocumentTask(ProvidesInTransaction<Document> documentFinder,
                                   String typeName, String filename, Contents contents) {
      this.documentFinder = documentFinder;
      this.typeName = typeName;
      this.filename = filename;
      this.contents = contents;
    }

    @Override
    public Version run() {
      return jdbi.inTransaction(transaction -> {
        final Document doc = documentFinder.executeIn(transaction);
        final var file = new HaveFileForDocumentByType(fileIdGenerator, doc, typeName).executeIn(transaction);
        final var entry = new SetFileProvenance(file, filename).executeIn(transaction);
        return new SetCurrentFileContents(versionIdGenerator, file, contents).executeIn(transaction);
      });
    }
  }
}
