# Test `/task/index/type/{type}`

All files can be indexed and reindexed by type. 

To index, we first create three documents, each with one file and version, using the import task, which does not index: 

 - using type: [`text`](- "#type");

[ ](- "#importResult=importFiles(#type)")

Then:

 - Response statuses should be: [201, 201, 201](- "?=#importResult.status");
 - Full response of the first imported document:

[ ](- "ext:embed=#importResult.body")

### Indexing files
When indexing the files with a `POST` to [`/task/index/type/{type}`](- "#indexEndpoint") 

 - where `{type}` is [ ](- "c:echo=#type");

[ ](- "#retrieveResult=indexFilesBy(#indexEndpoint, #type)")

Then:

 - Response status should be: [200](- "?=#retrieveResult.status");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### Checking index
When searching the autocomplete index:

[ ](- "#searchResult=searchFullText()")

Then:

 - The response status should be: [200](- "?=#searchResult.status");
 - The document count should be: [3](- "?=#searchResult.count");
 - The contents should contain: [beunhaas, dakhaas, zandhaas](- "?=#searchResult.contents");
 - Full response:

[ ](- "ext:embed=#searchResult.body")
