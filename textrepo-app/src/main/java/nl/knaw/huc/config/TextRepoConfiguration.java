package nl.knaw.huc.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import nl.knaw.huc.service.index.config.IndexerWithMappingConfiguration;

public class TextRepoConfiguration extends Configuration {

  @Valid
  @NotNull
  private DataSourceFactory database = new DataSourceFactory();

  @Valid
  @NotNull
  private SwaggerBundleConfiguration swaggerBundleConfiguration = new SwaggerBundleConfiguration();

  @Valid
  @NotNull
  private List<IndexerWithMappingConfiguration> indexers = new ArrayList<>();

  @Valid
  @NotNull
  private ResourceLimitsConfiguration resourceLimits = new ResourceLimitsConfiguration();

  @Valid
  @NotNull
  private PaginationConfiguration pagination = new PaginationConfiguration();

  @Valid
  @NotNull
  private String dateFormat = "";

  @Valid
  @NotNull
  private VersionConfiguration version = new VersionConfiguration();

  private FlywayConfiguration flyway;

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
  public ResourceLimitsConfiguration getResourceLimits() {
    return resourceLimits;
  }

  @JsonProperty("limits")
  public void setResourceLimits(ResourceLimitsConfiguration resourceLimits) {
    this.resourceLimits = resourceLimits;
  }

  @JsonProperty("indexers")
  public List<IndexerWithMappingConfiguration> getIndexers() {
    return indexers;
  }

  @JsonProperty("indexers")
  public void setCustomFacetIndexers(List<IndexerWithMappingConfiguration> indexers) {
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

  @JsonProperty("version")
  public VersionConfiguration getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(VersionConfiguration version) {
    this.version = version;
  }

  @JsonProperty("flyway")
  public FlywayConfiguration getFlyway() {
    return flyway;
  }

  @JsonProperty("flyway")
  public void setFlyway(FlywayConfiguration flyway) {
    this.flyway = flyway;
  }
}
