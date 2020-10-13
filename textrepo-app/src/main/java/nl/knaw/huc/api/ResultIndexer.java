package nl.knaw.huc.api;

import nl.knaw.huc.service.index.MappedIndexerConfiguration;

import java.util.List;

public class ResultIndexer {
  private final String name;
  private final List<String> hosts;
  private final List<String> mimetypes;

  public ResultIndexer(MappedIndexerConfiguration indexer) {
    this.name = indexer.name;
    this.hosts = indexer.elasticsearch.hosts;
    this.mimetypes = indexer.mimetypes;
  }

  public String getName() {
    return name;
  }

  public List<String> getHosts() {
    return hosts;
  }

  public List<String> getMimetypes() {
    return mimetypes;
  }
}
