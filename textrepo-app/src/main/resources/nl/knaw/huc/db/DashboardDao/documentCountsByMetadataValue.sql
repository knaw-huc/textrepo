SELECT dm.value, count(dm.value)
FROM documents d
         JOIN documents_metadata dm ON dm.document_id = d.id
WHERE dm.key = :key
GROUP BY dm.value;