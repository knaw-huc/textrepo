package nl.knaw.huc.db;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.PageParams;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentsDao {

  /**
   * Postgres sets created_at to now()
   */
  @SqlQuery("insert into documents (id, external_id) values (:id, :externalId) " +
      "returning *")
  @RegisterConstructorMapper(value = Document.class)
  Document insert(@BindBean Document document);

  @SqlQuery("select id, external_id, created_at from documents where id = ?")
  @RegisterConstructorMapper(value = Document.class)
  Optional<Document> get(UUID id);

  @SqlQuery("select id, external_id, created_at from documents where external_id = ?")
  @RegisterConstructorMapper(value = Document.class)
  Optional<Document> getByExternalId(String externalId);

  @SqlQuery("select id, external_id, created_at from documents " +
      "where (:externalId is null or external_id like :externalId) " +
      "limit :limit offset :offset")
  @RegisterConstructorMapper(value = Document.class)
  List<Document> getByExternalIdLike(@Bind("externalId") String externalId, @BindBean PageParams pageParams);

  @SqlQuery("select count(*) from documents " +
      "where (:externalId is null or external_id like :externalId)")
  int countByExternalIdLike(@Bind("externalId") String externalId);

  @SqlQuery("insert into documents (id, external_id) values (:id, :externalId) " +
      "on conflict (id) do update set external_id = excluded.external_id " +
      "returning *")
  @RegisterConstructorMapper(value = Document.class)
  Document upsert(@BindBean Document document);

  @SqlUpdate("delete from documents where id = ?")
  void delete(UUID id);
}
