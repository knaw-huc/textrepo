# Test `/task/get/{externalId}/document/metadata`

Document metadata can be retrieved with the `get` task and an external ID.

To retrieve document metadata we first create: 

  - a document with external ID: [`test-external-id`](- "#externalId");
  - and some metadata with key [`testKey`](- "#key") and value [`testValue`](- "#value").

[ ](- "#docId=createDocument(#externalId)")
[ ](- "createMetadata(#docId, #key, #value)")

### Retrieve document metadata
When retrieving the metadata of a document with a `GET` to [`/task/get/{externalId}/document/metadata`](- "#getEndpoint") 

 - where `{externalId}` is [ ](- "c:echo=#externalId"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #externalId, #key)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - Metadata key `testKey` should have value [`testValue`](- "?=#retrieveResult.value");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")