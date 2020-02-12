# Test `/rest/file/{id}/versions`

## Retrieve file versions

### Set up

[ ](- "#docId=createDocument()")
[ ](- "#fileId=createFile(#docId)")
[ ](- "#oldVersionId=createVersion(#fileId)")
[ ](- "#newVersionId=createVersion(#fileId)")

A file can have multiple versions.
To view all versions of a file we first create: 
  
  - a document;
  - a file: [ ](- "c:echo=#fileId");
  - an old version: [ ](- "c:echo=#oldVersionId");
  - and a new version: [ ](- "c:echo=#newVersionId").

### Retrieve file versions
When retrieving the versions of a file with a `GET` to [`/rest/files/{id}/versions`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#fileId"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #fileId)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain [two versions](- "?=#retrieveResult.twoVersions");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")


