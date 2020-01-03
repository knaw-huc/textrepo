package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import org.concordion.api.BeforeSpecification;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.sql.DriverManager.getConnection;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static nl.knaw.huc.textrepo.Config.AUTOCOMPLETE_INDEX;
import static nl.knaw.huc.textrepo.Config.CUSTOM_INDEX;
import static nl.knaw.huc.textrepo.Config.FILE_INDEX;
import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.Config.HTTP_ES_HOST;
import static nl.knaw.huc.textrepo.Config.POSTGRES_DB;
import static nl.knaw.huc.textrepo.Config.POSTGRES_HOST;
import static nl.knaw.huc.textrepo.Config.POSTGRES_PASSWORD;
import static nl.knaw.huc.textrepo.Config.POSTGRES_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@FullOGNL
@RunWith(ConcordionRunner.class)
public abstract class AbstractConcordionTest {

  final Logger logger = LoggerFactory.getLogger(this.getClass());

  static Client client() {
    return JerseyClientBuilder.newClient();
  }

  private final List<String> indices = newArrayList(
      ES_HOST + "/" + FILE_INDEX,
      ES_HOST + "/" + CUSTOM_INDEX,
      ES_HOST + "/" + AUTOCOMPLETE_INDEX
  );


  final static String APP_HOST = HTTP_APP_HOST;
  final static String ES_HOST = HTTP_ES_HOST;

  private final static Configuration jsonPathConf = Configuration
      .defaultConfiguration()
      .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
      .addOptions(Option.SUPPRESS_EXCEPTIONS);

  /**
   * Do not throw an exception but return null when node doesn't exist
   */
  final static ParseContext jsonPath = JsonPath.using(jsonPathConf);

  @BeforeSpecification
  public void setUp() {
    emptyIndices();
    emptyTextrepoDatabase();
  }

  private void emptyIndices() {

    // wait for docs to be indexed:
    try {
      SECONDS.sleep(1);
    } catch (InterruptedException ex) {
      logger.error("Could not sleep", ex);
    }

    indices.forEach(this::emptyIndex);
  }

  private void emptyIndex(String index) {

    var indexExists = client()
        .target(index)
        .request()
        .get();

    if(indexExists.getStatus() == 404) {
      logger.info("Not clearing index [{}] because it does not (yet) exist", index);
      return;
    }

    logger.info("Clearing index [{}]", index);
    var delete = client()
        .target(index + "/_delete_by_query?conflicts=proceed")
        .request()
        .post(Entity.entity("{\"query\": {\"match_all\": {}}}", APPLICATION_JSON_TYPE));
    assertThat(delete.getStatus()).isEqualTo(200);

    // wait for docs to be deleted:
    try {
      SECONDS.sleep(1);
    } catch (InterruptedException ex) {
      logger.error("Could not sleep", ex);
    }

    this.checkNoDocs(index);
  }

  private void checkNoDocs(String index) {
    var countRequest = client()
        .target(index + "/_count")
        .request().get();
    var json = countRequest.readEntity(String.class);
    int count = jsonPath.parse(json).read("$.count");
    assertThat(count).isEqualTo(0);
  }

  private void emptyTextrepoDatabase() {
    logger.info("truncate tables owned by [{}]", POSTGRES_USER);

    var host = POSTGRES_HOST;
    var db = POSTGRES_DB;
    var user = POSTGRES_USER;
    var password = POSTGRES_PASSWORD;

    try (var connection = getConnection("jdbc:postgresql://" + host + "/" + db, user, password)) {
      var statement = connection.createStatement();
      statement.executeQuery("select truncate_tables_by_owner('" + POSTGRES_USER + "');");
    } catch (SQLException ex) {
      throw new RuntimeException("Could not truncate tables", ex);
    }
  }

}
