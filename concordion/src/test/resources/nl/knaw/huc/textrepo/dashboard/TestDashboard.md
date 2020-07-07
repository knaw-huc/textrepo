# Test `/dashboard`

An overview of various diagnostic statistics can be found using the `dashboard`.

We first create the following documents:

  - external ID [`doc1`](- "#doc1") without metadata and without files;
  - external ID [`doc2`](- "#doc2") without metadata, but with a file of type
  [`text`](- "#doc2Type")
  - external ID [`doc3`](- "#doc3") without files, but with metadata
  key [`doc3Key`](- "#doc3Key") and value [`doc3Value`](- "#doc3Value")
  
[ ](- "#doc1Id=createDocument(#doc1)")

[ ](- "#doc2Id=createDocument(#doc2)")
[ ](- "#file2Id=createFile(#doc2Id)")

[ ](- "#doc3Id=createDocument(#doc3)")
[ ](- "createMetadata(#doc3Id, #doc3Key, #doc3Value)")

## Check document status
When checking the dashboard using a `GET` to [`/dashboard`](- "#endpoint")

[ ](- "#result=retrieve(#endpoint)")

Then:

  - The response status should be [200](- "?=#result.status");
  - There should be [3](- "?=#result.documentCount") documents;
  - There should be [2](- "?=#result.withoutFiles") documents without files;
  - There should be [2](- "?=#result.withoutMetadata") documents without metadata;
  - There should be [1](- "?=#result.orphans") *orphan*: a document with no file and no metadata; 

  - Full response:

[ ](- "ext:embed=#result.body")
