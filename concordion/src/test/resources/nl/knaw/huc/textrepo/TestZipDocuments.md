# TestZipDocuments

## [Uploading a zip file](- 'upload')

When:
 
 - posting a zip file "[multiple-files.zip](- "#text")" with two files at [`/documents`](- "#documentEndpoint");
   [ ](- "#doc=uploadZip(#text)")

Then:

 - The HTTP response code should be [200](- "?=#doc.status");
 - The json response should contain [2](- "?=#doc.locationCount") location entries;
 - The first entry should have filename [een.txt](- "?=#doc.filename1");
 - The first entry should have a location with a [valid UUID](- "?=#doc.documentIdIsUUID1");
 - The second entry should have filename [twee.txt](- "?=#doc.filename2");
 - The second entry should have a location with a [valid UUID](- "?=#doc.documentIdIsUUID2").

---

When:

 - requesting the latest versions at [`/documents/{documentId}/versions`](- "#documentEndpoint"); 
   [ ](- "#versions=requestLatestVersions(#doc.documentId1, #doc.documentId2)")

Then:

 - the latest version of the first document should have fileHash: [bf83caeb324ec7d185e8bf266d97ca9354732b76eee46b6227adb082](- "?=#versions.fileHash1") 
 - the latest version of the second document should have fileHash: [4fcc5672305c87f66f09c39f7229fd126cab95edf30821ec9ee4a66f](- "?=#versions.fileHash2") 

---

When:

 - querying the elasticsearch documents index at [`/documents/_doc/{documentId}`](- "#documentIndexEndpoint")
   [ ](- "#esDocs=requestEsDocs(#doc.documentId1, #doc.documentId2)")
   
Then:

 - the first entry should have content: [Een.](- "?=#esDocs.getIndexDocument1") 
 - the second entry should have content: [Twee.](- "?=#esDocs.getIndexDocument2") 
 
 