# Test `/rest/contents/{sha}`

## Retrieve version contents

[ ](- "#docId=createDocument()")
[ ](- "#fileId=createFile(#docId)")
[ ](- "#versionId=createVersion(#fileId)")
[ ](- "#sha=getSha(#versionId)")

Every version has a sha of its contents. To retrieve the contents of a version by its sha we first create: 
  
  - a document;
  - a file;
  - and a version: [ ](- "c:echo=#versionId") 
  - with a sha: [ ](- "c:echo=#sha").

### Retrieve contents by its sha
When retrieving the contents of a file with a `GET` to [`/rest/contents/{sha}`](- "#getEndpoint") 

 - where `{sha}` is [ ](- "c:echo=#sha"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #sha)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain [some scrumptious content](- "?=#retrieveResult.contents");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")
