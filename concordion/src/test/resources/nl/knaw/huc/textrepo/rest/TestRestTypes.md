# Test `/rest/types`

Every file has a type and a document contains only one file per type. 

### Create type
When creating the following type with a `POST` to [/rest/types](- "#createEndpoint"):

[{ "name": "test-name", "mimetype": "test-mimetype" }](- "#newEntity")

[ ](- "#createResult=create(#createEndpoint, #newEntity)")

Then:

 - The response status should be: [201](- "?=#createResult.status");
 - The response should contain a [valid ID](- "?=#createResult.hasId");
 - Full response:
 
[ ](- "ext:embed=#createResult.body")

### Retrieve type
When retrieving the following type with a `GET` to [/rest/types/{id}](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=#createResult.id"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain a [valid ID](- "?=#retrieveResult.hasId");
 - The response should contain a name [test-name](- "?=#retrieveResult.name");
 - The response should contain a name [test-mimetype](- "?=#retrieveResult.mimetype");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Update type
When updating type [ ](- "c:echo=#createResult.id") with a `PUT` to [/rest/types/{id}](- "#updateEndpoint"):

[{ "name": "updated-test-name", "mimetype": "updated-test-mimetype" }](- "#updatedEntity")

[ ](- "#updateResult=update(#updateEndpoint, #createResult.id, #updatedEntity)")

Then:

 - The response status should be: [200](- "?=#updateResult.status");
 - Full response:

[ ](- "ext:embed=#updateResult.body")

### Retrieve type after updating
When retrieving type [ ](- "c:echo=#createResult.id") with a `GET` to [/rest/types/{id}](- "#getEndpoint"):

[ ](- "#getAfterUpdateResult=getAfterUpdate(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#getAfterUpdateResult.status");
 - The response should contain a [valid ID](- "?=#getAfterUpdateResult.hasId");
 - The response should contain a name [updated-test-name](- "?=#getAfterUpdateResult.name");
 - The response should contain a name [updated-test-mimetype](- "?=#getAfterUpdateResult.mimetype");
 - Full response:

[ ](- "ext:embed=#getAfterUpdateResult.body")

### Delete type
When deleting type [ ](- "c:echo=#createResult.id") with a `DELETE` to [/rest/types/{id}](- "#deleteEndpoint"):

[ ](- "#deleteResult=delete(#deleteEndpoint, #createResult.id)")

Then:

 - The response status should be: [200](- "?=#deleteResult.status").

### Retrieve type after deleting
When retrieving type [ ](- "c:echo=#createResult.id") with a `GET` to [/rest/types/{id}](- "#getEndpoint"):

[ ](- "#retrieveAfterDeleteResult=getAfterDelete(#getEndpoint, #createResult.id)")

Then:

 - The response status should be: [404](- "?=#retrieveAfterDeleteResult.status").

