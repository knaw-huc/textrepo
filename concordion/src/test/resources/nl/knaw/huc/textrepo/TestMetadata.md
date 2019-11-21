# TestMetadata

## [Creating and updating metadata](- 'createFileWithMetadata')

When:

 - creating a file [`test1.txt`](- "#filename") at [`/files`](- "#fileEndpoint");
 - and adding metadata [`{"foo":"bar","spam":"eggs"}`](- "#metadata") at [`/files/{fileId}/metadata`](- "#metadataEndpoint");

[ ](- "#file=createFileWithMetadata(#filename, #fileEndpoint, #metadata, #metadataEndpoint)")
Then:

 - Metadata entry `filename` should have value: [test1.txt](- "?=#file.filename");
 - Metadata entry `spam` should have value: [eggs](- "?=#file.spam");
 - Metadata entry `foo` should have value: [bar](- "?=#file.foo");

---

When:

 - updating file with 
   file [test2.txt](- "#newFilename") at [`/files/{fileId}/contents`](- "#fileContentsEndpoint")
   [ ](- "#file2=updateMetadataNameOfFile(#fileContentsEndpoint, #file.fileId, #newFilename)")

Then:

 - Metadata entry `filename` should now have value: [test2.txt](- "?=#file2.filename");
 - Metadata entry `spam` should still have value: [eggs](- "?=#file2.spam");
 - Metadata entry `foo` should still have value: [bar](- "?=#file2.foo");

When:

 - updating file with 
   metadata-key [`foo`](- "#updatedKey") and -value [`baz`](- "#updatedValue") at [`/files/{fileId}/metadata/{key}`](- "#fileMetadataEndpoint")
   [ ](- "#file3=updateMetadataEntry(#fileMetadataEndpoint, #file.fileId, #updatedKey, #updatedValue)")

Then:

 - Metadata entry `filename` should still have value: [test2.txt](- "?=#file3.filename");
 - Metadata entry `spam` should still have value: [eggs](- "?=#file3.spam");
 - Metadata entry `foo` should now have value: [baz](- "?=#file3.foo");
 