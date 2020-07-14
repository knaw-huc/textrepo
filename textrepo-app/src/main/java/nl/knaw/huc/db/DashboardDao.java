package nl.knaw.huc.db;

import nl.knaw.huc.api.DocumentCounts;
import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.PageParams;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface DashboardDao {
  @SqlQuery("select count(*) from documents")
  int countDocuments();

  @SqlQuery("select count(*) from documents d where " +
      "not exists (select from documents_files df where df.document_id = d.id)")
  int countDocumentsWithoutFiles();

  @SqlQuery("select count(*) from documents d where " +
      "not exists (select from documents_metadata dm where dm.document_id = d.id)")
  int countDocumentsWithoutMetadata();

  @SqlQuery("select count(*) from documents d where " +
      "not exists (select from documents_files fm where fm.document_id = d.id) " +
      "and not exists (select from documents_metadata dm where dm.document_id = d.id) ")
  int countOrphans();

  @SqlQuery("select " +
      "(select count(*) from documents d) as document_count, " +
      "(select count(*) from documents d, documents_files df where d.id = df.document_id) as has_file, " +
      "(select count(*) from documents d, documents_metadata dm where d.id = dm.document_id) as has_metadata, " +
      "(select count(*) from documents d, documents_files df, documents_metadata dm " +
      "where d.id = df.document_id and d.id = dm.document_id) as has_both;")
  @RegisterConstructorMapper(DocumentCounts.class)
  DocumentCounts getDocumentCounts();

  @SqlQuery("select d.id, d.external_id, d.created_at from documents d where " +
      "not exists (select from documents_files fm where fm.document_id = d.id)" +
      "and not exists (select from documents_metadata dm where dm.document_id = d.id) " +
      "order by d.external_id " +
      "limit :limit offset :offset")
  @RegisterConstructorMapper(Document.class)
  List<Document> findOrphans(@BindBean PageParams pageParams);
}
