package nl.knaw.huc.service;

import nl.knaw.huc.MimetypeSubtypesConfiguration;
import nl.knaw.huc.api.MimetypeSubtypesResult;
import nl.knaw.huc.core.SupportedType;

import java.util.ArrayList;
import java.util.List;

public class SubtypeService {

  private final List<MimetypeSubtypesConfiguration> mimetypeSubtypes;

  public SubtypeService(List<MimetypeSubtypesConfiguration> mimetypeSubtypes) {
    this.mimetypeSubtypes = mimetypeSubtypes;
  }

  public SupportedType determine(String mimetype) {
    if (SupportedType.exists(mimetype)) {
      return SupportedType.fromString(mimetype);
    }

    for (var mimetypeSubtype : mimetypeSubtypes) {
      if (mimetypeSubtype.getSubtypes().contains(mimetype)) {
        return SupportedType.fromString(mimetypeSubtype.getMimetype());
      }
    }

    throw SupportedType.unexpectedMimetype(mimetype);
  }

  public static List<MimetypeSubtypesResult> toTypesResultList(
      List<MimetypeSubtypesConfiguration> mimetypeSubtypes
  ) {
    List<MimetypeSubtypesResult> result = new ArrayList<>();
    for (var mimetype : SupportedType.values()) {
      var subtypes = mimetypeSubtypes
          .stream()
          .filter(st -> st.getMimetype().equals(mimetype.getMimetype()))
          .map(MimetypeSubtypesConfiguration::getSubtypes)
          .findFirst();

      var typeResult = new MimetypeSubtypesResult(
          mimetype.getMimetype(),
          subtypes.orElse(new ArrayList<>())
      );
      result.add(typeResult);
    }
    return result;
  }

  public List<MimetypeSubtypesConfiguration> getMimetypeSubtypes() {
    return mimetypeSubtypes;
  }
}
