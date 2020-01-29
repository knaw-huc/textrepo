package nl.knaw.huc.db;

import nl.knaw.huc.core.Document;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;
import java.util.UUID;

public interface DocumentsDao {
  @SqlUpdate("insert into documents (id, external_id) values (:id, :externalId)")
  void insert(@BindBean Document document);

  @SqlQuery("select id, external_id from documents where id = ?")
  @RegisterConstructorMapper(value = Document.class)
  Document get(UUID id);

  @SqlQuery("select id, external_id from documents where external_id = ?")
  @RegisterConstructorMapper(value = Document.class)
  Optional<Document> getByExternalId(String externalId);
}
