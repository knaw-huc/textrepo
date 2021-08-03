# Test `/task/index/{name}`

All files relevant to a specific indexer can be indexed and reindexed. Other indices will not be touched.

To index, we first create three documents, each with one file and version, using the import task with query param `index=false`: 

 - using type: [text](- "#type");

[ ](- "#importResult=importDocs(#type)")

Then:

 - Response statuses should be: [201, 201, 201](- "?=#importResult.status");
 - Full response of first imported document:

[ ](- "ext:embed=#importResult.body")

### Indexing by index name
When indexing the files with a `POST` to [/task/index/indexer/{name}](- "#indexEndpoint") 

 - where `{name}` is [autocomplete](- "#indexName");

[ ](- "#retrieveResult=indexFilesBy(#indexEndpoint, #indexName)")

Then:

 - Response status should be: [200](- "?=#retrieveResult.status");
 - Full response:

[ ](- "ext:embed=#retrieveResult.body")

### The index should be updated
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
