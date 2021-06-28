# Test `/rest/versions`

A file can have multiple versions.

To add a version we first create:
 
[ ](- "#docId=createDocument()")
[ ](- "#fileId=createFile(#docId)")

 - a document: [ ](- "c:echo=#docId")
 - a file: [ ](- "c:echo=#fileId")

## Create version
When creating the following version with a `POST` to [`/rest/versions`](- "#createEndpoint"):

[```new content```](- "#newContent")

[ ](- "#createResult=create(#createEndpoint, #newContent, #fileId)")

Then:

 - The response status should be: [201](- "?=#createResult.status");
 - The response should contain a [valid UUID](- "?=#createResult.validUuid");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

## Retrieve version
When retrieving the following version with a `GET` to [`/rest/versions/{id}`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#createResult.id"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain a [valid UUID](- "?=#retrieveResult.validUuid");
 - The response should contain a [valid sha224](- "?=#retrieveResult.validSha");
 - The response should contain a [valid timestamp](- "?=#retrieveResult.validTimestamp");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

## Update version
When updating version [ ](- "c:echo=#createResult.id") with a `PUT` to [`/rest/versions/{id}`](- "#updateEndpoint"):

[ ](- "#updateResult=update(#createEndpoint, #newContent, #createResult.id)")

Then:

 - The response status should be: [405](- "?=#updateResult.status") - Method not allowed;
 - Full response:

[ ](- "ext:embed=#updateResult.body")

## Delete version
When deleting version [ ](- "c:echo=#createResult.id") with a `DELETE` to [`/rest/versions/{id}`](- "#deleteEndpoint"):

[ ](- "#deleteResult=delete(#deleteEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

## Retrieve version after deleting
When retrieving version [ ](- "c:echo=#createResult.id") with a `GET` to [`/rest/versions/{id}`](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=getAfterDelete(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [404](- "?=#retrieveAfterDeleteResult.status").

