# Test `/task/find/{externalId}/document/metadata`

Document metadata can be retrieved with the `find` task and an external ID.

To find document metadata we first create: 

  - a document with external ID: [`test-external-id`](- "#externalId");
  - and some metadata with key [`testKey`](- "#key") and value [`testValue`](- "#value").

[ ](- "#docId=createDocument(#externalId)")
[ ](- "createMetadata(#docId, #key, #value)")

### Retrieve document metadata
When retrieving the metadata of a document with a `GET` to [`/task/find/{externalId}/document/metadata`](- "#findEndpoint") 

 - where `{externalId}` is [ ](- "c:echo=#externalId"):

[ ](- "#retrieveResult=retrieve(#findEndpoint, #docId, #externalId, #key)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - Metadata key `testKey` should have value [`testValue`](- "?=#retrieveResult.value");
 - Headers should contain link to [original resource](- "?=#retrieveResult.original");
 - Headers should contain link to [parent resource](- "?=#retrieveResult.parent");
 - Link headers:

[ ](- "ext:embed=#retrieveResult.headers")

 - Full response:

[ ](- "ext:embed=#retrieveResult.body")
