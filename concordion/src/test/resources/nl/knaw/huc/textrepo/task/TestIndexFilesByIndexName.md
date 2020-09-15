# Test `/task/index/{name}`

All files can be indexed and reindexed by index name. The other indices will not be touched.

To index, we first create three documents, each with one file and version, using the import task, which does not index: 

 - using type: [`text`](- "#type");

[ ](- "#importResult=importDocs(#type)")

Then:

 - Response statuses should be: [200, 200, 200](- "?=#importResult.status");
 - Full response of first imported document:

[ ](- "ext:embed=#importResult.body")

### Indexing by index name
When indexing the files with a `POST` to [`/task/index/{name}`](- "#indexEndpoint") 

 - where `{name}` is [`autocomplete`](- "#indexName");

[ ](- "#retrieveResult=indexFilesBy(#indexEndpoint, #indexName)")

Then:

 - Response status should be: [200](- "?=#retrieveResult.status");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### The named index should be updated
When searching the autocomplete index:

[ ](- "#searchResult=searchAutocompleteIndex()")

Then:

 - The response status should be: [200](- "?=#searchResult.status");
 - The document count should be: [3](- "?=#searchResult.count");
 - The contents should contain: [beunhaas, dakhaas, zandhaas](- "?=#searchResult.contents");
 - Full response:

[ ](- "ext:embed=#searchResult.body")

### The other index should be empty
When searching the full-text index:

[ ](- "#searchResult=searchFullTextIndex()")

Then:

 - The response status should be: [200](- "?=#searchResult.status");
 - The document count should be: [0](- "?=#searchResult.count");
 - Full response:

[ ](- "ext:embed=#searchResult.body")
