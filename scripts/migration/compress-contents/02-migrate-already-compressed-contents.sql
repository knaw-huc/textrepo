-- this assumes column 'compressed' has been added to table 'contents'

-- check if 'contents' begins with gzip magic header bytes 0x1f8b,
-- if so, copy contents as is into 'compressed' column
UPDATE contents SET compressed = contents
  WHERE compressed IS NULL
  AND SUBSTRING(contents FROM 1 FOR 2) = '\x1f8b';
