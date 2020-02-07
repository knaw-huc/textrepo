# Test `/rest/file/{id}/metadata`

## Create, retrieve, update and delete file metadata

### Set up

[ ](- "createDocument()")
[ ](- "#fileId=createFile()")

To add file metadata we first create: 
  
  - a document;
  - and a file: [ ](- "c:echo=#fileId").

### Create file metadata
When adding the following file metadata with a `PUT` to [`/rest/files/{id}/metadata/{key}`](- "#createEndpoint")

 - where key is [`test-key`](- "#metadataKey");
 - where value is [`test-value`](- "#metadataValue").

[ ](- "#createResult=create(#createEndpoint, #fileId, #metadataKey, #metadataValue)")

Then:

 - The response status should be: [200](- "?=#createResult.status");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Retrieve file metadata
When retrieving the metadata of a file with a `GET` to [`/rest/files/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#fileId"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #fileId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain the value [test-value](- "?=#retrieveResult.value");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Update file metadata entry
When updating metadata entry with a `PUT` to [`/rest/files/{id}/metadata/{key}`](- "#updateEndpoint"):

 - where key is [`test-key`](- "#metadataKey")
 - where value is [`updated-test-value`](- "#updatedMetadataValue")

[ ](- "#updateResult=update(#updateEndpoint, #fileId, #metadataKey, #updatedMetadataValue)")
Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should its updated value [updated-test-value](- "?=#updateResult.value");
 - Full response:

[ ](- "ext:embed=#updateResult.body")

### Retrieve file metadata after updating entry
When retrieving the metadata of a file with a `GET` to [`/rest/files/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#fileId"):

[ ](- "#updatedReadResult=retrieve(#getEndpoint, #fileId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#updatedReadResult.status");
 - The response should contain the value [updated-test-value](- "?=#updatedReadResult.value");
 - Full response:

[ ](- "ext:embed=#updatedReadResult.body")

### Delete file metadata entry
When removing a metadata entry with a `DELETE` to [`/rest/files/{id}/metadata/{key}`](- "#deleteEndpoint"):

 - where `{id}` is [ ](- "c:echo=#fileId")
 - where `{key}` is [ ](- "c:echo=#metadataKey")

[ ](- "#deleteResult=delete(#deleteEndpoint, #fileId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

### Retrieve file metadata after deleting entry
When retrieving file metadata with a `GET` to [`/rest/files/{id}/metadata`](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=retrieveAfterDelete(#getEndpoint, #fileId)")

Then:

 - The response status should be: [200](- "?=#retrieveAfterDeleteResult.status").
 - File metadata should be empty: [{}](- "?=#retrieveAfterDeleteResult.body").

