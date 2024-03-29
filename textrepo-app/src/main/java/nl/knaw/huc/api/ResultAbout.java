package nl.knaw.huc.api;

import static java.util.stream.Collectors.toList;

import java.util.List;
import nl.knaw.huc.config.TextRepoConfiguration;

public class ResultAbout {
  private final ResultAboutVersion version;
  private final String contentDecompressionLimit;

  private final List<ResultIndexer> indexers;

  public ResultAbout(TextRepoConfiguration config) {
    this.version = new ResultAboutVersion(config.getVersion());
    this.contentDecompressionLimit = config.getResourceLimits().contentDecompressionLimit + "kB";

    this.indexers = config
        .getIndexers()
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
