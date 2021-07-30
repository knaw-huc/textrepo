# Test `/dashboard`

An overview of various diagnostic statistics can be found using the `dashboard`.

We first create the following documents:

  - external ID [doc1](- "#doc1") without metadata and without files;
  - external ID [doc2](- "#doc2") without metadata, but with a file of type
  [text](- "#doc2Type")
  - external ID [doc3](- "#doc3") without files, but with metadata
  key [doc3Key](- "#doc3Key") and value [doc3Value](- "#doc3Value")
  - external ID [doc4](- "#doc4") with both a file of type [text](- "#doc4Type")
  and metadata with key [doc4Key](- "#doc4Key") and value [doc4Value](- "#doc4Value")
  
[ ](- "#doc1Id=createDocument(#doc1)")

[ ](- "#doc2Id=createDocument(#doc2)")
[ ](- "#file2Id=createFile(#doc2Id)")

[ ](- "#doc3Id=createDocument(#doc3)")
[ ](- "createMetadata(#doc3Id, #doc3Key, #doc3Value)")

[ ](- "#doc4Id=createDocument(#doc4)")
[ ](- "#file4Id=createFile(#doc4Id)")
[ ](- "createMetadata(#doc4Id, #doc4Key, #doc4Value)")

## Check document count overview
When checking the dashboard using a `GET` to [/dashboard](- "#endpoint")

[ ](- "#result=retrieve(#endpoint)")

Then:

  - The response status should be [200 OK](- "?=#result.status");
  - There should be [4 documents](- "?=#result.documentCount") (`doc1`, `doc2`, `doc3`, `doc4`);
  - There should be [2 documents with a file](- "?=#result.hasFile") (`doc1`, `doc3`);
  - There should be [2 documents with metadata](- "?=#result.hasMetadata") (`doc1`, `doc2`);
  - There should be [1 document with both a file and metadata](- "?=#result.hasBoth") (`doc4`)
  - There should be [1 document without any files or metadata](- "?=#result.hasNone"): ("orphan") (`doc1`); 

  - Full response:

[ ](- "ext:embed=#result.body")

## Inspect orphans

Orphaned documents, i.e., documents with no file and no metadata associated with them, can be listed.

When checking for orphans using a `GET` to [/dashboard/orphans](- "#endpoint")

[ ](- "#result=retrieveOrphans(#endpoint)")

Then:

 - The response status should be [200 OK](- "?=#result.status");
 - The result [is properly paginated](- "?=#result.isPaginated") using `items`, `total`, `limit`, and `offset`;
 - The `items` array contains exactly the *orphan* document with [externalId: ["doc1"]](- "?=#result.orphanExternalId");
 - There should be [1 item](- "?=#result.itemCount") in total;

 - Full response:

 [ ](- "ext:embed=#result.body")
