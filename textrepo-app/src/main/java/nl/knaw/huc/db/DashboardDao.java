package nl.knaw.huc.db;

import nl.knaw.huc.core.Document;
import nl.knaw.huc.core.DocumentsOverview;
import nl.knaw.huc.core.PageParams;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface DashboardDao {
  @SqlQuery("select * from get_documents_overview()")
  @RegisterConstructorMapper(DocumentsOverview.class)
  DocumentsOverview getDocumentsOverview();

  @SqlQuery("select count(*) from documents d where " +
      "not exists (select from documents_files fm where fm.document_id = d.id) " +
      "and not exists (select from documents_metadata dm where dm.document_id = d.id) ")
  int countOrphans();

  @SqlQuery("select d.id, d.external_id, d.created_at from documents d where " +
      "not exists (select from documents_files fm where fm.document_id = d.id)" +
      "and not exists (select from documents_metadata dm where dm.document_id = d.id) " +
      "order by d.external_id " +
      "limit :limit offset :offset")
  @RegisterConstructorMapper(Document.class)
  List<Document> findOrphans(@BindBean PageParams pageParams);
}
