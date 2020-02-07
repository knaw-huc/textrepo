# Test `/rest/documents`

## Create, retrieve, update and delete documents

### Create document
When creating the following document with a `POST` to [`/rest/documents`](- "#createEndpoint"):

[```{
  "externalId": "test-external-id"
}```](- "#newEntity")

[ ](- "#createResult=create(#createEndpoint, #newEntity)")

Then:

 - The response status should be: [200](- "?=#createResult.status");
 - The response should contain a [valid UUID](- "?=#createResult.validUuid");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Get document
When retrieving the following document with a `GET` to [`/rest/documents/{id}`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#createResult.id"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain a [valid UUID](- "?=#retrieveResult.validUuid");
 - The response should contain an external ID [test-external-id](- "?=#retrieveResult.externalId");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Update document
When updating document [ ](- "c:echo=#createResult.id") with a `PUT` to [`/rest/documents/{id}`](- "#updateEndpoint"):

[```{
  "externalId": "updated-test-external-id"
}```](- "#updatedEntity")


[ ](- "#updateResult=update(#updateEndpoint, #createResult.id, #updatedEntity)")

Then:

 - The response status should be: [200](- "?=#updateResult.status");
 - The response should its updated external ID [updated-test-external-id](- "?=#updateResult.externalId");
 - Full response:

[ ](- "ext:embed=#updateResult.body")

### Delete document
When deleting document [ ](- "c:echo=#createResult.id") with a `DELETE` to [`/rest/documents/{id}`](- "#deleteEndpoint"):

[ ](- "#deleteResult=delete(#deleteEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

### Get document after deleting
When retrieving document [ ](- "c:echo=#createResult.id") with a `GET` to [`/rest/documents/{id}`](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=getAfterDelete(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [404](- "?=#retrieveAfterDeleteResult.status").

