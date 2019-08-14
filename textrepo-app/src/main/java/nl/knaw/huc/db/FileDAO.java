package nl.knaw.huc.db;

import nl.knaw.huc.api.TextRepoFile;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PSQLException;

public interface FileDAO {

  @SqlUpdate("insert into files (sha224, content) values (:sha224, :content)")
  void insert(@Bind("sha224") String sha224, @Bind("content") byte[] content);

  @SqlQuery("select sha224 from files where sha224 = :sha224")
  String existsSha224(@Bind("sha224") String sha224);

  @SqlQuery("select sha224, content from files where sha224 = :sha224")
  @RegisterConstructorMapper(value = TextRepoFile.class)
  TextRepoFile findBySha224(@Bind("sha224") String sha224);

}
