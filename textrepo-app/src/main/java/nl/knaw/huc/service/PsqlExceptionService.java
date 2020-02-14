package nl.knaw.huc.service;

import org.postgresql.util.PSQLException;

public class PsqlExceptionService {

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

}
