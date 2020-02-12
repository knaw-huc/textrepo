package nl.knaw.huc.resources.task;

import nl.knaw.huc.core.Version;
import nl.knaw.huc.service.task.TaskBuilderFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/task/find")
public class FindResource {
  private final TaskBuilderFactory factory;

  public FindResource(TaskBuilderFactory factory) {
    this.factory = factory;
  }

  @GET
  @Path("/latest/{uuid}/version")
  @Produces(APPLICATION_JSON)
  public Version findLatestVersion(@PathParam("uuid") @NotNull @Valid UUID fileId) {
    final var task = factory.getFileFinderBuilder()
                            .forFile(fileId)
                            .build();
    return task.run();
  }
}
