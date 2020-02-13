package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;

@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces={"ext", "urn:concordion-extensions:2010"})
public class TestRestTypes extends AbstractConcordionTest {

  public static class CreateResult {
    public int status;
    public String body;
    public String hasId;
    public int id;
  }

  public CreateResult create(Object endpoint, Object newEntity) {
    final var response = client
        .target(HOST + endpoint.toString())
        .request()
        .post(entity(newEntity.toString(), APPLICATION_JSON_TYPE));

    var result = new CreateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    System.out.println("type status: " + result.status);
    System.out.println("type body: " + body);
    result.body = asPrettyJson(body);
    result.id = jsonPath.parse(body).read("$.id", Integer.class);
    result.hasId = result.id != 0 ? "valid ID" : "ID should not be 0";
    return result;
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String hasId;
    public String name;
    public String mimetype;
  }

  public RetrieveResult retrieve(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.hasId = json.read("$.id", Integer.class) != 0 ? "valid ID" : "ID should not be 0";
    result.name = json.read("$.name", String.class);
    result.mimetype = json.read("$.mimetype", String.class);
    return result;
  }

  public static class UpdateResult {
    public int status;
    public String body;
    public String hasId;
    public String name;
    public String mimetype;
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
    var json = jsonPath.parse(body);
    result.hasId = json.read("$.id", Integer.class) != 0 ? "valid ID" : "ID should not be 0";
    result.name = json.read("$.name", String.class);
    result.mimetype = json.read("$.mimetype", String.class);
    return result;
  }

  public RetrieveResult getAfterUpdate(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.hasId = json.read("$.id", Integer.class) != 0 ? "valid ID" : "ID should not be 0";
    result.name = json.read("$.name", String.class);
    result.mimetype = json.read("$.mimetype", String.class);
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

  public GetAfterDeleteResult getAfterDelete(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new GetAfterDeleteResult();
    result.status = response.getStatus();
    return result;
  }

}
