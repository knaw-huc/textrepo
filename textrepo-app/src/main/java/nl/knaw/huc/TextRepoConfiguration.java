package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.service.index.MappedIndexerConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class TextRepoConfiguration extends Configuration {

  @Valid
  @NotNull
  private DataSourceFactory database = new DataSourceFactory();

  @Valid
  @NotNull
  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  @Valid
  @NotNull
  private SwaggerBundleConfiguration swaggerBundleConfiguration = new SwaggerBundleConfiguration();

  @Valid
  @NotNull
  private List<MappedIndexerConfiguration> indexers = new ArrayList<>();

  @Valid
  @NotNull
  private ResourceLimits resourceLimits;

  @Valid
  @NotNull
  private PaginationConfiguration pagination = new PaginationConfiguration();

  @Valid
  @NotNull
  private String dateFormat = "";

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

  @JsonProperty("swagger")
  public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
    return swaggerBundleConfiguration;
  }

  @JsonProperty("swagger")
  public void setSwaggerBundleConfiguration(
      SwaggerBundleConfiguration swaggerBundleConfiguration) {
    this.swaggerBundleConfiguration = swaggerBundleConfiguration;
  }

  @JsonProperty("limits")
  public ResourceLimits getResourceLimits() {
    return resourceLimits;
  }

  @JsonProperty("limits")
  public void setResourceLimits(ResourceLimits resourceLimits) {
    this.resourceLimits = resourceLimits;
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

  @JsonProperty("dateFormat")
  public String getDateFormat() {
    return dateFormat;
  }

  @JsonProperty("dateFormat")
  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }
}
