# Test /rest/document/{id}/metadata

## Create, retrieve, update and delete document metadata

### Set up

[ ](- "#docId=createDocument()")

To add document metadata we created a document first: [ ](- "c:echo=#docId")

### Create document metadata
When adding the following document metadata with a `PUT` to [`/rest/documents/{id}/metadata/{key}`](- "#createEndpoint")

 - where key is [`test-key`](- "#metadataKey")
 - where value is [`test-value`](- "#metadataValue")

[ ](- "#createResult=create(#createEndpoint, #docId, #metadataKey, #metadataValue)")

Then:

 - The response status should be: [200](- "?=#createResult.status");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Retrieve document metadata
When reading the metadata of a document with a `GET` to [`/rest/documents/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#docId"):

[ ](- "#readResult=read(#getEndpoint, #docId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#readResult.status");
 - The response should contain the value [test-value](- "?=#readResult.value");
 - Full response:

[ ](- "ext:embed=#readResult.body")

### Update document metadata entry
When updating metadata entry with a `PUT` to [`/rest/documents/{id}/metadata/{key}`](- "#updateEndpoint"):

 - where key is [`test-key`](- "#metadataKey")
 - where value is [`updated-test-value`](- "#updatedMetadataValue")

[ ](- "#updateResult=update(#updateEndpoint, #docId, #metadataKey, #updatedMetadataValue)")
Then:

 - The response status should be: [200](- "?=#readResult.status");
 - The response should its updated value [updated-test-value](- "?=#updateResult.value");
 - Full response:

[ ](- "ext:embed=#updateResult.body")

### Retrieve document metadata after updating entry
When reading the metadata of a document with a `GET` to [`/rest/documents/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#docId"):

[ ](- "#updatedReadResult=read(#getEndpoint, #docId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#updatedReadResult.status");
 - The response should contain the value [updated-test-value](- "?=#updatedReadResult.value");
 - Full response:

[ ](- "ext:embed=#updatedReadResult.body")

### Delete document metadata entry
When removing a metadata entry with a `DELETE` to [`/rest/documents/{id}/metadata/{key}`](- "#deleteEndpoint"):

 - where `{id}` is [ ](- "c:echo=#docId")
 - where `{key}` is [ ](- "c:echo=#metadataKey")

[ ](- "#deleteResult=delete(#deleteEndpoint, #docId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

### Retrieve document metadata after deleting entry
When reading document metadata with a `GET` to [`/rest/documents/{id}/metadata`](- "#getEndpoint"):

[ ](- "#readAfterDeleteResult=getAfterDelete(#getEndpoint, #docId)")

Then:

 - The response status should be: [200](- "?=#readAfterDeleteResult.status").
 - Document metadata should be empty: [{}](- "?=#readAfterDeleteResult.body").

