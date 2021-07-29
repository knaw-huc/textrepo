# Test Index Mutations

When creating, updating and deleting files in the database, the appropriate changes in the indices will also be made.
New file versions will overwrite older versions in the es index. 

To retrieve suggestions, we first create a document with a text file: 
  
[ ](- "#docId1=createDocument()")
[ ](- "#fileId1=createFile(#docId1)")

  - File ID: [ ](- "c:echo=#fileId1")

## Index when creating files
Files are indexed when added:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [text](- "#fileType")

[ ](- "#result=searchFileIndexWithoutVersions()")

- The response status should be: [200](- "?=#result.status");
- The response should contain [correct file](- "?=#result.found");
- The response should contain [0](- "?=#result.versionCount") versions;

Full response:
[ ](- "ext:embed=#result.body")

## Index when creating file versions

When a new file version with contents 
"[hello Tesseract](- "#text1")", 
is [POST](- "#versions=upload(#fileId1, #text1)")ed,
then we should have a [valid versions](- "?=#versions.validVersions"):

  - [ ](- "c:echo=#versions.versionUuid1")
  - [ ](- "c:echo=#versions.versionUuid2")

Files are re-indexed when versions are added:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [text](- "#fileType")

[ ](- "#result=searchFileIndexWithVersions()")

- The response status should be: [200](- "?=#result.status");
- The response should contain [correct file](- "?=#result.found");
- The response should contain [2](- "?=#result.versionCount") versions;

Full response:
[ ](- "ext:embed=#result.body")

## Index when updating file type

When updating file [ ](- "c:echo=#fileId1") with a `PUT` to [/rest/files/{id}](- "#updateEndpoint"):

[{
"docId": "{docId}", "typeId": {typeId}
}](- "#updatedEntity")


[ ](- "#updateResult=update(#updateEndpoint, #fileId1, #updatedEntity, #docId1, getFooTypeId())")

Then:

- The response status should be: [200](- "?=#updateResult.status");
- The response should contain the [updated type](- "?=#updateResult.updatedType");

Files are re-indexed when file type is changed:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [foo](- "#fileType")

[ ](- "#result=searchFileIndexWithType(#fileType)")

- The response status should be: [200](- "?=#result.status");
- The response should contain [correct file](- "?=#result.found");
- The response should contain [correct type](- "?=#result.type");

Full response:
[ ](- "ext:embed=#result.body")

