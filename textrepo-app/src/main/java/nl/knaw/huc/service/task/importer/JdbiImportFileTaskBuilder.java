package nl.knaw.huc.service.task.importer;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.resources.ResourceUtils;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.HaveDocumentByExternalId;
import nl.knaw.huc.service.task.HaveFileForDocumentByType;
import nl.knaw.huc.service.task.InTransactionProvider;
import nl.knaw.huc.service.task.SetCurrentFileContents;
import nl.knaw.huc.service.task.SetFileProvenance;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class JdbiImportFileTaskBuilder implements ImportFileTaskBuilder {
  private static final Logger log = LoggerFactory.getLogger(JdbiImportFileTaskBuilder.class);

  private final Jdbi jdbi;
  private final Supplier<UUID> documentIdGenerator;
  private final Supplier<UUID> fileIdGenerator;
  private final Supplier<UUID> versionIdGenerator;

  private String externalId;
  private String typeName;
  private String filename;
  private boolean allowNewDocument;
  private InputStream inputStream;

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
  public ImportFileTaskBuilder withInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  @Override
  public Task<Version> build() {
    final InTransactionProvider<Document> documentFinder;
    if (allowNewDocument) {
      documentFinder = new HaveDocumentByExternalId(documentIdGenerator, externalId);
    } else {
      documentFinder = new FindDocumentByExternalId(externalId);
    }
    return new JdbiImportDocumentTask(documentFinder, typeName, filename, inputStream);
  }

  private class JdbiImportDocumentTask implements Task<Version> {
    private final InTransactionProvider<Document> documentFinder;
    private final String typeName;
    private final String filename;
    private final InputStream inputStream;

    private JdbiImportDocumentTask(InTransactionProvider<Document> documentFinder,
                                   String typeName, String filename, InputStream inputStream) {
      this.documentFinder = documentFinder;
      this.typeName = typeName;
      this.filename = filename;
      this.inputStream = inputStream;
    }

    @Override
    public Version run() {
      // To keep transaction time to a minimum, construct new Content object first, outside the transaction
      final Contents contents = ResourceUtils.readContents(inputStream);

      // Now that 'contents' is ready, enter transaction to update document, file, version and contents
      return jdbi.inTransaction(transaction -> {
        final var doc = documentFinder.executeIn(transaction);
        final var file = new HaveFileForDocumentByType(fileIdGenerator, doc, typeName).executeIn(transaction);
        new SetFileProvenance(file, filename).executeIn(transaction);
        return new SetCurrentFileContents(versionIdGenerator, file, contents).executeIn(transaction);
      });
    }

  }

}
