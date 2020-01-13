package nl.knaw.huc.service;

import nl.knaw.huc.core.Type;
import nl.knaw.huc.db.TypeDao;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.CONFLICT;

public class JdbiTypeService implements TypeService {
  private static final Logger LOGGER = LoggerFactory.getLogger(JdbiTypeService.class);

  private final Jdbi jdbi;

  public JdbiTypeService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public List<String> list() {
    return types().list();
  }

  @Override
  public short create(@Nonnull Type type) {
    if (types().exists(type.getName())) {
      throw new WebApplicationException("Duplicate type name: " + type.getName(), CONFLICT);
    }
    final var id = types().create(type);
    LOGGER.trace("Type '{}' created with id: {}", type.getName(), id);
    return id;
  }

  @Override
  public short getId(@Nonnull String name) {
    return types().find(name).orElseThrow(() -> new NotFoundException("No such type: " + name));
  }

  @Override
  public Type getType(Short typeId) {
    return types()
        .get(typeId)
        .orElseThrow(() -> new RuntimeException(format(
            "Could not find type for type id [%s]",
            typeId
        )));
  }

  private TypeDao types() {
    return jdbi.onDemand(TypeDao.class);
  }
}
