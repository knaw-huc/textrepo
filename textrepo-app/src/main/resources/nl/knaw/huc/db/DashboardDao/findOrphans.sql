SELECT d.id, d.external_id, d.created_at
FROM documents d
WHERE NOT EXISTS(SELECT FROM documents_files fm WHERE fm.document_id = d.id)
  AND NOT EXISTS(SELECT FROM documents_metadata dm WHERE dm.document_id = d.id)
ORDER BY d.external_id
LIMIT :limit OFFSET :offset
