# Test `/rest/versions/{id}/contents`

## Retrieve version contents

### Set up

[ ](- "#docId=createDocument()")
[ ](- "#fileId=createFile(#docId)")
[ ](- "#versionId=createVersion(#fileId)")

A version has content.
To retrieve the contents of a version we first create: 
  
  - a document;
  - a file;
  - and a version: [ ](- "c:echo=#versionId");

### Retrieve file versions
When retrieving the contents of a file with a `GET` to [`/rest/versions/{id}/contents`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#versionId"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #versionId)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain [some scrumptious content](- "?=#retrieveResult.contents");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")


