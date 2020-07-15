SELECT
    (SELECT COUNT(*) FROM documents d) AS document_count,
    (SELECT COUNT(*) FROM documents d, documents_files df
        WHERE df.document_id = d.id) AS has_file,
    (SELECT COUNT(*) FROM documents d, documents_metadata dm
        WHERE dm.document_id = d.id) AS has_metadata,
    (SELECT COUNT(*)
        FROM documents d, documents_files df, documents_metadata dm
        WHERE df.document_id = d.id AND dm.document_id = d.id)
        AS has_both;
