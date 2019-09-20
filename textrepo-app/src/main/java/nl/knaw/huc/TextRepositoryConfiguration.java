package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TextRepositoryConfiguration extends Configuration {

  @Valid
  @NotNull
  private DataSourceFactory database = new DataSourceFactory();

  @Valid
  @NotNull
  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  @Valid
  @NotNull
  private ElasticsearchConfiguration elasticsearch = new ElasticsearchConfiguration();

  @Valid
  @NotNull
  private SwaggerBundleConfiguration swaggerBundleConfiguration = new SwaggerBundleConfiguration();

  @Valid
  @NotNull
  private CustomFacetIndexerConfiguration customFacetIndexer = new CustomFacetIndexerConfiguration();

  @JsonProperty("jerseyClient")
  public JerseyClientConfiguration getJerseyClientConfiguration() {
    return jerseyClient;
  }

  @JsonProperty("jerseyClient")
  public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
    this.jerseyClient = jerseyClient;
  }

  @JsonProperty("database")
  public void setDataSourceFactory(DataSourceFactory factory) {
    this.database = factory;
  }

  @JsonProperty("database")
  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  @JsonProperty("elasticsearch")
  public ElasticsearchConfiguration getElasticsearch() {
    return elasticsearch;
  }

  @JsonProperty("elasticsearch")
  public void setElasticsearch(ElasticsearchConfiguration elasticsearch) {
    this.elasticsearch = elasticsearch;
  }

  @JsonProperty("swagger")
  public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
    return swaggerBundleConfiguration;
  }

  @JsonProperty("swagger")
  public void setSwaggerBundleConfiguration(
      SwaggerBundleConfiguration swaggerBundleConfiguration) {
    this.swaggerBundleConfiguration = swaggerBundleConfiguration;
  }

  @JsonProperty("customFacetIndexer")
  public CustomFacetIndexerConfiguration getCustomFacetIndexer() {
    return customFacetIndexer;
  }

  @JsonProperty("customFacetIndexer")
  public void setCustomFacetIndexer(CustomFacetIndexerConfiguration customFacetIndexer) {
    this.customFacetIndexer = customFacetIndexer;
  }
}
