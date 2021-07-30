# Test `/rest/versions/{id}/contents`

A version has content.

To retrieve the contents of a version we first create: 
  
[ ](- "#docId=createDocument()")
[ ](- "#fileId=createFile(#docId)")
[ ](- "#versionId=createVersion(#fileId)")

  - a document;
  - a file;
  - and a version: [ ](- "c:echo=#versionId").

## Retrieve contents
When retrieving the contents of a file with a `GET` to [/rest/versions/{id}/contents](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#versionId"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #versionId)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain [some scrumptious content](- "?=#retrieveResult.contents");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

## Delete version and its contents
When deleting version [ ](- "c:echo=#versionId") with a `DELETE` to [/rest/versions/{id}](- "#deleteEndpoint"):

[ ](- "#deleteResult=delete(#deleteEndpoint, #versionId)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

## Retrieve contents after version is deleted
When retrieving the contents of a file with a `GET` to [/rest/versions/{id}/contents](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#versionId"):

[ ](- "#retrieveResultAfterDelete=retrieve(#getEndpoint, #versionId)")

Then:

 - The response status should be: [404](- "?=#retrieveResultAfterDelete.status");

