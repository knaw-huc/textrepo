# TestDocuments

## [Uploading a document](- 'upload')

When a zip file "[multiple-files.zip](- "#text")" with multiple files is [```POST```ed](- "#doc=uploadZip(#text)") then

 - The HTTP response code should be [200](- "?=#doc.status");
 - The json response should contain [2](- "?=#doc.locationCount") location entries
 - The first entry should have filename [een.txt](- "?=#doc.filename1") 
 - The first entry should have a location with a [valid UUID](- "?=#doc.documentIdIsUUID1"): [ ](- "c:echo=#doc.location1").
 
 
 