package nl.knaw.huc.textrepo.rest;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import nl.knaw.huc.textrepo.util.TestUtils;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.time.Month;
import java.time.Year;

import static java.time.LocalDate.now;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces = {"ext", "urn:concordion-extensions:2010"})
public class TestRestVersions extends AbstractConcordionTest {

  public String createDocument() {
    return RestUtils.createDocument("dummy-" + randomAlphabetic(5));
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, textTypeId);
  }

  public static class CreateResult {
    public int status;
    public String body;
    public String validUuid;
    public String id;
  }

  public CreateResult create(Object endpoint, Object newEntity, String fileId) {
    var multiPart = new FormDataMultiPart()
        .field("fileId", fileId)
        .field("contents", newEntity, APPLICATION_OCTET_STREAM_TYPE);

    var request = client
        .register(MultiPartFeature.class)
        .target(HOST + endpoint)
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());

    var response = request.post(entity);

    var result = new CreateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.id = jsonPath.parse(body).read("$.id");
    result.validUuid = TestUtils.isValidUuidMsg(result.id);
    result.body = asPrettyJson(body);
    return result;
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String validUuid;
    public String validSha;
    public String validTimestamp;
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
    String resultSha = json.read("$.contentsSha");
    result.validSha = resultSha.length() == 56 ? "valid sha224" : resultSha + " is not valid";
    var yearValid = json.read("$.createdAt[0]", Integer.class).equals(Year.now().getValue());
    var monthValid = json.read("$.createdAt[1]", Integer.class).equals(Month.from(now()).getValue());
    var dayValid = json.read("$.createdAt[2]", Integer.class).equals(now().getDayOfMonth());
    result.validTimestamp = yearValid && monthValid && dayValid
        ? "valid timestamp"
        : json.read("$.createdAt", String.class) + " is not valid";
    return result;
  }

  public static class UpdateResult {
    public int status;
    public String body;
  }

  public UpdateResult update(Object endpoint, Object updatedEntity, String id) {

    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .put(entity(updatedEntity.toString(), APPLICATION_JSON_TYPE));

    var body = response.readEntity(String.class);
    var result = new UpdateResult();
    result.status = response.getStatus();
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
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
