package nl.knaw.huc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ElasticsearchConfiguration {
  @JsonProperty
  List<String> hosts;
}
