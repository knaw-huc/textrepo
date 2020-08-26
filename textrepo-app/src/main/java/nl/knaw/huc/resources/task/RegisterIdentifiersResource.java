package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(tags = {"task", "register"})
@Path("task/register")
@Produces(APPLICATION_JSON)
public class RegisterIdentifiersResource {
  private static final Logger log = LoggerFactory.getLogger(RegisterIdentifiersResource.class);

  private final TaskBuilderFactory factory;

  public RegisterIdentifiersResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @POST
  // This works for, e.g., "curl <host> --data-binary @file"
  // note that "curl -d" eats up whitespace and newlines
  public List<Document> postIdentifiers(InputStream content) {
    return registerIdentifiers(content);
  }

  @PUT
  // This works for, e.g., "curl <host> -T file" (aka "curl --upload")
  public List<Document> putIdentifiers(InputStream content) {
    return registerIdentifiers(content);
  }

  private List<Document> registerIdentifiers(InputStream inputStream) {
    final var externalIds = new BufferedReader(new InputStreamReader(inputStream)).lines();

    final var registerIdentifiersTask = factory
        .getRegisterIdentifiersBuilder()
        .forExternalIdentifiers(externalIds)
        .build();

    final var docs = registerIdentifiersTask.run();
    log.debug("Registered {} identifiers", docs.size());
    return docs;
  }
}
