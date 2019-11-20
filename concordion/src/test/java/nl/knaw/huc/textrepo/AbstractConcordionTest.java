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

import javax.ws.rs.client.Client;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.DriverManager.getConnection;
import static nl.knaw.huc.textrepo.Config.CUSTOM_INDEX;
import static nl.knaw.huc.textrepo.Config.DOCUMENT_INDEX;
import static nl.knaw.huc.textrepo.Config.HTTP_APP_HOST;
import static nl.knaw.huc.textrepo.Config.HTTP_ES_HOST;
import static nl.knaw.huc.textrepo.Config.POSTGRES_DB;
import static nl.knaw.huc.textrepo.Config.POSTGRES_HOST;
import static nl.knaw.huc.textrepo.Config.POSTGRES_PASSWORD;
import static nl.knaw.huc.textrepo.Config.POSTGRES_USER;
import static org.junit.Assert.assertEquals;

@FullOGNL
@RunWith(ConcordionRunner.class)
public abstract class AbstractConcordionTest {

  static Client client() {
    return JerseyClientBuilder.newClient();
  }

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
    deleteIndices();
    emptyTextrepoDatabase();
  }

  private void deleteIndices() {
    var indices = new ArrayList<String>();
    indices.add(ES_HOST + "/" + DOCUMENT_INDEX);
    indices.add(ES_HOST + "/" + CUSTOM_INDEX);
    indices.forEach(this::deleteIndex);
  }

  private void deleteIndex(String index) {
    System.out.printf("deleting index [%s]\n", index);
    var get = client()
        .target(index)
        .request().get();
    if (get.getStatus() == 404) {
      return;
    }

    var delete = client()
        .target(index)
        .request().delete();
    assertEquals(delete.getStatus(), 200);

    get = client()
        .target(index)
        .request().get();
    assertEquals(get.getStatus(), 404);
  }

  private void emptyTextrepoDatabase() {
    var host = POSTGRES_HOST;
    var db = POSTGRES_DB;
    var user = POSTGRES_USER;
    var password = POSTGRES_PASSWORD;

    try (var connection = getConnection("jdbc:postgresql://" + host + "/" + db, user, password)) {
      var statement = connection.createStatement();
      statement.executeQuery("select truncate_tables_by_owner('" + POSTGRES_USER + "');");
    } catch (SQLException ex) {
      throw new RuntimeException("Could not empty postgres tables", ex);
    }
  }

}
