package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import org.concordion.api.AfterSpecification;
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

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;
import static java.sql.DriverManager.getConnection;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static nl.knaw.huc.textrepo.Config.FILE_TYPE;
import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.Config.HTTP_ES_HOST;
import static nl.knaw.huc.textrepo.Config.MIMETYPE;
import static nl.knaw.huc.textrepo.Config.POSTGRES_DB;
import static nl.knaw.huc.textrepo.Config.POSTGRES_HOST;
import static nl.knaw.huc.textrepo.Config.POSTGRES_PASSWORD;
import static nl.knaw.huc.textrepo.Config.POSTGRES_USER;
import static nl.knaw.huc.textrepo.Config.TYPES_URL;
import static nl.knaw.huc.textrepo.util.IndexUtils.emptyIndices;

@FullOGNL
@RunWith(ConcordionRunner.class)
public abstract class AbstractConcordionTest {

  final Logger logger = LoggerFactory.getLogger(this.getClass());
  final static Client client = JerseyClientBuilder.newClient();

  static Client client() {
    return client;
  }

  final static String APP_HOST = HTTP_APP_HOST;
  final static String ES_HOST = HTTP_ES_HOST;

  private final static Configuration jsonPathConf = Configuration
      .defaultConfiguration()
      .addOptions(DEFAULT_PATH_LEAF_TO_NULL)
      .addOptions(SUPPRESS_EXCEPTIONS);

  /**
   * Do not throw an exception but return null when node doesn't exist
   */
  final static ParseContext jsonPath = JsonPath.using(jsonPathConf);

  @BeforeSpecification
  public void init() {
    initTypes();
  }

  @AfterSpecification
  public void cleanUp() {
    emptyIndices();
    emptyTextrepoDatabase();
  }

  private void initTypes() {
    client
        .target(TYPES_URL)
        .request()
        .post(json("{" +
            "\"name\": \"" + FILE_TYPE + "\"," +
            "\"mimetype\": \"" + MIMETYPE + "\"" +
        "}"));
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
