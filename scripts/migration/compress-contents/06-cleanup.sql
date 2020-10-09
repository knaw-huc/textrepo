-- reclaim diskspace after dropping column 'contents' by performing a 'full vacuum'
--
-- See https://www.postgresql.org/docs/9.1/sql-vacuum.html on how 'aggressive'
-- VACUUM FULL is vs. a regular VACUUM

VACUUM FULL;
