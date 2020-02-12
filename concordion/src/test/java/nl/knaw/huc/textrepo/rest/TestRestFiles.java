package nl.knaw.huc.textrepo.rest;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import nl.knaw.huc.textrepo.util.TestUtils;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces = {"ext", "urn:concordion-extensions:2010"})
public class TestRestFiles extends AbstractConcordionTest {

  public String createDocument() {
    return RestUtils.createDocument("dummy-" + randomAlphabetic(5));
  }

  public static class CreateResult {
    public int status;
    public String body;
    public String validUuid;
    public String id;
  }

  public CreateResult create(Object endpoint, Object newEntity, Object docId, Object typeId) {
    var newEntityJson = newEntity
        .toString()
        .replace("{docId}", docId.toString())
        .replace("{typeId}", typeId.toString());

    final var response = client
        .target(HOST + endpoint.toString())
        .request()
        .post(entity(newEntityJson, APPLICATION_JSON_TYPE));

    var result = new CreateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.id = JsonPath.parse(body).read("$.id");
    result.validUuid = TestUtils.isValidUuidMsg(result.id);
    result.body = asPrettyJson(body);
    return result;
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String validUuid;
    public String correctType;
  }

  public RetrieveResult retrieve(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = JsonPath.parse(body);
    result.validUuid = TestUtils.isValidUuidMsg(json.read("$.id"));
    int resultTypeId = json.read("$.typeId");
    result.correctType = resultTypeId == textTypeId ? "correct type" : "" + resultTypeId + " != " + textTypeId;
    return result;
  }

  public static class UpdateResult {
    public int status;
    public String body;
    public String updatedType;
  }

  public UpdateResult update(Object endpoint, Object id, Object updatedEntity, Object docId, Object typeId) {
    updatedEntity = updatedEntity
        .toString()
        .replace("{docId}", docId.toString())
        .replace("{typeId}", typeId.toString());

    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .put(entity(updatedEntity.toString(), APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    var result = new UpdateResult();
    result.status = response.getStatus();
    result.body = asPrettyJson(body);
    var json = JsonPath.parse(body);
    int resultTypeId = json.read("$.typeId");
    result.updatedType = resultTypeId == fooTypeId ? "updated type" : "" + resultTypeId + " != " + typeId;
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
