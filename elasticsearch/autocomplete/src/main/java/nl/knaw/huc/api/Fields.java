package nl.knaw.huc.api;

import java.util.Set;

public class Fields {

  public Set<Suggestion> suggest;

  public Fields(Set<Suggestion> suggest) {
    this.suggest = suggest;
  }

}
