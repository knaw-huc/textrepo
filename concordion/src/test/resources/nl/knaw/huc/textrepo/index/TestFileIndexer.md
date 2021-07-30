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

## Find files by file type
When can search files by type using the following query:

[ ](- "ext:embed=getEsQuerySearchByFileType()")

 - where `{type}` is [text](- "#fileType")

[ ](- "#result=searchEsQuerySearchByFileType(#fileType)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")

## Find files by document metadata
We can search by document metadata key and values pairs using the following query:

[ ](- "ext:embed=getEsQuerySearchByDocMetadata()")

 - where `{key}` is [foo](- "#docMetaKey")
 - where `{value}` is [bar](- "#docMetaValue")

[ ](- "#result=searchEsQuerySearchByDocMetadata(#docMetaKey, #docMetaValue)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")



## Find files by metadata key only

Search for a file metadata key, ignoring the value it has.

[ ](- "ext:embed=getEsQuerySearchByFileMetadataKey()")

 - where `{key}` is [file-foo](- "#fileMetaKey")

[ ](- "#result=searchEsQuerySearchByFileMetadataKey(#fileMetaKey)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct files](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")

## Find files by latest modification time of contents
Different versions of a file can have the same contents and hash.
To check what version is the last version with changed contents, we can run the following query:

[ ](- "ext:embed=getEsQuerySearchByContentsLastModified()")

 - where `{dateTime}` is [ ](- "c:echo=getDateTime()")

[ ](- "#result=searchEsQuerySearchByContentsLastModified()")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")

# Other examples

## Find files by type and latest modification time of contents

Find all files by `{type}` that have versions with changed contents at or after `{dateTime}`:

[ ](- "ext:embed=getEsQuerySearchByFileTypeAndContentsLastModified()")

 - where `{dateTime}` is [ ](- "c:echo=getDateTime()")
 - where `{type}` is [text](- "#type")

[ ](- "#result=searchEsQuerySearchByFileTypeAndContentsLastModified(#type)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct file](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")


## Find documents by metadata

Find all documents by document metadata `{key}` and `{value}`, including an aggregated count of found _documents_:

[ ](- "ext:embed=getEsQuerySearchDocsByMetadata()")

 - where `{key}` is [foo](- "#docMetaKey")
 - where `{value}` is [bar](- "#docMetaValue")

[ ](- "#result=searchEsQueryDocsByMetadata(#docMetaKey, #docMetaValue)")

 - The response status should be: [200](- "?=#result.status");
 - The response should contain [correct document](- "?=#result.found");

Full response:
[ ](- "ext:embed=#result.body")
