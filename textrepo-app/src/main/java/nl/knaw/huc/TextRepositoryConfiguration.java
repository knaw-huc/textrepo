package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.service.index.MappedIndexerConfiguration;
import nl.knaw.huc.service.index.ElasticsearchConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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
  private List<MappedIndexerConfiguration> indexers = new ArrayList<>();

  @Valid
  @NotNull
  private PaginationConfiguration pagination = new PaginationConfiguration();

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

  @JsonProperty("indexers")
  public List<MappedIndexerConfiguration> getCustomFacetIndexers() {
    return indexers;
  }

  @JsonProperty("indexers")
  public void setCustomFacetIndexers(List<MappedIndexerConfiguration> indexers) {
    this.indexers = indexers;
  }

  @JsonProperty("pagination")
  public PaginationConfiguration getPagination() {
    return pagination;
  }

  @JsonProperty("pagination")
  public void setPagination(PaginationConfiguration pagination) {
    this.pagination = pagination;
  }
}
