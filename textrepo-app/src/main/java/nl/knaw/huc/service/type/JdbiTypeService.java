package nl.knaw.huc.service.type;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.Constraint.FILES_TYPE_ID;
import static nl.knaw.huc.helpers.PsqlExceptionHelper.violatesConstraint;

import java.util.List;
import javax.annotation.Nonnull;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.TypesDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbiTypeService implements TypeService {
  private static final Logger log = LoggerFactory.getLogger(JdbiTypeService.class);

  private final Jdbi jdbi;

  public JdbiTypeService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public List<Type> list() {
    return types().list();
  }

  @Override
  public Type create(@Nonnull Type type) {
    throwWhenNameExists(type);
    type.setId(types().create(type));
    log.trace("Created type {}", type);
    return type;
  }

  @Override
  public short getId(@Nonnull String name) {
    return types().findByName(name)
                  .orElseThrow(() -> new NotFoundException("No such type: " + name));
  }

  @Override
  public Type getType(Short typeId) {
    return types()
        .getById(typeId)
        .orElseThrow(() -> new NotFoundException(format(
            "Could not find type with id %s",
            typeId
        )));
  }

  @Override
  public Type upsert(Type type) {
    throwWhenNameExists(type);
    types().upsert(type);
    log.trace("Upserted type {}", type);
    return type;
  }

  @Override
  public void delete(Short typeId) {
    try {
      types().delete(typeId);
    } catch (JdbiException ex) {
      if (violatesConstraint(ex, FILES_TYPE_ID)) {
        throw new ForbiddenException(
            format("Cannot delete type: files of type %s still exist", typeId));
      }
      throw ex;
    }
    log.trace("Deleted type with id {}", typeId);
  }

  private TypesDao types() {
    return jdbi.onDemand(TypesDao.class);
  }

  private void throwWhenNameExists(@Nonnull Type type) {
    final var existingId = types().findByName(type.getName());
    if (existingId.isPresent() && existingId.get() != type.getId()) {
      throw new WebApplicationException("Duplicate type name: " + type.getName(), CONFLICT);
    }
  }
}
