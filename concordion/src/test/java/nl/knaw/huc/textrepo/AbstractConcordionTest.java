package nl.knaw.huc.textrepo;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import org.concordion.api.AfterSpecification;
import org.concordion.api.BeforeSpecification;
import org.concordion.api.FullOGNL;
import org.concordion.api.extension.Extensions;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.ext.EmbedExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;
import static java.sql.DriverManager.getConnection;
import static nl.knaw.huc.textrepo.Config.FOO_MIMETYPE;
import static nl.knaw.huc.textrepo.Config.FOO_TYPE;
import static nl.knaw.huc.textrepo.Config.POSTGRES_DB;
import static nl.knaw.huc.textrepo.Config.POSTGRES_HOST;
import static nl.knaw.huc.textrepo.Config.POSTGRES_PASSWORD;
import static nl.knaw.huc.textrepo.Config.POSTGRES_USER;
import static nl.knaw.huc.textrepo.Config.TEXT_MIMETYPE;
import static nl.knaw.huc.textrepo.Config.TEXT_TYPE;
import static nl.knaw.huc.textrepo.util.IndexUtils.emptyIndices;
import static nl.knaw.huc.textrepo.util.RestUtils.createType;

@FullOGNL
@RunWith(ConcordionRunner.class)
@Extensions(EmbedExtension.class)
@ConcordionOptions(declareNamespaces = {"ext", "urn:concordion-extensions:2010"})
public abstract class AbstractConcordionTest {

  final Logger log = LoggerFactory.getLogger(this.getClass());
  protected final static Client client = JerseyClientBuilder.newClient();
  protected static int textTypeId;
  protected static int fooTypeId;

  protected static Client client() {
    return client;
  }

  private final static Configuration jsonPathConf = Configuration
      .defaultConfiguration()
      .addOptions(DEFAULT_PATH_LEAF_TO_NULL)
      .addOptions(SUPPRESS_EXCEPTIONS);

  /**
   * Do not throw an exception but return null when node doesn't exist:
   */
  protected final static ParseContext jsonPath = JsonPath.using(jsonPathConf);

  @BeforeSpecification
  public void init() {
    initTypes();
  }

  @AfterSpecification
  public void cleanUp() {
    emptyTextRepoDatabase();
    emptyIndices();
  }

  private void initTypes() {
    textTypeId = createType(TEXT_TYPE, TEXT_MIMETYPE);
    fooTypeId = createType(FOO_TYPE, FOO_MIMETYPE);
  }

  private void emptyTextRepoDatabase() {
    log.info("truncate tables owned by [{}]", POSTGRES_USER);

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

  protected static String readableStatus(Response response) {
    return String.format("%d %s", response.getStatus(), response.getStatusInfo());
  }

  public int getTextTypeId() {
    return textTypeId;
  }

  public int getFooTypeId() {
    return fooTypeId;
  }

  public String code(String string) {
    return "<code>" + string + "</code>";
  }

  public String code(int number) {
    return code("" + number);
  }

}
