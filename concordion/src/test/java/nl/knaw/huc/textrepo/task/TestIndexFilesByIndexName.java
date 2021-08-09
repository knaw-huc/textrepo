package nl.knaw.huc.textrepo.task;

import nl.knaw.huc.textrepo.AbstractConcordionTest;
import nl.knaw.huc.textrepo.util.IndexUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huc.textrepo.Config.AUTOCOMPLETE_INDEX;
import static nl.knaw.huc.textrepo.Config.FULL_TEXT_INDEX;
import static nl.knaw.huc.textrepo.Config.HOST;
import static nl.knaw.huc.textrepo.util.IndexUtils.indexToUrl;
import static nl.knaw.huc.textrepo.util.TestUtils.asCodeBlock;
import static nl.knaw.huc.textrepo.util.TestUtils.asPrettyJson;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class TestIndexFilesByIndexName extends AbstractConcordionTest {

  private static final Logger log = LoggerFactory.getLogger(IndexUtils.class);


  public static class ImportResult {
    public String status;
    public String body;
  }

  /**
   * Use /task/import-endpoint which does not index
   */
  public ImportResult importDocs(String fileType) {
    var results = new ArrayList<Integer>();

    var response = importDocument(
        "test-" + randomAlphanumeric(10),
        "beunhaas".getBytes(),
        fileType
    );
    results.add(response.getStatus());
    var body = response.readEntity(String.class);

    results.add(importDocument(
        "test-" + randomAlphanumeric(10),
        "dakhaas".getBytes(),
        fileType
    ).getStatus());

    results.add(importDocument(
        "test-" + randomAlphanumeric(10),
        "zandhaas".getBytes(),
        fileType
    ).getStatus());

    var result = new ImportResult();
    result.status = results.stream().map(String::valueOf).collect(Collectors.joining(", "));
    result.body = asPrettyJson(body.equals("") ? " " : body);
    return result;
  }

  private Response importDocument(String externalId, byte[] content, String type) {
    var contentDisposition = FormDataContentDisposition
        .name("contents")
        .fileName(externalId + ".txt")
        .size(content.length)
        .build();

    final var multiPart = new FormDataMultiPart()
        .bodyPart(new FormDataBodyPart(
            contentDisposition,
            new String(content, UTF_8),
            APPLICATION_OCTET_STREAM_TYPE)
        );

    var importUrl = HOST + "/task/import/documents/" + externalId + "/" + type
        + "?allowNewDocument=true"
        + "&index=false";
    var request = client
        .register(MultiPartFeature.class)
        .target(importUrl)
        .request();

    var entity = entity(multiPart, multiPart.getMediaType());
    return request.post(entity);

  }

  public static class IndexResult {
    public int status;
    public String body;
  }

  public IndexResult indexFilesBy(String indexEndpoint, String indexName) {
    var response = client
        .target(HOST + indexEndpoint.replace("{name}", indexName))
        .request()
        .post(entity("", APPLICATION_JSON));

    var result = new IndexResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asCodeBlock(body.equals("") ? " " : body);

    return result;
  }

  public static class SearchResult {
    public int status;
    public int count;
    public String contents;
    public String body;
  }

  public SearchResult searchAutocompleteIndex() throws InterruptedException {
    SECONDS.sleep(2);
    var indexUrl = indexToUrl(AUTOCOMPLETE_INDEX);
    var searchIndexUrl = indexUrl + "/_search";
    var response = client.target(searchIndexUrl).request().get();
    var result = new SearchResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    result.count = jsonPath.parse(body).read("$.hits.total.value", Integer.class);
    var contents = jsonPath.parse(body).read("$.hits.hits[*]._source.suggest[0].input", String[].class);
    Arrays.sort(contents);
    result.contents = String.join(", ", contents);
    return result;
  }

  public SearchResult searchFullTextIndex() {
    var indexUrl = indexToUrl(FULL_TEXT_INDEX);
    var searchIndexUrl = indexUrl + "/_search";
    var response = client.target(searchIndexUrl).request().get();
    var result = new SearchResult();
    result.status = response.getStatus();
    var body = response.readEntity(String.class);
    result.body = asPrettyJson(body);
    result.count = jsonPath.parse(body).read("$.hits.total.value", Integer.class);
    return result;
  }
}
