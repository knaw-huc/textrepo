package nl.knaw.huc.resources;

import com.codahale.metrics.annotation.Timed;
import nl.knaw.huc.api.MetadataEntry;
import nl.knaw.huc.api.TextRepoFile;
import nl.knaw.huc.api.Version;
import nl.knaw.huc.db.FileDao;
import nl.knaw.huc.db.MetadataDao;
import nl.knaw.huc.db.VersionDao;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

// TODO: remove when dao's are in use
@Path("/test")
public class TestResource {

  private Jdbi jdbi;

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
    getFileDao().insert(file);

    var metadata = new MetadataEntry(
      documentUuid,
      metadataKey,
      "testvalue"
    );
    getMetadataDao().insert(metadata);

    var version = new Version(
      documentUuid,
      versionDate,
      sha
    );
    getVersionDao().insert(version);

    return Response
      .ok(new Object() {
        public MetadataEntry newMetadata = getMetadataDao().findByDocumentUuidAndKey(documentUuid, metadataKey);
        public Version newVersion = getVersionDao().findByDocumentUuidAndDate(documentUuid, versionDate);
      })
      .build();
  }

  private FileDao getFileDao() {
    return jdbi.onDemand(FileDao.class);
  }

  private MetadataDao getMetadataDao() {
    return jdbi.onDemand(MetadataDao.class);
  }

  private VersionDao getVersionDao() {
    return jdbi.onDemand(VersionDao.class);
  }

}
