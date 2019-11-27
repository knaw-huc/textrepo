package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.knaw.huc.resources.ResourceUtils.locationOf;

public class MultipleLocations {

  @JsonProperty
  public Map<String, URI> locations;

  public MultipleLocations() {
    // jackson
  }

  public MultipleLocations(List<ResultContents> resultContentsList) {
    locations = resultContentsList
      .stream()
      .collect(Collectors.toMap(
        ResultContents::getFilename,
        r -> locationOf(r.getVersion().getFileId())
      ));
  }
}
