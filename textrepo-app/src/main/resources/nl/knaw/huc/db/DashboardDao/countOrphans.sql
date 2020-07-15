SELECT count(*)
FROM documents d
WHERE NOT EXISTS(SELECT FROM documents_files fm WHERE fm.document_id = d.id)
  AND NOT EXISTS(SELECT FROM documents_metadata dm WHERE dm.document_id = d.id)
