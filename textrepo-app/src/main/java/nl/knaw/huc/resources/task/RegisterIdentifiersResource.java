package nl.knaw.huc.resources.task;

import io.swagger.annotations.Api;
import nl.knaw.huc.service.task.TaskBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Api(tags = {"task", "registerIdentifiers"})
@Path("task/register")
public class RegisterIdentifiersResource {
  private static final Logger log = LoggerFactory.getLogger(RegisterIdentifiersResource.class);

  private final TaskBuilderFactory factory;

  public RegisterIdentifiersResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @POST
  // This works for, e.g., "curl <host> --data-binary @file"
  // not that "curl -d" eats up whitespace and newlines
  public Response postIdentifiers(InputStream content) {
    registerIdentifiers(new InputStreamReader(content));
    return Response.ok().build();
  }

  @PUT
  // This works for, e.g., "curl <host> -T file" (aka "curl --upload")
  public Response putIdentifiers(InputStream content) {
    registerIdentifiers(new InputStreamReader(content));
    return Response.ok().build();
  }

  private void registerIdentifiers(InputStreamReader inputStreamReader) {
    new BufferedReader(inputStreamReader)
        .lines()
        .limit(25)
        .forEach(this::registerId);
  }

  private void registerId(String id) {
    if (id.length() > 100) {
      log.warn("skipping long id: [{}]", StringUtils.abbreviateMiddle(id, "...", 100));
      return;
    }
    log.info("registering id: [{}]", id);
  }
}
