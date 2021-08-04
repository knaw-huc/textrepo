# Test Index Mutations

When creating, updating and deleting files in the database, the appropriate changes in the indices will also be made.
New file versions will overwrite older versions in the es index. 

## Index when creating files

Files are indexed when added.

To retrieve suggestions, we first create a document with a text file: 
  
[ ](- "#docId1=createDocument()")
[ ](- "#fileId1=createFile(#docId1)")

  - File ID: [ ](- "c:echo=#fileId1")

Find all ES docs using query:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [text](- "#fileType")

[ ](- "#result=searchFileIndexWithoutVersions()")

- The response status should be: [200](- "?=#result.status");
- The response should contain [correct file](- "?=#result.found");
- The response should contain [0](- "?=#result.versionCount") versions;

## Index when creating file versions

Files are re-indexed when versions are added.

When new file versions with contents 
"[op zondag naar een tuincentrum toe rijden](- "#text1")" and "[daar in dat tuincentrum een plant bevoelen](- "#text2")", 
are [POST](- "#versions=upload(#fileId1, #text1, #text2)")ed,
then we should have a [valid versions](- "?=#versions.validVersions"):

  - [ ](- "c:echo=#versions.versionUuid1")
  - [ ](- "c:echo=#versions.versionUuid2")

Find all ES docs using query:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [text](- "#fileType")

[ ](- "#result=searchFileIndexWithVersions()")

- The response status should be: [200](- "?=#result.status");
- The response should contain [correct file](- "?=#result.found");
- The response should contain [2](- "?=#result.versionCount") versions;

## Index when updating file type

Files are re-indexed when file type is changed.

When updating file [ ](- "c:echo=#fileId1") with a `PUT` to [/rest/files/{id}](- "#updateEndpoint"):

[{
"docId": "{docId}", "typeId": {typeId}
}](- "#updatedEntity")


[ ](- "#updateResult=update(#updateEndpoint, #fileId1, #updatedEntity, #docId1, getFooTypeId())")

Then:

- The response status should be: [200](- "?=#updateResult.status");
- The response should contain the [updated type](- "?=#updateResult.updatedType");

Find all ES docs using query:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [foo](- "#fileType")

[ ](- "#result=searchFileIndexWithType(#fileType)")

- The response status should be: [200](- "?=#result.status");
- The response should contain [correct file](- "?=#result.found");
- The response should contain [correct type](- "?=#result.type");

## Index when deleting latest version

Files are re-indexed when latest version is deleted.

When deleting latest version [ ](- "c:echo=#versions.versionUuid2") with a `DELETE` to [/rest/versions/{id}](- "#deleteVersionEndpoint"):

[ ](- "#deleteResult=deleteVersion(#deleteVersionEndpoint, #versions.versionUuid2)")

Then:

- The response status should be: [204](- "?=#deleteResult.status").

Find all ES docs using query:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [foo](- "#fileType")

[ ](- "#result=searchFileIndexWithType(#fileType)")

- The response status should be: [200](- "?=#result.status");
- The response should contain [correct file](- "?=#result.found");
- The response should contain [1](- "?=#result.versionCount") version;

## Index when deleting file

Deleted files are removed from indices.

When deleting file [ ](- "c:echo=#fileId1") with a `DELETE` to [/rest/files/{id}](- "#deleteFileEndpoint"):

[ ](- "#deleteResult=deleteFile(#deleteFileEndpoint, #fileId1)")

Then:

- The response status should be: [204](- "?=#deleteResult.status").

Find all ES docs using query:

[ ](- "ext:embed=getEsQuery()")

- where `{type}` is [foo](- "#fileType")

[ ](- "#result=searchEmptyFileIndex()")

- The response status should be: [200](- "?=#result.status");
- The response should contain [no files](- "?=#result.found");

