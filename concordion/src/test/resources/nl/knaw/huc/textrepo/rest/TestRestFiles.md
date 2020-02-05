# Test /rest/files

## Create, retrieve, update and delete files

### Set up

[ ](- "#docId=createDocument()")
[ ](- "#typeId=getTypeId()")
[ ](- "#fooTypeId=getFooTypeId()")

To add file metadata we created a file first: [ ](- "c:echo=#docId")

### Create file
When creating the following file with a `POST` to [`/rest/files`](- "#createEndpoint"):

[```{
  "docId": "{docId}", "typeId": {typeId}
}```](- "#newEntity")

[ ](- "#createResult=create(#createEndpoint, #newEntity, #docId, #typeId)")

Then:

 - The response status should be: [200](- "?=#createResult.status");
 - The response should contain a [valid UUID](- "?=#createResult.validUuid");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Retrieve file
When reading the following file with a `GET` to [`/rest/files/{id}`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#createResult.id"):

[ ](- "#readResult=read(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#readResult.status");
 - The response should contain a [valid UUID](- "?=#readResult.validUuid");
 - The response should contain a [correct type](- "?=#readResult.correctType");
 - Full response:

[ ](- "ext:embed=#readResult.body")

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
When reading file [ ](- "c:echo=#createResult.id") with a `GET` to [`/rest/files/{id}`](- "#getEndpoint"):

[ ](- "#readAfterDeleteResult=getAfterDelete(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [404](- "?=#readAfterDeleteResult.status").

