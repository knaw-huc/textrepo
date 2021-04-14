package nl.knaw.huc.service.task;

import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.TypesDao;
import org.jdbi.v3.core.Handle;

import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FindType implements InTransactionProvider<Type> {
  private final String typeName;

  private Handle transaction;

  public FindType(String typeName) {
    this.typeName = requireNonNull(typeName);
  }

  @Override
  public Type executeIn(Handle transaction) {
    this.transaction = requireNonNull(transaction);

    return types().getByName(typeName)
                  .orElseThrow(illegalType(typeName));
  }

  private Supplier<NotFoundException> illegalType(String name) {
    return () -> new NotFoundException(format("Illegal type: %s", name));
  }

  private TypesDao types() {
    return transaction.attach(TypesDao.class);
  }

}
