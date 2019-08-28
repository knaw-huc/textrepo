# TestZipDocuments

## [Uploading a zip file](- 'upload')

When a zip file "[multiple-files.zip](- "#text")" with two files is [```POST```ed](- "#doc=uploadZip(#text)") then

 - The HTTP response code should be [200](- "?=#doc.status");
 - The json response should contain [2](- "?=#doc.locationCount") location entries
 - The first entry should have filename [een.txt](- "?=#doc.filename1") 
 - The first entry should have a location with a [valid UUID](- "?=#doc.documentIdIsUUID1"): [ ](- "c:echo=#doc.location1").
 - When requesting the latest version, the first document version should have fileHash: [bf83caeb324ec7d185e8bf266d97ca9354732b76eee46b6227adb082](- "?=#doc.fileHash1") 
 - When querying the documents index, the first entry should have content: [Een.](- "?=#doc.getIndexDocument1") 
 - The second entry should have filename [twee.txt](- "?=#doc.filename2") 
 - The second entry should have a location with a [valid UUID](- "?=#doc.documentIdIsUUID2"): [ ](- "c:echo=#doc.location2").
 - When requesting the latest version, the second document version should have a fileHash: [4fcc5672305c87f66f09c39f7229fd126cab95edf30821ec9ee4a66f](- "?=#doc.fileHash2") 
 - When querying the documents index, the second entry should have content: [Twee.](- "?=#doc.getIndexDocument2") 
 
 
 