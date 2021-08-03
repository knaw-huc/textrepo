# Test Index Tasks

When importing and deleting documents with tasks, you can choose to skip indexing and index later on using one of the index tasks. 

## Import without indexing

To speed up an import, you can skip the indexing step:

- using ``index=false``;

[ ](- "#importResult=importDoc()")

Then:

- The response status should be: [201](- "?=#importResult.status");

When searching the file index:

[ ](- "#searchResult=searchFileIndex()")

Then:

- The response status should be: [200](- "?=#searchResult.status");
- The document count should be: [0](- "?=#searchResult.count");

## Index single index

You can index or reindex a subset of files.

When retrieving the versions of a file with a `POST` to [/task/index/type/{name}](- "#indexTaskEndpoint")

- where `{name}` is [text](- "#typeName");

[ ](- "#indexResult=indexType(#indexTaskEndpoint, #typeName)")

Then:

- The response status should be: [200](- "?=#indexResult.status");

When searching the file index:

[ ](- "#searchResult=searchFileIndex()")

Then:

- The response status should be: [200](- "?=#searchResult.status");
- The document count should be: [1](- "?=#searchResult.count");

## Delete file without index delete

To speed up the deletion of files, you can skip the indexing step using ``index=false``.

When deleting a document, including its files and versions with a `DELETE` to [/task/delete/documents/{externalId}?index=false](- "#indexTaskEndpoint")

- where `{externalId}` is [ ](- "c:echo=getExternalId()");

[ ](- "#indexResult=deleteDoc(#indexTaskEndpoint, getExternalId())")

Then:

- The response status should be: [200](- "?=#indexResult.status");

When searching the file index:

[ ](- "#searchResult=searchFileIndex()")

Then:

- The response status should be: [200](- "?=#searchResult.status");
- The document count should be: [1](- "?=#searchResult.count");

## Remove deleted files from indices

You can remove all ES docs with IDs that do not exist in the files table. 

When removing all deleted files still present in indices with a `DELETE` to [/task/index/deleted-files](- "#deleteDeletedTaskEndpoint")

[ ](- "#deleteDeletedResult=deleteDeleted(#deleteDeletedTaskEndpoint)")

Then:

- The response status should be: [200](- "?=#deleteDeletedResult.status");

When searching the file index:

[ ](- "#searchResult=searchFileIndex()")

Then:

- The response status should be: [200](- "?=#searchResult.status");
- The document count should be: [0](- "?=#searchResult.count");
