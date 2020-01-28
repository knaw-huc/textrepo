package nl.knaw.huc.resources.task;

import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.core.Contents;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.core.Version;
import nl.knaw.huc.db.ContentsDao;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.DocumentsDao;
import nl.knaw.huc.db.FileDao;
import nl.knaw.huc.db.MetadataDao;
import nl.knaw.huc.db.TypeDao;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotFoundException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.time.LocalDateTime.now;
import static nl.knaw.huc.resources.ResourceUtils.readContent;

class JdbiDocumentImportBuilder implements TaskBuilder {
  private final Jdbi jdbi;
  private final Supplier<UUID> idGenerator;

  private String externalId;
  private String typeName;
  private String filename;
  private InputStream inputStream;

  public JdbiDocumentImportBuilder(Jdbi jdbi, Supplier<UUID> idGenerator) {
    this.jdbi = jdbi;
    this.idGenerator = idGenerator;
  }

  @Override
  public TaskBuilder forExternalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  @Override
  public TaskBuilder withType(String typeName) {
    this.typeName = typeName;
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
    final var typeId = getTypeId();
    final var contents = Contents.fromContent(readContent(inputStream));

    final var getDocument = new GetOrCreateDocument(jdbi, idGenerator);
    final var getDocumentFileByType = new GetOrCreateFile(jdbi, idGenerator, typeId);
    final var updateFilename = new UpdateFilename(jdbi, filename);
    final var getVersion = new GetOrCreateVersion(jdbi, contents);

    var task = getDocument.andThen(getDocumentFileByType)
                          .andThen(updateFilename)
                          .andThen(getVersion);

    return new JdbiImportDocumentTask(task, externalId);
  }

  private short getTypeId() {
    return types().find(typeName).orElseThrow(typeNotFound(typeName));
  }

  private TypeDao types() {
    return jdbi.onDemand(TypeDao.class);
  }

  private Supplier<NotFoundException> typeNotFound(String name) {
    return () -> new NotFoundException(String.format("No type found with name: %s", name));
  }

  private static class UpdateFilename implements Function<TextrepoFile, TextrepoFile> {
    private final Jdbi jdbi;
    private final String filename;

    private UpdateFilename(Jdbi jdbi, String filename) {
      this.jdbi = jdbi;
      this.filename = filename;
    }

    @Override
    public TextrepoFile apply(TextrepoFile file) {
      metadata().updateFileMetadata(file.getId(), new MetadataEntry("filename", filename));
      return file;
    }

    private MetadataDao metadata() {
      return jdbi.onDemand(MetadataDao.class);
    }
  }

  private static class GetOrCreateVersion implements Function<TextrepoFile, Version> {
    private final Jdbi jdbi;
    private final Contents contents;

    private GetOrCreateVersion(Jdbi jdbi, Contents contents) {
      this.jdbi = jdbi;
      this.contents = contents;
    }

    @Override
    public Version apply(TextrepoFile file) {
      return versions().findLatestByFileId(file.getId())
                       .filter(v -> v.getContentsSha().equals(contents.getSha224()))
                       .orElseGet(() -> createVersion(file));
    }

    private Version createVersion(TextrepoFile file) {
      contents().insert(contents);
      return new Version(file.getId(), now(), contents.getSha224());
    }

    private VersionDao versions() {
      return jdbi.onDemand(VersionDao.class);
    }

    private ContentsDao contents() {
      return jdbi.onDemand(ContentsDao.class);
    }
  }

  private static class GetOrCreateFile implements Function<Document, TextrepoFile> {
    private final Jdbi jdbi;
    private final Supplier<UUID> idGenerator;
    private final short typeId;

    private GetOrCreateFile(Jdbi jdbi, Supplier<UUID> idGenerator, short typeId) {
      this.jdbi = jdbi;
      this.idGenerator = idGenerator;
      this.typeId = typeId;
    }

    @Override
    public TextrepoFile apply(Document doc) {
      return df().findFile(doc.getId(), typeId).orElseGet(() -> createFile(typeId));
    }

    private TextrepoFile createFile(short typeId) {
      final var fileId = idGenerator.get();
      files().create(fileId, typeId);
      return new TextrepoFile(fileId, typeId);
    }

    private DocumentFilesDao df() {
      return jdbi.onDemand(DocumentFilesDao.class);
    }

    private FileDao files() {
      return jdbi.onDemand(FileDao.class);
    }
  }

  private static class GetOrCreateDocument implements Function<String, Document> {
    private final Jdbi jdbi;
    private final Supplier<UUID> idGenerator;

    private GetOrCreateDocument(Jdbi jdbi, Supplier<UUID> idGenerator) {
      this.jdbi = jdbi;
      this.idGenerator = idGenerator;
    }

    @Override
    public Document apply(String externalId) {
      return docs().getByExternalId(externalId).orElseGet(() -> createDocument(externalId));
    }

    private Document createDocument(String externalId) {
      final var document = new Document(idGenerator.get(), externalId);
      docs().insert(document);
      return document;
    }

    private DocumentsDao docs() {
      return jdbi.onDemand(DocumentsDao.class);
    }
  }

}
