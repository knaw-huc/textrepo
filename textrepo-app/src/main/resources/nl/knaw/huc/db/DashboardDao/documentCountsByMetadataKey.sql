SELECT coalesce(dm.key, '<no metadata>') as "key", count(d.id)
FROM documents d
         LEFT JOIN documents_metadata dm ON d.id = dm.document_id
GROUP BY dm.key
ORDER BY dm.key;
