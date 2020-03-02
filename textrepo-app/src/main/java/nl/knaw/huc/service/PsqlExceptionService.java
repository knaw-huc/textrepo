package nl.knaw.huc.service;

import org.postgresql.util.PSQLException;

public class PsqlExceptionService {

  public enum Constraint {
    // These constraints are named postgres constraints, from "postgres/initdb/02-init.sql"
    FILES_TYPE_ID("files_type_id_fkey"),
    VERSIONS_CONTENTS_SHA("versions_contents_sha_fkey");

    private final String name;

    Constraint(String name) {
      this.name = name;
    }
  }

  /**
   * Check if exception contains a psql exception about the violated constraint
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