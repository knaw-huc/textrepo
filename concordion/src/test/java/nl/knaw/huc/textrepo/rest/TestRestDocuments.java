package nl.knaw.huc.textrepo.rest;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.TestUtils;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;

public class TestRestDocuments extends AbstractConcordionTest {

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
    result.body = asPrettyJson(body);
    result.id = jsonPath.parse(body).read("$.id");
    result.validUuid = TestUtils.isValidUuidMsg(result.id);
    return result;
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String validUuid;
    public String externalId;
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
    var json = jsonPath.parse(body);
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
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.externalId = json.read("$.externalId");
    return result;
  }

  public static class DuplicateExternalIdErrorResult {
    public int status;
    public String body;
    public String msg;
  }

  public DuplicateExternalIdErrorResult tryDuplicate(Object endpoint, Object newEntity) {
    final var response = client
        .target(HOST + endpoint.toString())
        .request()
        .post(entity(newEntity.toString(), APPLICATION_JSON_TYPE));

    var result = new DuplicateExternalIdErrorResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var expectedErrorMsg = "Document not created: external ID [updated-test-external-id] already exists";
    result.msg = jsonPath.parse(body).read("$.message").equals(expectedErrorMsg)
        ? "error message"
        : "no error msg";
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
