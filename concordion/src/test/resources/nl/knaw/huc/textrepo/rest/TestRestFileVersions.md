# Test `/rest/file/{id}/versions`

A file can have multiple versions.

To view all versions of a file we first create: 

[ ](- "#docId=createDocument()")
[ ](- "#fileId=createFile(#docId)")
[ ](- "#oldVersionId=createVersion(#fileId)")
[ ](- "#newVersionId=createVersion(#fileId)")

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
 - The response should contain [2](- "?=#retrieveResult.twoVersions") versions;
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Paginate file versions
When retrieving the versions of a file with a `GET` to [`/rest/files/{id}/versions`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#fileId");
 - where query parameter `limit` is [`1`](- "#limit") and `offset` is [`0`](- "#offset").

[ ](- "#paginateResult=paginate(#getEndpoint, #fileId, #offset, #limit)")

Then:

 - The response status should be: [200](- "?=#paginateResult.status");
 - The response should contain [1](- "?=#paginateResult.itemCount") version;
 - Total should be [2](- "?=#paginateResult.total");
 - Full response:

[ ](- "ext:embed=#paginateResult.body")


