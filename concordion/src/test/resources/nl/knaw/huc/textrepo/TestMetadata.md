# TestMetadata

## [Creating and updating metadata](- 'createDocumentWithMetadata')

When creating a document [test1.txt](- "#filename") 
with metadata [{"foo":"bar","spam":"eggs"}](- "#metadata")
[ ](- "#doc=createDocumentWithMetadata(#filename, #metadata)")
, then:

 - Metadata entry `filename` should have value: [test1.txt](- "?=#doc.filename");
 - Metadata entry `foo` should have value: [bar](- "?=#doc.foo");
 - Metadata entry `spam` should have value: [eggs](- "?=#doc.spam");

When updating document with 
file [test2.txt](- "#newFilename") 
[ ](- "#doc2=updateDocumentFilename(#doc.documentId, #newFilename)")
, then:

 - Metadata entry `filename` should now have value: [test2.txt](- "?=#doc2.filename");
 - Metadata entry `foo` should still have value: [bar](- "?=#doc2.foo");
 - Metadata entry `spam` should still have value: [eggs](- "?=#doc2.spam");
 