package nl.knaw.huc.api;

import nl.knaw.huc.MimetypeSubtypesConfiguration;
import nl.knaw.huc.core.SupportedType;

import java.util.ArrayList;
import java.util.List;

public class MimetypeSubtypesResult {

  public List<MimetypeSubtypeResult> types = new ArrayList<>();

  /**
   * Merge SupportedTypes and mimetypeSubtypes into a single List< MimetypeSubtype >
   */
  public MimetypeSubtypesResult(List<MimetypeSubtypesConfiguration> mimetypeSubtypes) {
    for (var mimetype : SupportedType.values()) {
      var subtypes = mimetypeSubtypes
          .stream()
          .filter(st -> st.getMimetype().equals(mimetype.getMimetype()))
          .map(MimetypeSubtypesConfiguration::getSubtypes)
          .findFirst();

      var typeResult = new MimetypeSubtypeResult(
          mimetype.getMimetype(),
          subtypes.orElse(new ArrayList<>())
      );
      types.add(typeResult);
    }
  }

  public static class MimetypeSubtypeResult {
    public String mimetype;
    public List<String> subtypes;

    public MimetypeSubtypeResult(String mimetype, List<String> subtypes) {

      this.mimetype = mimetype;
      this.subtypes = subtypes;
    }
  }
}
