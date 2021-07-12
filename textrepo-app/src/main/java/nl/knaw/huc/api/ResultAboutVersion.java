package nl.knaw.huc.api;

import nl.knaw.huc.config.VersionConfiguration;

public class ResultAboutVersion {

  private final String tag;

  private final String commit;

  public ResultAboutVersion(VersionConfiguration version) {
    this.tag = version.tag;
    this.commit = version.commit;
  }

  public String getTag() {
    return tag;
  }

  public String getCommit() {
    return commit;
  }
}
