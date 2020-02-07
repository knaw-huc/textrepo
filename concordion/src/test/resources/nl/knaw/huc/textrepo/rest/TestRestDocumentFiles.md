# Test `/rest/documents/{id}/files`

## Retrieve files of a document

### Set up

[ ](- "createDocumentWithTwoFiles()")

To retrieve all files of a document we first create:

 - a document: [ ](- "c:echo=getDocId()");
 - with a file of type `text`: [ ](- "c:echo=getTextFileId()");
 - and a file of type `foo`: [ ](- "c:echo=getFooFileId()").

### Retrieve files of a document
When retrieving the following file with a `GET` to [`/rest/documents/{id}/files`](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=getDocId()"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, getDocId())")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain [2](- "?=#retrieveResult.count") entries;
 - One [text](- "?=#retrieveResult.type1") file;
 - And one [foo](- "?=#retrieveResult.type2") file;
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

