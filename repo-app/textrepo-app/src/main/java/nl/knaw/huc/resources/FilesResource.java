package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.db.FileDAO;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/files")
@Produces(APPLICATION_JSON)
public class FilesResource {
  private final AtomicInteger counter;
  private final FileDAO dao;

  public FilesResource(Jdbi jdbi) {
    this.counter = new AtomicInteger();
    dao = jdbi.onDemand(FileDAO.class);
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  public void postFile(
    @FormDataParam("file") InputStream uploadedInputStream,
    @FormDataParam("file") FormDataContentDisposition fileDetail
  ) {
    var id = counter.incrementAndGet();
    try {
      dao.insert(id, fileDetail.getFileName(), uploadedInputStream.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException("Could not read input stream of posted file");
    }
  }
}