# Test `/rest/files`

A document contains files, no more than one file per type.

[ ](- "#docId=createDocument()")
[ ](- "#typeId=getTextTypeId()")
[ ](- "#fooTypeId=getFooTypeId()")

To add a file we create a document first: [ ](- "c:echo=#docId")

### Create file
When creating the following file with a `POST` to [`/rest/files`](- "#createEndpoint"):

[```{
  "docId": "{docId}", "typeId": {typeId}
}```](- "#newEntity")

[ ](- "#createResult=create(#createEndpoint, #newEntity, #docId, #typeId)")

Then:

 - The response status should be: [201](- "?=#createResult.status");
 - The response should contain a [valid UUID](- "?=#createResult.validUuid");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Retrieve file
When retrieving the following file with a `GET` to [`/rest/files/{id}`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#createResult.id"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain a [valid UUID](- "?=#retrieveResult.validUuid");
 - The response should contain a [correct type](- "?=#retrieveResult.correctType");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Update file
When updating file [ ](- "c:echo=#createResult.id") with a `PUT` to [`/rest/files/{id}`](- "#updateEndpoint"):

[```{
  "docId": "{docId}", "typeId": {typeId}
}```](- "#updatedEntity")


[ ](- "#updateResult=update(#updateEndpoint, #createResult.id, #updatedEntity, #docId, #fooTypeId)")

Then:

 - The response status should be: [200](- "?=#updateResult.status");
 - The response should contain the [updated type](- "?=#updateResult.updatedType");
 - Full response:

[ ](- "ext:embed=#updateResult.body")

### Delete file
When deleting file [ ](- "c:echo=#createResult.id") with a `DELETE` to [`/rest/files/{id}`](- "#deleteEndpoint"):

[ ](- "#deleteResult=delete(#deleteEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

### Retrieve file after deleting
When retrieving file [ ](- "c:echo=#createResult.id") with a `GET` to [`/rest/files/{id}`](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=getAfterDelete(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [404](- "?=#retrieveAfterDeleteResult.status").

