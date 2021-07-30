# Test `/rest/documents/{id}/files`

A document can contain multiple types, and only one file per type. 

To retrieve all files of a document we first create:

[ ](- "createDocumentWithTwoFiles()")

 - a document: [ ](- "c:echo=getDocId()");
 - with a file of type `text`: [ ](- "c:echo=getTextFileId()");
 - and a file of type `foo`: [ ](- "c:echo=getFooFileId()").

### Retrieve files of a document
When retrieving the following file with a `GET` to [/rest/documents/{id}/files](- "#getEndpoint") 

 - where `{id}` is [ ](- "c:echo=getDocId()"):

[ ](- "#retrieveResult=retrieve(#getEndpoint, getDocId())")

Then:

 - The response status should be: [200](- "?=#retrieveResult.status");
 - The response should contain [2](- "?=#retrieveResult.count") entries;
 - One [text](- "?=#retrieveResult.type1") file;
 - And one [foo](- "?=#retrieveResult.type2") file;
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")


### Paginate document files
Results are divided in pages using offset and limit.

When retrieving the versions of a file with a `GET` to [/rest/documents/{id}/files?offset={offset}&limit={limit}](- "#getEndpoint") 

 - where `{id}` is [ ](- "ext:embed=code(getDocId())");
 - where `{offset}` is [0](- "#offset") and `{limit}` is [1](- "#limit").

[ ](- "#paginateResult=paginate(#getEndpoint, getDocId(), #offset, #limit)")

Then:

 - The response status should be: [200](- "?=#paginateResult.status");
 - The response should only contain [1](- "?=#paginateResult.size") file;
 - Total should be [2](- "?=#paginateResult.total");
 - Full response:

[ ](- "ext:embed=#paginateResult.body")

### Filter by file type
Results can be filtered by type.

When retrieving the versions of a file with a `GET` to [/rest/documents/{id}/files?typeId={typeId}](- "#getEndpoint") 

 - where `{id}` is [ ](- "ext:embed=code(getDocId())");
 - where `{typeId}` is [ ](- "ext:embed=code(getTextTypeId())");

[ ](- "#paginateResult=filter(#getEndpoint, getDocId(), getTextTypeId(), getTextFileId())")

Then:

 - The response status should be: [200](- "?=#paginateResult.status");
 - The response should only contain the [text](- "?=#paginateResult.hasOld") file;
 - Total should be [1](- "?=#paginateResult.total");
 - Full response:

[ ](- "ext:embed=#paginateResult.body")
