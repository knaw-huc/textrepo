package nl.knaw.huc.service;

import nl.knaw.huc.db.TypeDao;
import org.jdbi.v3.core.Jdbi;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

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
  public short create(String name) {
    if (types().exists(name)) {
      throw new WebApplicationException("Duplicate type name: " + name, CONFLICT);
    }
    // Perhaps add exception mapper to generically catch this like so:
    // try {
    //   return types().create(name);
    // } catch (Exception e) {
    //   if (e.getCause() instanceof PSQLException) {
    //     final var cause = (PSQLException) e.getCause();
    //     final var sqlState = cause.getSQLState();
    //     LOGGER.debug("Exception during type creation, sqlState={}", sqlState);
    //     if (sqlState.equals("23505")) {
    //       throw new WebApplicationException("Duplicate type name: " + name, CONFLICT);
    //     }
    //   }
    //   throw new WebApplicationException(e.getMessage(), INTERNAL_SERVER_ERROR);
    // }

    final var id = types().create(name);
    LOGGER.trace("Type '{}' created with id: {}", name, id);

    return id;
  }

  @Override
  public short get(String name) {
    return types().find(name).orElseThrow(() -> new NotFoundException("No such type: " + name));
  }

  private TypeDao types() {
    return jdbi.onDemand(TypeDao.class);
  }
}
