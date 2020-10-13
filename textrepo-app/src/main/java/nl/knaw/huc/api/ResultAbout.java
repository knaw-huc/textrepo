package nl.knaw.huc.api;

import nl.knaw.huc.TextRepoConfiguration;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ResultAbout {
  private final ResultAboutVersion version;
  private final String contentDecompressionLimit;

  private final List<ResultIndexer> indexers;

  public ResultAbout(TextRepoConfiguration config) {
    this.version = new ResultAboutVersion(config.getVersion());
    this.contentDecompressionLimit = config.getResourceLimits().contentDecompressionLimit + "kB";

    this.indexers = config
        .getCustomFacetIndexers()
        .stream()
        .map(ResultIndexer::new)
        .collect(toList());
  }

  public ResultAboutVersion getVersion() {
    return version;
  }

  public List<ResultIndexer> getIndexers() {
    return indexers;
  }

  public String getContentDecompressionLimit() {
    return contentDecompressionLimit;
  }
}
