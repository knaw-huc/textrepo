-- truncate all tables owned by username
CREATE OR REPLACE FUNCTION truncate_tables_by_owner(username IN VARCHAR) RETURNS void AS $$
DECLARE
    statements CURSOR FOR
    SELECT tablename FROM pg_tables
    WHERE tableowner = username AND schemaname = 'public';
BEGIN
  FOR stmt IN statements LOOP
    RAISE NOTICE 'truncate table %', stmt.tablename;
    EXECUTE 'TRUNCATE TABLE ' || quote_ident(stmt.tablename) || ' CASCADE;';
  END LOOP;
END;
$$ LANGUAGE plpgsql;



-- overview of which documents have files and / or metadata
CREATE OR REPLACE FUNCTION get_documents_overview(
  OUT document_count bigint, OUT has_file bigint,
  OUT has_metadata bigint, OUT has_both bigint) AS $$
    SELECT
        (SELECT COUNT(*) FROM documents d),
        (SELECT COUNT(*) FROM documents d, documents_files df
	    WHERE df.document_id = d.id),
        (SELECT COUNT(*) FROM documents d, documents_metadata dm
	    WHERE dm.document_id = d.id),
        (SELECT COUNT(*)
	    FROM documents d, documents_files df, documents_metadata dm
            WHERE df.document_id = d.id AND dm.document_id = d.id);
$$ LANGUAGE SQL;
