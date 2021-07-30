# Test `/task/find/{externalId}/file/metadata?type={name}`

File metadata can be retrieved with the `find` task, an external ID and file type.

To retrieve file metadata we first create: 

  - a document with external ID: [test-external-id](- "#externalId");
  - with a file of type: [text](- "#fileType");
  - and some file metadata with key [testKey](- "#key") and value [testValue](- "#value").

[ ](- "#docId=createDocument(#externalId)")
[ ](- "#fileId=createFile(#docId)")
[ ](- "createMetadata(#fileId, #key, #value)")

### Retrieve file metadata
When retrieving the metadata of a file with a `GET` to [/task/find/{externalId}/file/metadata?type={name}](- "#findEndpoint") 

 - where `{externalId}` is [ ](- "c:echo=#externalId");
 - where type `{name}` is [ ](- "c:echo=#fileType");

[ ](- "#retrieveResult=retrieve(#findEndpoint, #externalId, #fileType, #key)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - Metadata key `testKey` should have value [testValue](- "?=#retrieveResult.value");
 - Headers should contain link to [original resource](- "?=#retrieveResult.original");
 - Headers should contain link to [parent resource](- "?=#retrieveResult.parent");
 - Headers should contain link to [type resource](- "?=#retrieveResult.type");
 - Link headers:

[ ](- "ext:embed=#retrieveResult.headers")

 - Full response:

[ ](- "ext:embed=#retrieveResult.body")
