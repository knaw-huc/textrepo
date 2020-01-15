package nl.knaw.huc.core;

import nl.knaw.huc.exceptions.IllegalMimetypeException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public enum SupportedType {

  XML("application/xml"),
  TXT("text/plain");

  private String mimetype;

  SupportedType(String mimetype) {
    this.mimetype = mimetype;
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
    throw new IllegalMimetypeException(format(
        "Unexpected mimetype: got [%s] but should be one of [%s]",
        mimetype, stream(values()).map(v -> "" + v.getMimetype()).collect(joining(", ")))
    );
  }
}
