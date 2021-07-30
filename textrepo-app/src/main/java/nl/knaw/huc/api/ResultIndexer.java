package nl.knaw.huc.api;

import nl.knaw.huc.service.index.config.IndexerWithMappingConfiguration;

import java.util.List;

public class ResultIndexer {
  private final String name;
  private final List<String> hosts;
  private final ResultIndexerFieldsConfiguration fields;
  private final String mapping;
  private final String types;

  public ResultIndexer(IndexerWithMappingConfiguration indexer) {
    this.name = indexer.name;
    this.hosts = indexer.elasticsearch.hosts;
    this.fields = new ResultIndexerFieldsConfiguration(indexer.fields);
    this.mapping = indexer.mapping;
    this.types = indexer.types;
  }

  public String getName() {
    return name;
  }

  public List<String> getHosts() {
    return hosts;
  }

  public ResultIndexerFieldsConfiguration getFields() {
    return fields;
  }

  public String getMapping() {
    return mapping;
  }

  public String getTypes() {
    return types;
  }
}
