# Test `/rest/document/{id}/metadata`

## Create, retrieve, update and delete document metadata

[ ](- "#docId=createDocument()")

A document has metadata: a map of key-value pairs. 
To add document metadata we created a document first: [ ](- "c:echo=#docId")

### Create document metadata
When adding the following document metadata with a `PUT` to [`/rest/documents/{id}/metadata/{key}`](- "#createEndpoint")

 - where key is [`test-key`](- "#metadataKey");
 - and value is [`test-value`](- "#metadataValue").

[ ](- "#createResult=create(#createEndpoint, #docId, #metadataKey, #metadataValue)")

Then:

 - The response status should be: [200](- "?=#createResult.status");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Retrieve document metadata
When retrieving the metadata of a document with a `GET` to [`/rest/documents/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#docId"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #docId, #metadataKey)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain the value [test-value](- "?=#retrieveResult.value");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Update document metadata entry
When updating metadata entry with a `PUT` to [`/rest/documents/{id}/metadata/{key}`](- "#updateEndpoint"):

 - where key is [`test-key`](- "#metadataKey")
 - where value is [`updated-test-value`](- "#updatedMetadataValue")

[ ](- "#updateResult=update(#updateEndpoint, #docId, #metadataKey, #updatedMetadataValue)")
Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should its updated value [updated-test-value](- "?=#updateResult.value");
 - Full response:

[ ](- "ext:embed=#updateResult.body")

### Retrieve document metadata after updating entry
When retrieving the metadata of a document with a `GET` to [`/rest/documents/{id}/metadata`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#docId"):

[ ](- "#updatedReadResult=retrieve(#getEndpoint, #docId, #metadataKey)")

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
When retrieving document metadata with a `GET` to [`/rest/documents/{id}/metadata`](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=retrieveAfterDelete(#getEndpoint, #docId)")

Then:

 - The response status should be: [200](- "?=#retrieveAfterDeleteResult.status").
 - Document metadata should be empty: [{}](- "?=#retrieveAfterDeleteResult.body").

