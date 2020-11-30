package nl.knaw.huc.core;

import nl.knaw.huc.exceptions.IllegalMimetypeException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public enum SupportedType {

  XML("application/xml"),
  TXT("text/plain"),
  ODT("application/vnd.oasis.opendocument.text"),
  DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

  private String mimetype;

  SupportedType(String mimetype) {
    this.mimetype = mimetype;
  }

  public static boolean exists(String mimetype) {
    return stream(values()).anyMatch(st -> st.mimetype.equals(mimetype));
  }

  public String getMimetype() {
    return mimetype;
  }

  public static SupportedType fromString(String mimetype) {
    for (var supportedType : values()) {
      if (supportedType.mimetype.equals(mimetype)) {
        return supportedType;
      }
    }
    throw unexpectedMimetype(mimetype);
  }

  public static IllegalMimetypeException unexpectedMimetype(String mimetype) {
    return new IllegalMimetypeException(format(
        "Unexpected mimetype: got [%s] but should be one of [%s] or their subtypes",
        mimetype, supported()
    ));
  }

  private static String supported() {
    return stream(values()).map(v -> "" + v.getMimetype()).collect(joining(", "));
  }
}
