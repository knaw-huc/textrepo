package nl.knaw.huc.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huc.resources.ResourceUtils.locationOf;

public class MultipleLocations {

  @JsonProperty
  public final List<URI> locations;

  public MultipleLocations(List<Version> versions) {
    locations = versions
        .stream()
        .map(v -> locationOf(v))
        .collect(toList());
  }
}
