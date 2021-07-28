package nl.knaw.huc.api;

import java.util.List;


public class MimetypeSubtypesResult {
  public String mimetype;
  public List<String> subtypes;

  public MimetypeSubtypesResult(String mimetype, List<String> subtypes) {

    this.mimetype = mimetype;
    this.subtypes = subtypes;
  }
}
