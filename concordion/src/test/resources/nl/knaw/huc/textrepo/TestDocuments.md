# TestDocuments

## [Create, get, update and delete documents](- 'create')

### Create document
When creating the following document with a `POST` to [`/rest/documents`](- "#createDocumentEndpoint"):

[```{
  "externalId": "test-external-id"
}```](- "#newEntity")

[ ](- "#createResult=create(#createDocumentEndpoint, #newEntity)")

Then:

 - The response status should be: [200](- "?=#createResult.status");
 - The following response should contain a [valid UUID](- "?=#createResult.validUuid"):

[ ](- "ext:embed=#createResult.body")

### Get document
When reading the following document with a `GET` to [`/rest/documents/{id}`](- "#getDocumentEndpoint") 
where `{id}` is [ ](- "c:echo=#createResult.docId"):

[ ](- "#readResult=read(#getDocumentEndpoint, #createResult.docId)")

Then:

 - The response status should be: [200](- "?=#readResult.status");
 - The response should contain a [valid UUID](- "?=#readResult.validUuid"),
 - And the response should contain an external ID [test-external-id](- "?=#readResult.externalId"):

[ ](- "ext:embed=#readResult.body")

