package nl.knaw.huc.service.index;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Supported methods to POST to the ./fields-endpoint of an indexer:
 * - ORIGINAL: POST a file to ./fields with the request body containing the file contents
 *   and the Content-Type header containing the original mimetype
 * - MULTIPART: POST a file to ./fields with a `multipart/form-data` Content-Type header
 *   and a multipart body part named "file" which contains the file contents
 *   and a Content-Type header with the file mimetype
 */
public enum FieldsType {

  ORIGINAL("original"),
  MULTIPART("multipart");

  private final String name;

  FieldsType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static FieldsType fromString(String str) {
    return stream(FieldsType.values())
        .filter(ft -> ft.name.equals(str))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(format(
            "Unexpected FieldsType.name: got [%s] but should be one of [%s]",
            str, stream(values()).map(v -> "" + v.getName()).collect(joining(", ")))
        ));
  }
}
