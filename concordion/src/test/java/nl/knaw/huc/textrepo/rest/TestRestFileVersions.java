package nl.knaw.huc.textrepo.rest;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;

import javax.ws.rs.core.UriBuilder;

import static javax.ws.rs.client.Entity.entity;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static nl.knaw.huc.textrepo.util.TestUtils.replaceUrlParams;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces = {"ext", "urn:concordion-extensions:2010"})
public class TestRestFileVersions extends AbstractConcordionTest {

  public String createDocument() {
    return RestUtils.createDocument("dummy-" + randomAlphabetic(5));
  }

  public String createFile(String docId) {
    return RestUtils.createFile(docId, textTypeId);
  }

  public String createVersion(String fileId) {
    return RestUtils.createVersion(fileId, "random-content-" + randomAlphabetic(10));
  }

  public static class RetrieveResult {
    public int status;
    public String body;
    public String twoVersions;
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
    var length = json.read("$.items.length()", Integer.class);
    result.twoVersions = "" + length;
    return result;
  }

  public static class PaginateResult {
    public int status;
    public String body;
    public int itemCount;
    public String externalDocumentId;
    public int total;
  }

  public PaginateResult paginate(Object endpoint, String fileId, String offset, String limit) {
    var url = UriBuilder
        .fromPath(replaceUrlParams(endpoint, fileId).toString())
        .queryParam("offset", offset)
        .queryParam("limit", limit)
        .build();
    final var response = client
        .target(url)
        .request()
        .get();

    var result = new PaginateResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = jsonPath.parse(body);
    result.itemCount = json.read("$.items.length()");
    result.externalDocumentId = json.read("$.items[0].externalId");
    result.total = json.read("$.total", Integer.class);
    return result;
  }


}
