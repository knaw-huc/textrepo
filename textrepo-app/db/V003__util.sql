-- truncate all tables owned by username
-- except for the flyway_schema_history table
CREATE OR REPLACE FUNCTION truncate_tables_by_owner(username IN VARCHAR) RETURNS void AS $$
DECLARE
    statements CURSOR FOR
    SELECT tablename FROM pg_tables
    WHERE tableowner = username AND schemaname = 'public' AND tablename <> 'flyway_schema_history';
BEGIN
  FOR stmt IN statements LOOP
    RAISE NOTICE 'truncate table %', stmt.tablename;
    EXECUTE 'TRUNCATE TABLE ' || quote_ident(stmt.tablename) || ' CASCADE;';
  END LOOP;
END;
$$ LANGUAGE plpgsql;
