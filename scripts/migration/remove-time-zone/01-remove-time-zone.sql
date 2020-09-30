-- remove time zone from timestamps, so postgres data type for timestamp
-- is in sync with Java / Dao classes using LocalDateTime.java

ALTER TABLE versions ALTER COLUMN created_at TYPE timestamp;
ALTER TABLE documents ALTER COLUMN created_at TYPE timestamp;
