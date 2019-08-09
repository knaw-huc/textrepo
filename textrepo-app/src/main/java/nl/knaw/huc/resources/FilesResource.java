package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.db.FileDAO;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
import static org.apache.commons.lang3.ArrayUtils.addAll;

@Path("/files")
@Produces(APPLICATION_JSON)
public class FilesResource {

  private final FileDAO dao;

  public FilesResource(Jdbi jdbi) {
    dao = jdbi.onDemand(FileDAO.class);
  }

  @POST
  @Timed
  @Consumes(MULTIPART_FORM_DATA)
  public void postFile(
    @FormDataParam("file") InputStream uploadedInputStream,
    @FormDataParam("file") FormDataContentDisposition fileDetail
  ) {
    try {
      var content = uploadedInputStream.readAllBytes();
      var name = fileDetail.getFileName();
      var sha1 = sha1Hex(addAll(content, name.getBytes()));
      dao.insert(sha1, name, content);
    } catch (IOException e) {
      throw new RuntimeException("Could not read input stream of posted file", e);
    }
  }

  @GET
  @Path("/{sha1}")
  @Timed
  @Produces(APPLICATION_OCTET_STREAM)
  public Response getFileBySha1(
    @PathParam("sha1") String sha1
  ) {
    var textRepoFile = dao.findBySha1(sha1);
    return Response
      .ok(textRepoFile.getContent(), APPLICATION_OCTET_STREAM)
      .header("Content-Disposition", "attachment; filename=\"" + textRepoFile.getName() + "\"")
      .build();
  }

}