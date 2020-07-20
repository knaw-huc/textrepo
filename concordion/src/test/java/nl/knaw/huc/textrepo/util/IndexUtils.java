package nl.knaw.huc.textrepo.util;

import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.HTTP_ES_HOST;
import static nl.knaw.huc.textrepo.Config.INDICES;
import static nl.knaw.huc.textrepo.util.TestUtils.getByUrl;
import static nl.knaw.huc.textrepo.util.TestUtils.sleepMs;
import static org.assertj.core.api.Assertions.assertThat;

public class IndexUtils {

  private final static Client client = JerseyClientBuilder.newClient();
  private static final Logger log = LoggerFactory.getLogger(IndexUtils.class);

  public static void emptyIndices() {
    INDICES.forEach(IndexUtils::emptyIndex);
  }

  private static void emptyIndex(String index) {
    var indexExists = client
        .target(indexToUrl(index).toString())
        .request()
        .get();

    if (indexExists.getStatus() == 404) {
      log.info("Not clearing index [{}] because it does not exist", index);
      return;
    }

    log.info("Clearing index [{}]", index);

    refreshIndex(client, index);
    var delete = client
        .target(indexToUrl(index) + "/_delete_by_query")
        .request()
        .post(Entity.entity("{\"query\": {\"match_all\": {}}}", APPLICATION_JSON_TYPE));
    assertThat(delete.getStatus()).isEqualTo(200);

    refreshIndex(client, index);
    checkNoDocs(index);
  }

  private static void checkNoDocs(String index) {
    var countRequest = client
        .target(indexToUrl(index) + "/_count")
        .request().get();
    var json = countRequest.readEntity(String.class);
    int count = JsonPath.parse(json).read("$.count");
    assertThat(count).isEqualTo(0);
  }

  public static URL indexToUrl(String index) {
    try {
      return new URL(HTTP_ES_HOST + "/" + index);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(format("Could not create url from index [%s]", index));
    }
  }

  public static void refreshIndex(Client client, String index) {
    var uri = indexToUrl(index) + "/_refresh";
    var refreshRequest = client
        .register(MultiPartFeature.class)
        .target(uri)
        .request()
        .post(entity("", APPLICATION_JSON_TYPE));
    assertThat(refreshRequest.getStatus()).isEqualTo(200);
    // wait a bit until refreshed:
    sleepMs(100);
  }

}
