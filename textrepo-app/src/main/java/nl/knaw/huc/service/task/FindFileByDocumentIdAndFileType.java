package nl.knaw.huc.service.task;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.UUID;
import java.util.function.Supplier;
import javax.ws.rs.NotFoundException;
import nl.knaw.huc.core.TextRepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.TypesDao;
import org.jdbi.v3.core.Handle;

public class FindFileByDocumentIdAndFileType implements InTransactionProvider<TextRepoFile> {
  private final UUID docId;
  private final String typeName;
  private Handle transaction;

  public FindFileByDocumentIdAndFileType(UUID docId, String typeName) {
    this.docId = docId;
    this.typeName = typeName;
  }

  @Override
  public TextRepoFile executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    var type = types().findByName(this.typeName).orElseThrow(illegalType(this.typeName));
    return documentFiles().findFile(docId, type).orElseThrow(this::noFileForDocAndType);
  }

  private Supplier<NotFoundException> illegalType(String name) {
    return () -> new NotFoundException(format("Illegal type: %s", name));
  }

  private NotFoundException noFileForDocAndType() {
    return new NotFoundException(
        format("No file for document %s and type %s", this.docId, this.typeName));
  }

  private TypesDao types() {
    return transaction.attach(TypesDao.class);
  }

  private DocumentFilesDao documentFiles() {
    return transaction.attach(DocumentFilesDao.class);
  }
}
