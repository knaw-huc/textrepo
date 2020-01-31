package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.util.TestUtils;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;

@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces={"ext", "urn:concordion-extensions:2010"})
public class TestDocuments extends AbstractConcordionTest {

  public static class CreateResult {
    public int status;
    public String body;
    public String validUuid;
    public String id;
  }

  public CreateResult create(Object endpoint, Object newEntity) {
    final var response = client
        .target(HOST + endpoint.toString())
        .request()
        .post(entity(newEntity.toString(), APPLICATION_JSON_TYPE));

    var result = new CreateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.id = JsonPath.parse(body).read("$.id");
    result.validUuid = TestUtils.isValidUuidMsg(result.id);
    result.body = asPrettyJson(body);
    return result;
  }

  public static class ReadResult {
    public int status;
    public String body;
    public String validUuid;
    public String externalId;
  }

  public ReadResult read(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new ReadResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = JsonPath.parse(body);
    result.validUuid = TestUtils.isValidUuidMsg(json.read("$.id"));
    result.externalId = json.read("$.externalId");
    return result;
  }

  public static class UpdateResult {
    public int status;
    public String body;
    public String externalId;
  }

  public UpdateResult update(Object endpoint, Object id, Object updatedEntity) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .put(entity(updatedEntity.toString(), APPLICATION_JSON_TYPE));

    var result = new UpdateResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = JsonPath.parse(body);
    result.externalId = json.read("$.externalId");
    return result;
  }

  public static class DeleteResult {
    public int status;
  }

  public DeleteResult delete(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .delete();

    var result = new DeleteResult();
    result.status = response.getStatus();
    return result;
  }

  public static class GetAfterDeleteResult {
    public int status;
  }

  public GetAfterDeleteResult getAfterDelele(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new GetAfterDeleteResult();
    result.status = response.getStatus();
    return result;
  }



  private URI replaceUrlParams(Object endpoint, Object... params) {
    return UriBuilder.fromPath(HOST + endpoint.toString()).build(params);
  }

}
