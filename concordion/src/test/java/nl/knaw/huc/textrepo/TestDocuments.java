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
    public String docId;
  }

  public CreateResult create(Object endpoint, Object newEntity) {
    final var response = client
        .register(MultiPartFeature.class)
        .target(HOST + endpoint.toString())
        .request()
        .post(entity(newEntity.toString(), APPLICATION_JSON_TYPE));

    var result = new CreateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.docId = JsonPath.parse(body).read("$.id");
    result.validUuid = TestUtils.isValidUuidMsg(result.docId);
    result.body = asPrettyJson(body);
    return result;
  }

  public static class ReadResult {
    public int status;
    public String body;
    public String validUuid;
    public String externalId;
  }

  public ReadResult read(Object endpoint, Object docId) {
    final var response = client
        .register(MultiPartFeature.class)
        .target(replaceUrlParams(endpoint, docId.toString()))
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

  private URI replaceUrlParams(Object endpoint, String param) {
    return UriBuilder.fromPath(HOST + endpoint.toString()).build(param);
  }

  public CreateResult update(Object endpoint, Object id, Object newEntity) {
    return new CreateResult();
  }

  public CreateResult delete(Object endpoint, Object id) {
    return new CreateResult();
  }

}
