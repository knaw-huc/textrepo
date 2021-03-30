package nl.knaw.huc.service.task.importer;

import nl.knaw.huc.api.ResultImportDocument;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.resources.ResourceUtils;
import nl.knaw.huc.service.task.FindDocumentByExternalId;
import nl.knaw.huc.service.task.HaveDocumentByExternalId;
import nl.knaw.huc.service.task.HaveFileForDocumentByType;
import nl.knaw.huc.service.task.InTransactionProvider;
import nl.knaw.huc.service.task.SetFileContents;
import nl.knaw.huc.service.task.SetFileProvenance;
import nl.knaw.huc.service.task.Task;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;

public class JdbiImportFileTaskBuilder implements ImportFileTaskBuilder {
  private static final Logger log = LoggerFactory.getLogger(JdbiImportFileTaskBuilder.class);

  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;

  private String externalId;
  private String typeName;
  private String filename;
  private boolean allowNewDocument;
  private boolean asLatestVersion;
  private InputStream inputStream;

  public JdbiImportFileTaskBuilder(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = requireNonNull(jdbi);
    this.idGenerator = requireNonNull(idGenerator);
  }

  @Override
  public ImportFileTaskBuilder allowNewDocument(boolean allowNewDocument) {
    this.allowNewDocument = allowNewDocument;
    return this;
  }

  @Override
  public ImportFileTaskBuilder asLatestVersion(boolean asLatestVersion) {
    this.asLatestVersion = asLatestVersion;
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
  public ImportFileTaskBuilder withContents(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  @Override
  public Task<ResultImportDocument> build() {
    final InTransactionProvider<Document> documentFinder;
    if (allowNewDocument) {
      documentFinder = new HaveDocumentByExternalId(idGenerator, externalId);
    } else {
      documentFinder = new FindDocumentByExternalId(externalId);
    }
    return new JdbiImportDocumentTask(jdbi,
        documentFinder,
        idGenerator,
        typeName,
        filename,
        inputStream,
        asLatestVersion
    );
  }

  private static class JdbiImportDocumentTask implements Task<ResultImportDocument> {
    private final Jdbi jdbi;
    private final InTransactionProvider<Document> documentFinder;
    private final Supplier<UUID> idGenerator;
    private final String typeName;
    private final String filename;
    private final InputStream inputStream;
    private final boolean asLatestVersion;

    private JdbiImportDocumentTask(
        Jdbi jdbi,
        InTransactionProvider<Document> documentFinder,
        Supplier<UUID> idGenerator,
        String typeName,
        String filename,
        InputStream inputStream,
        boolean asLatestVersion
    ) {
      this.jdbi = jdbi;
      this.documentFinder = documentFinder;
      this.idGenerator = idGenerator;
      this.typeName = typeName;
      this.filename = filename;
      this.inputStream = inputStream;
      this.asLatestVersion = asLatestVersion;
    }

    @Override
    public ResultImportDocument run() {
      // To keep transaction time to a minimum, construct new Content object first, outside the transaction
      final Contents contents = ResourceUtils.readContents(inputStream);

      // Now that 'contents' is ready, enter transaction to update document, file, version and contents
      return jdbi.inTransaction(transaction -> {
        final var doc = documentFinder.executeIn(transaction);
        final var file = new HaveFileForDocumentByType(idGenerator, doc, typeName).executeIn(transaction);
        new SetFileProvenance(file, filename).executeIn(transaction);
        final var justBeforeCreation = now();
        final var version = new SetFileContents(idGenerator, file, contents, asLatestVersion)
            .executeIn(transaction);
        final var wasCreatedInThisRun = version.getCreatedAt().isAfter(justBeforeCreation);
        return new ResultImportDocument(doc, version, wasCreatedInThisRun);
      });
    }

  }

}
