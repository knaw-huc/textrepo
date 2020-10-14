# Test File Indexer

The file indexer creates and indexes a summary of each file, including its document, type, version and metadata information.

Below you can find some example queries as how to use the file index.

[ ](- "#docIds=createDocuments()")
[ ](- "#fileIds=createFiles()")
[ ](- "#versionIds=createVersions()")

To search the index, we first create: 

 - two document: [ ](- "c:echo=#docIds")
 - each with two files: [ ](- "c:echo=#fileIds")
 - each file with zero or two versions: [ ](- "c:echo=#versionIds")

## Find by file type
When can search files by type using the following query:

[ ](- "ext:embed=getEsQuerySearchByFileType()")

 - where `{type}` is [`text`](- "#fileType")

[ ](- "#result=searchEsQuerySearchByFileType(#fileType)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")

## Find by document metadata
We can search by document metadata key and values pairs using the following query:

[ ](- "ext:embed=getEsQuerySearchByDocMetadata()")

 - where `{key}` is [`foo`](- "#docMetaKey")
 - where `{value}` is [`bar`](- "#docMetaValue")

[ ](- "#result=searchEsQuerySearchByDocMetadata(#docMetaKey, #docMetaValue)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")

## Find by latest modification time of contents
Different versions of a file can have the same contents and hash.
To check what version is the last version with changed contents, we can run the following query:

[ ](- "ext:embed=getEsQuerySearchByContentsLastModified()")

 - where `{dateTime}` is [ ](- "c:echo=getDateTime()")

[ ](- "#result=searchEsQuerySearchByContentsLastModified()")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")




