package nl.knaw.huc.service.task;

import nl.knaw.huc.core.TextrepoFile;
import nl.knaw.huc.db.DocumentFilesDao;
import nl.knaw.huc.db.TypesDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FindFileByDocumentIdAndFileType implements InTransactionProvider<TextrepoFile> {
  private final UUID docId;
  private final String typeName;
  private Handle transaction;

  public FindFileByDocumentIdAndFileType(UUID docId, String typeName) {
    this.docId = docId;
    this.typeName = typeName;
  }

  @Override
  public TextrepoFile executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);
    var type = types().find(this.typeName).orElseThrow(illegalType(this.typeName));
    return documentFiles().findFile(docId, type).orElseThrow(this::noFileForDocAndType);
  }

  private Supplier<NotFoundException> illegalType(String name) {
    return () -> new NotFoundException(format("Illegal type: %s", name));
  }

  private NotFoundException noFileForDocAndType() {
    return new NotFoundException(format("No file for document %s and type %s", this.docId, this.typeName));
  }

  private TypesDao types() {
    return transaction.attach(TypesDao.class);
  }

  private DocumentFilesDao documentFiles() {
    return transaction.attach(DocumentFilesDao.class);
  }
}
