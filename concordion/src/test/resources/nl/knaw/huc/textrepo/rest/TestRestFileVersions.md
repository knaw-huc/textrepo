# Test `/rest/file/{id}/versions`

A file can have multiple versions.

To view all versions of a file we first create: 

[ ](- "#docId=createDocument()")
[ ](- "#fileId=createFile(#docId)")
[ ](- "#oldVersionId=createVersion(#fileId)")
[ ](- "#newVersionId=createVersion(#fileId)")

  - a document;
  - a file: [ ](- "ext:embed=code(#fileId)");
  - an old version: [ ](- "ext:embed=code(#oldVersionId)");
  - and a new version: [ ](- "ext:embed=code(#newVersionId)").

### Retrieve file versions
When retrieving the versions of a file with a `GET` to [`/rest/files/{id}/versions`](- "#getEndpoint") 

 - where `{id}` is [ ](- "ext:embed=code(#fileId)"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #fileId, #oldVersionId, #newVersionId)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain both the [old and new](- "?=#retrieveResult.twoVersions") version;
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Paginate file versions
Results are divided in pages using offset and limit.

When retrieving the versions of a file with a `GET` to [`/rest/files/{id}/versions?offset={offset}&limit={limit}`](- "#getEndpoint") 

 - where `{id}` is [ ](- "ext:embed=code(#fileId)");
 - where `{offset}` is [`0`](- "#offset") and `{limit}` is [`1`](- "#limit").

[ ](- "#paginateResult=paginate(#getEndpoint, #fileId, #offset, #limit, #oldVersionId)")

Then:

 - The response status should be: [200](- "?=#paginateResult.status");
 - The response should only contain the [old](- "?=#paginateResult.hasOld") version;
 - Total should be [2](- "?=#paginateResult.total");
 - Full response:

[ ](- "ext:embed=#paginateResult.body")


