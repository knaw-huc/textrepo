package nl.knaw.huc.api;

import nl.knaw.huc.MimetypeSubtypesConfiguration;
import nl.knaw.huc.core.SupportedType;

import java.util.ArrayList;
import java.util.List;


public class MimetypeSubtypesResult {
  public String mimetype;
  public List<String> subtypes;

  public MimetypeSubtypesResult(String mimetype, List<String> subtypes) {

    this.mimetype = mimetype;
    this.subtypes = subtypes;
  }
}
