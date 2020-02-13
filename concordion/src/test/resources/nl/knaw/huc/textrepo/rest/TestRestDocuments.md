# Test `/rest/documents`

Document is the top level entity in the text repository. 

A document can represent any physical document like a page or multiple pages, and contains all text files (plain text, xml, etc.) that describe this document.  

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

### Retrieve document
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

### Retrieve document after deleting
When retrieving document [ ](- "c:echo=#createResult.id") with a `GET` to [`/rest/documents/{id}`](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=getAfterDelete(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [404](- "?=#retrieveAfterDeleteResult.status").

