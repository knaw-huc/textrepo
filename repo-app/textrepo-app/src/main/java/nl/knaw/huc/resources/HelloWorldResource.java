package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.Saying;
import nl.knaw.huc.db.FileDAO;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
  private final String template;
  private final String defaultName;
  private final AtomicLong counter;
  private Jdbi jdbi;
  private final FileDAO dao;

  public HelloWorldResource(String template, String defaultName, Jdbi jdbi) {
    this.template = template;
    this.defaultName = defaultName;
    this.jdbi = jdbi;
    this.counter = new AtomicLong();
    dao = jdbi.onDemand(FileDAO.class);
  }

  @GET
  @Timed
  public Saying sayHello(@QueryParam("name") Optional<String> name) {
    final var nameString = String.format(template, name.orElse(defaultName));
    var saying = new Saying(counter.incrementAndGet(), nameString);
    var id = (int) saying.getId();
    dao.insert(id, nameString);
    return saying;
  }
}