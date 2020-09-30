-- Activate the compressed version by first renaming the
-- old contents to 'uncompressed' and then renaming the new
-- contents in 'compressed' column to 'contents'

ALTER TABLE contents RENAME contents TO uncompressed;
ALTER TABLE contents RENAME compressed TO contents;
