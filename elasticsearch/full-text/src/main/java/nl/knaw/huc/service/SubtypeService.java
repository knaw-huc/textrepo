package nl.knaw.huc.service;

import nl.knaw.huc.MimetypeSubtypesConfiguration;
import nl.knaw.huc.core.SupportedType;

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

  public List<MimetypeSubtypesConfiguration> getMimetypeSubtypes() {
    return mimetypeSubtypes;
  }
}
