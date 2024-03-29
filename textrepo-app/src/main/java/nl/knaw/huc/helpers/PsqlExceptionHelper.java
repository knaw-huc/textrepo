package nl.knaw.huc.helpers;

import org.postgresql.util.PSQLException;

public class PsqlExceptionHelper {

  public enum Constraint {
    // These constraints are named postgres constraints, from "postgres/initdb/02-init.sql"
    FILES_TYPE_ID("files_type_id_fkey"),
    VERSIONS_CONTENTS_SHA("versions_contents_sha_fkey"),
    DOCUMENTS_EXTERNAL_ID_KEY("documents_external_id_key"),
    DOCUMENTS_METADATA_DOCUMENT_ID_FKEY("documents_metadata_document_id_fkey"),
    FILES_METADATA_FILE_ID_FKEY("files_metadata_file_id_fkey"),
    VERSIONS_METADATA_FILE_ID_FKEY("versions_metadata_version_id_fkey");

    private final String name;

    Constraint(String name) {
      this.name = name;
    }
  }

  /**
   * Check if exception contains a psql exception about the violated constraint.
   */
  public static boolean violatesConstraint(
      Exception ex,
      String constraint
  ) {
    if (!(ex.getCause() instanceof PSQLException)) {
      return false;
    }

    var cause = (PSQLException) ex.getCause();

    return constraint.equals(cause.getServerErrorMessage().getConstraint());
  }

  public static boolean violatesConstraint(Exception ex, Constraint constraint) {
    return violatesConstraint(ex, constraint.name);
  }
}
