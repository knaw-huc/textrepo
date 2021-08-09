package nl.knaw.huc.resources.about;

import com.jayway.jsonpath.JsonPath;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.huc.config.TextRepoConfiguration;
import nl.knaw.huc.config.VersionConfiguration;
import nl.knaw.huc.service.index.config.ElasticsearchConfiguration;
import nl.knaw.huc.service.index.FieldsConfiguration;
import nl.knaw.huc.service.index.config.IndexerWithMappingConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huc.service.index.FieldsType.ORIGINAL;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AboutResourceTest {

  private static final TextRepoConfiguration config;
  public static final ResourceExtension resource;

  static {
    config = new TextRepoConfiguration();
    var version = new VersionConfiguration();
    version.tag = "1.2.3-subliem";
    version.commit = "1234567890";
    config.setVersion(version);

    var indexers = new ArrayList<IndexerWithMappingConfiguration>();
    var indexerConfiguration = new IndexerWithMappingConfiguration();
    indexerConfiguration.name = "drommels";
    var fields = new FieldsConfiguration();
    fields.type = ORIGINAL;
    fields.url = "http://indexer-fields.wow";
    indexerConfiguration.mapping = "http://indexer-mapping.wow";
    indexerConfiguration.types = "http://indexer-types.wow";
    indexerConfiguration.fields = fields;
    indexerConfiguration.elasticsearch = new ElasticsearchConfiguration();
    indexerConfiguration.elasticsearch.hosts = new ArrayList<>();
    indexerConfiguration.elasticsearch.hosts.add("http://localhost:9200");
    indexers.add(indexerConfiguration);
    config.setCustomFacetIndexers(indexers);

    resource = ResourceExtension
        .builder()
        .addResource(new AboutResource(config))
        .build();
  }

  @Test
  public void aboutPage_returnsPageWithIndexerInfo() {
    final var response = resource
        .client()
        .target("/")
        .request(APPLICATION_JSON)
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
    final var body = response.readEntity(String.class);
    final var json = JsonPath.parse(body);
    assertThat(json.read("$.version.tag", String.class)).isEqualTo("1.2.3-subliem");
    assertThat(json.read("$.version.commit", String.class)).isEqualTo("1234567890");
    assertThat(json.read("$.indexers[0].name", String.class)).isEqualTo("drommels");
    assertThat(json.read("$.indexers[0].fields.url", String.class)).isEqualTo("http://indexer-fields.wow");
    assertThat(json.read("$.indexers[0].fields.type", String.class)).isEqualTo("original");
    assertThat(json.read("$.indexers[0].mapping", String.class)).isEqualTo("http://indexer-mapping.wow");
    assertThat(json.read("$.indexers[0].types", String.class)).isEqualTo("http://indexer-types.wow");
    assertThat(json.read("$.indexers[0].hosts[0]", String.class)).isEqualTo("http://localhost:9200");

  }

}
