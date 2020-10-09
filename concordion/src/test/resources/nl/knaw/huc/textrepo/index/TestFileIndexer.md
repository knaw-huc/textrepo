# Test File Indexer

The file indexer creates and indexes a summary of each file, including its document, type, version and metadata information.

Below you can find some example queries as how to use the file index.

[ ](- "#docIds=createDocuments()")
[ ](- "#fileIds=createFiles(#docIds)")
[ ](- "#versionIds=createVersions(#fileIds)")

To search the index, we first create: 

 - two document: [ ](- "c:echo=#docIds")
 - each with two files: [ ](- "c:echo=#fileIds")
 - each file with zero or two versions: [ ](- "c:echo=#versionIds")

## Find by document metadata
When can search by document metadata key and values pairs using the following query:

[ ](- "ext:embed=getEsQuerySearchByDocMetadata()")

 - where `{key}` is [`foo`](- "#docMetaKey")
 - where `{value}` is [`bar`](- "#docMetaValue")

[ ](- "#result=searchEsQuerySearchByDocMetadata(#docMetaKey, #docMetaValue)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")




