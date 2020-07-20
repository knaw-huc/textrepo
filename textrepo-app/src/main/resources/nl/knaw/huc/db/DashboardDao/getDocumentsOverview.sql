SELECT (SELECT count(*) as document_count
        FROM documents d),

       (SELECT count(*) AS has_file
        FROM documents d
        WHERE exists(SELECT df.document_id from documents_files df WHERE df.document_id = d.id)),

       (SELECT count(*) AS has_metadata
        FROM documents d
        WHERE exists(SELECT dm.document_id from documents_metadata dm WHERE dm.document_id = d.id)),

       (SELECT count(*) as has_both
        FROM documents d
        WHERE exists(SELECT df.document_id from documents_metadata df WHERE df.document_id = d.id)
          AND exists(SELECT dm.document_id from documents_files dm WHERE dm.document_id = d.id));
