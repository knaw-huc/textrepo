package nl.knaw.huc.resources.task;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.DocumentsDao;
import nl.knaw.huc.db.TypeDao;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

class JdbiDocumentImportBuilder implements TaskBuilder {
  private final Jdbi jdbi;

  private String externalId;
  private String type;
  private String filename;
  private InputStream inputStream;

  public JdbiDocumentImportBuilder(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public TaskBuilder forExternalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  @Override
  public TaskBuilder withType(String type) {
    this.type = type;
    return this;
  }

  @Override
  public TaskBuilder forFilename(String name) {
    this.filename = name;
    return this;
  }

  @Override
  public TaskBuilder withContents(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  @Override
  public Task build() {
    return new JdbiImportDocumentTask(externalId, type, filename, inputStream);
  }

  private class JdbiImportDocumentTask implements Task {
    private final String externalId;
    private final String typeName;
    private final String fileName;
    private final InputStream inputStream;

    public JdbiImportDocumentTask(String externalId,
                                  String typeName,
                                  String fileName,
                                  InputStream inputStream) {
      this.externalId = requireNonNull(externalId);
      this.typeName = requireNonNull(typeName);
      this.fileName = requireNonNull(fileName);
      this.inputStream = requireNonNull(inputStream);
    }

    @Override
    public void run() {
      final var type = getType();
      final var document = getDocument();
      // XXX: yuck!
      final var file = jdbi.onDemand(DocumentFilesDao.class).findFile(document.getId(), type.getName());
    }

    private Document getDocument() {
      return docs().getByExternalId(externalId)
                   .orElseThrow(notFound("No document found with external id: %s", externalId));
    }

    private Type getType() {
      // FIXME: double lookup
      final var typeId = types().find(typeName)
                                .orElseThrow(notFound("No type found with name: %s", this.typeName));
      return types().get(typeId).get();
    }

    private Supplier<NotFoundException> notFound(String format, Object... args) {
      return () -> new NotFoundException(String.format(format, args));
    }

    private TypeDao types() {
      return jdbi.onDemand(TypeDao.class);
    }

    private DocumentsDao docs() {
      return jdbi.onDemand(DocumentsDao.class);
    }
  }
}
