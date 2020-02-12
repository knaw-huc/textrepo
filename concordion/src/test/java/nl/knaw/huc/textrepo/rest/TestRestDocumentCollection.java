package nl.knaw.huc.textrepo.rest;

import com.jayway.jsonpath.JsonPath;
import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.RestUtils;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;

import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;

@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces={"ext", "urn:concordion-extensions:2010"})
public class TestRestDocumentCollection extends AbstractConcordionTest {

  public void createDocument(String externalId) {
    RestUtils.createDocument(externalId);
  }

  public static class SearchResult {
    public int status;
    public String body;
    public int documentCount;
    public String externalId;
  }

  public SearchResult search(Object endpoint, String queryParam, String queryParamValue) {
    var url = UriBuilder
        .fromPath(HOST + endpoint.toString())
        .queryParam(queryParam, queryParamValue)
        .build();
    final var response = client
        .target(url)
        .request()
        .get();

    var result = new SearchResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = JsonPath.parse(body);
    result.documentCount = json.read("$.length()");
    result.externalId = json.read("$[0].externalId");
    return result;
  }

  public static class SearchMultipleResult {
    public int status;
    public String body;
    public int documentCount;
    public String externalIds;
  }

  public SearchMultipleResult searchMultiple(Object endpoint, String queryParam, String queryParamValue) {
    var url = UriBuilder
        .fromPath(HOST + endpoint.toString())
        .queryParam(queryParam, queryParamValue)
        .build();
    final var response = client
        .target(url)
        .request()
        .get();

    var result = new SearchMultipleResult();
    result.status = response.getStatus();
    var  body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    var json = JsonPath.parse(body);
    result.documentCount = json.read("$.length()");
    List<String> externalIds = newArrayList(
        json.read("$[0].externalId"),
        json.read("$[1].externalId")
    );
    var expectedExternalIds = newArrayList("first-external-id", "second-external-id");
    result.externalIds = externalIds.containsAll(expectedExternalIds) ? "first and second" : "ids missing";
    return result;
  }

}
