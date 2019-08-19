package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticsearchConfiguration {

  private String host;
  private int port;

  @JsonProperty
  public String getHost() {
    return host;
  }

  @JsonProperty
  public void setHost(String host) {
    this.host = host;
  }

  @JsonProperty
  public int getPort() {
    return port;
  }

  @JsonProperty
  public void setPort(int port) {
    this.port = port;
  }
}
