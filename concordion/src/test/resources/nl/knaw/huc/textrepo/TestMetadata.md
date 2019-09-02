# TestMetadata

## [Creating and updating metadata](- 'createDocumentWithMetadata')

When:

 - creating a document [`test1.txt`](- "#filename") at [`/documents`](- "#documentEndpoint");
 - and adding metadata [`{"foo":"bar","spam":"eggs"}`](- "#metadata") at [`/documents/{documentId}/metadata`](- "#metadataEndpoint");

[ ](- "#doc=createDocumentWithMetadata(#filename, #documentEndpoint, #metadata, #metadataEndpoint)")
Then:

 - Metadata entry `filename` should have value: [test1.txt](- "?=#doc.filename");
 - Metadata entry `foo` should have value: [bar](- "?=#doc.foo");
 - Metadata entry `spam` should have value: [eggs](- "?=#doc.spam");

---

When:

 - updating document with 
   file [test2.txt](- "#newFilename") at [`/documents/{documentId}/files`](- "#documentFileEndpoint")
   [ ](- "#doc2=updateDocumentFilename(#documentFileEndpoint, #doc.documentId, #newFilename)")

Then:

 - Metadata entry `filename` should now have value: [test2.txt](- "?=#doc2.filename");
 - Metadata entry `foo` should still have value: [bar](- "?=#doc2.foo");
 - Metadata entry `spam` should still have value: [eggs](- "?=#doc2.spam");
 