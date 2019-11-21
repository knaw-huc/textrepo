# TestZipFiles

## [Uploading a zip file](- 'upload')

When:
 
 - posting a zip file "[multiple-files.zip](- "#text")" with two files at [`/files`](- "#fileEndpoint");
   [ ](- "#file=uploadZip(#text)")

Then:

 - The HTTP response code should be [200](- "?=#file.status");
 - The json response should contain [2](- "?=#file.locationCount") location entries;
 - The first entry should have filename [een.txt](- "?=#file.filename1");
 - The first entry should have a location with a [valid UUID](- "?=#file.fileIdIsUUID1");
 - The second entry should have filename [twee.txt](- "?=#file.filename2");
 - The second entry should have a location with a [valid UUID](- "?=#file.fileIdIsUUID2").

---

When:

 - requesting the latest versions at [`/files/{fileId}/versions`](- "#fileEndpoint"); 
   [ ](- "#versions=requestLatestVersions(#file.fileId1, #file.fileId2)")

Then:

 - the latest version of the first file should have fileHash: [bf83caeb324ec7d185e8bf266d97ca9354732b76eee46b6227adb082](- "?=#versions.fileHash1") 
 - the latest version of the second file should have fileHash: [4fcc5672305c87f66f09c39f7229fd126cab95edf30821ec9ee4a66f](- "?=#versions.fileHash2") 

---

When:

 - querying the elasticsearch files index at [`/files/_file/{fileId}`](- "#fileIndexEndpoint")
   [ ](- "#esFiles=requestFilesFromEs(#file.fileId1, #file.fileId2)")
   
Then:

 - the first entry should have content: [Een.](- "?=#esFiles.getIndexedFile1") 
 - the second entry should have content: [Twee.](- "?=#esFiles.getIndexedFile2") 
 
 