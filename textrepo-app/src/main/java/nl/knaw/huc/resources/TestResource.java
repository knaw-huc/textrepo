package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.FileDAO;
import nl.knaw.huc.db.MetadataDAO;
import nl.knaw.huc.db.VersionDAO;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

// TODO: remove when dao's are in use
@Path("/test")
public class TestResource {

  private Jdbi jdbi;
  private MetadataDAO metadataDAO;
  private VersionDAO versionDAO;
  private FileDAO fileDAO;

  public TestResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @GET
  @Timed
  @Produces(APPLICATION_JSON)
  public Response testDaos() {
    var sha = "aee1d4cff44a25fe972949e1cc299140691e4c0a243765c4ffafb802";
    var documentUuid = UUID.randomUUID();
    var metadataKey = "testkey";
    var versionDate = now();

    var file = new TextRepoFile(
      sha,
      "hello test".getBytes()
    );
    getFileDAO().insert(file);

    var metadata = new MetadataEntry(
      documentUuid,
      metadataKey,
      "testvalue"
    );
    getMetadataDAO().insert(metadata);

    var version = new Version(
      documentUuid,
      versionDate,
      sha
    );
    getVersionDAO().insert(version);

    return Response
      .ok(new Object() {
        public MetadataEntry newMetadata = metadataDAO.findByDocumentUuidAndKey(documentUuid, metadataKey);
        public Version newVersion = versionDAO.findByDocumentUuidAndDate(documentUuid, versionDate);
      })
      .header("Content-Disposition", "attachment;")
      .build();
  }

  private FileDAO getFileDAO() {
    if (fileDAO == null) {
      var found = jdbi.onDemand(FileDAO.class);
      if (found == null) {
        throw new RuntimeException("No FileDAO handle could be opened by jdbi");
      }
      fileDAO = found;
    }
    return fileDAO;
  }

  private MetadataDAO getMetadataDAO() {
    if (metadataDAO == null) {
      var found = jdbi.onDemand(MetadataDAO.class);
      if (found == null) {
        throw new RuntimeException("No MetadataDAO handle could be opened by jdbi");
      }
      metadataDAO = found;
    }
    return metadataDAO;
  }

  private VersionDAO getVersionDAO() {
    if (versionDAO == null) {
      var found = jdbi.onDemand(VersionDAO.class);
      if (found == null) {
        throw new RuntimeException("No VersionDAO handle could be opened by jdbi");
      }
      versionDAO = found;
    }
    return versionDAO;
  }

}