# Test `/rest/version/{id}/metadata`

A version has metadata: a map of key-value pairs. 

To add version metadata we first create: 

[ ](- "createDocument()")
[ ](- "#fileId=createFile()")
[ ](- "#versionId=createVersion()")

  - a document;
  - a file;
  - and a version: [ ](- "c:echo=#versionId").

### Create version metadata
When adding the following version metadata with a `PUT` to [`/rest/versions/{id}/metadata/{key}`](- "#createEndpoint")

 - where key is [`test-key`](- "#metadataKey");
 - where value is [`test-value`](- "#metadataValue").

[ ](- "#createResult=create(#createEndpoint, #versionId, #metadataKey, #metadataValue)")

Then:

 - The response status should be: [200](- "?=#createResult.status");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Retrieve version metadata
When retrieving the metadata of a version with a `GET` to [`/rest/versions/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#versionId"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #versionId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain the value [test-value](- "?=#retrieveResult.value");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Update version metadata entry
When updating metadata entry with a `PUT` to [`/rest/versions/{id}/metadata/{key}`](- "#updateEndpoint"):

 - where key is [`test-key`](- "#metadataKey")
 - where value is [`updated-test-value`](- "#updatedMetadataValue")

[ ](- "#updateResult=update(#updateEndpoint, #versionId, #metadataKey, #updatedMetadataValue)")
Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should its updated value [updated-test-value](- "?=#updateResult.value");
 - Full response:

[ ](- "ext:embed=#updateResult.body")

### Retrieve version metadata after updating entry
When retrieving the metadata of a version with a `GET` to [`/rest/versions/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#versionId"):

[ ](- "#updatedReadResult=retrieve(#getEndpoint, #versionId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#updatedReadResult.status");
 - The response should contain the value [updated-test-value](- "?=#updatedReadResult.value");
 - Full response:

[ ](- "ext:embed=#updatedReadResult.body")

### Delete version metadata entry
When removing a metadata entry with a `DELETE` to [`/rest/versions/{id}/metadata/{key}`](- "#deleteEndpoint"):

 - where `{id}` is [ ](- "c:echo=#versionId")
 - where `{key}` is [ ](- "c:echo=#metadataKey")

[ ](- "#deleteResult=delete(#deleteEndpoint, #versionId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

### Retrieve version metadata after deleting entry
When retrieving version metadata with a `GET` to [`/rest/versions/{id}/metadata`](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=retrieveAfterDelete(#getEndpoint, #versionId)")

Then:

 - The response status should be: [200](- "?=#retrieveAfterDeleteResult.status").
 - Version metadata should be empty: [{}](- "?=#retrieveAfterDeleteResult.body").

