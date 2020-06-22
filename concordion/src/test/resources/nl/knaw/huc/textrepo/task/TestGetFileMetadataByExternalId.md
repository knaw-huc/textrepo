# Test `/task/get/{externalId}/file/metadata?type={name}`

File metadata can be retrieved with the `get` task and an external ID.

To retrieve file metadata we first create: 

  - a document with external ID: [`test-external-id`](- "#externalId");
  - with a file of type: [`text`](- "#fileType");
  - and some file metadata with key [`testKey`](- "#key") and value [`testValue`](- "#value").

[ ](- "#docId=createDocument(#externalId)")
[ ](- "#fileId=createFile(#docId)")
[ ](- "createMetadata(#fileId, #key, #value)")

### Retrieve document metadata
When retrieving the metadata of a file with a `GET` to [`/task/get/{externalId}/file/metadata?type={typeName}`](- "#getEndpoint") 

 - where `{externalId}` is [ ](- "c:echo=#externalId");
 - where `{typeName}` is [ ](- "c:echo=#fileType");

[ ](- "#retrieveResult=retrieve(#getEndpoint, #externalId, #fileType, #key)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - Metadata key `testKey` should have value [`testValue`](- "?=#retrieveResult.value");
 - Headers should contain link to [original resource](- "?=#retrieveResult.original");
 - Headers should contain link to [parent resource](- "?=#retrieveResult.parent");
 - Headers should contain link to [type resource](- "?=#retrieveResult.type");
 - Headers:

[ ](- "ext:embed=#retrieveResult.headers")

 - Full response:

[ ](- "ext:embed=#retrieveResult.body")
