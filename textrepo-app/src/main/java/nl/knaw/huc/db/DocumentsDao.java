package nl.knaw.huc.db;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.PageParams;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDateTime;
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
      "and (:createdAfter\\:\\:timestamp is null or created_at >= :createdAfter\\:\\:timestamp) " +
      "order by created_at desc " +
      "limit :limit offset :offset")
  @RegisterConstructorMapper(value = Document.class)
  List<Document> findBy(
      @Bind("externalId") String externalId,
      @Bind("createdAfter") LocalDateTime createdAfter,
      @BindBean PageParams pageParams
  );

  @SqlQuery("select count(*) from documents")
  long count();

  @SqlQuery("select count(*) from documents " +
      "where (:externalId is null or external_id like :externalId) " +
      "and (:createdAfter\\:\\:timestamp is null or created_at >= :createdAfter\\:\\:timestamp)"
  )
  int countBy(@Bind("externalId") String externalId, @Bind("createdAfter") LocalDateTime createdAfter);

  @SqlQuery("insert into documents (id, external_id) values (:id, :externalId) " +
      "on conflict (id) do update set external_id = excluded.external_id " +
      "returning *")
  @RegisterConstructorMapper(value = Document.class)
  Document upsert(@BindBean Document document);

  // trying to get this behaviour: if already registered, ignore, return existing entry in db
  // "returning *" does not return anything if "on conflict do nothing is used" hence rewrite external_id
  // hic sunt draconis in case of concurrency as per
  // https://stackoverflow.com/questions/34708509/how-to-use-returning-with-on-conflict-in-postgresql
  @SqlQuery("insert into documents (id, external_id) values (:id, :externalId) " +
      "on conflict (external_id) do update set external_id = excluded.external_id " +
      "returning *")
  @RegisterConstructorMapper(value = Document.class)
  Document register(@BindBean Document document);

  @SqlUpdate("delete from documents where id = ?")
  void delete(UUID id);
}
