package nl.knaw.huc.textrepo.rest;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces = {"ext", "urn:concordion-extensions:2010"})
public class TestRestDocumentMetadata extends AbstractConcordionTest {

  public String createDocument() {
    return RestUtils.createDocument("dummy-" + randomAlphabetic(5));
  }

  public static class CreateResult {
    public int status;
    public String body;
  }

  public CreateResult create(Object endpoint, Object id, Object key, Object value) {
    final var response = client
        .target(replaceUrlParams(endpoint, id, key))
        .request()
        .put(entity(value.toString(), APPLICATION_JSON_TYPE));

    var result = new CreateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    return result;
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String value;
  }

  public RetrieveResult retrieve(Object endpoint, Object id, Object key) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new RetrieveResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.value = json.read("$." + key.toString());
    return result;
  }

  public static class UpdateResult {
    public int status;
    public String body;
    public String value;
  }

  public UpdateResult update(Object endpoint, Object docId, Object metadataKey, Object updatedMetadataValue) {
    final var response = client
        .target(replaceUrlParams(endpoint, docId, metadataKey))
        .request()
        .put(entity(updatedMetadataValue.toString(), APPLICATION_JSON_TYPE));

    var result = new UpdateResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.value = json.read("$.value");
    return result;
  }

  public static class DeleteResult {
    public int status;
  }

  public DeleteResult delete(Object endpoint, Object docId, Object key) {
    final var response = client
        .target(replaceUrlParams(endpoint, docId, key))
        .request()
        .delete();

    var result = new DeleteResult();
    result.status = response.getStatus();
    return result;
  }

  public static class RetrieveAfterDeleteResult {
    public int status;
    public String body;
  }

  public RetrieveAfterDeleteResult retrieveAfterDelete(Object endpoint, Object id) {
    final var response = client
        .target(replaceUrlParams(endpoint, id))
        .request()
        .get();

    var result = new RetrieveAfterDeleteResult();
    result.status = response.getStatus();
    result.body = response.readEntity(String.class);
    return result;
  }

}
