# TestDocuments

## [Uploading a document](- 'upload')

When a file with contents "[hello test](- "#text")" is [```POST```ed](- "#doc=upload(#text)") then

 - The HTTP response code should be [201 Created](- "?=#doc.status");
 - The result [has a Location header](- "?=#doc.hasLocationHeader"): [ ](- "c:echo=#doc.location");
 - The last part of the Location header, [ ](- "c:echo=#doc.documentId"), is the document ID
 which is a [valid UUID](- "?=#doc.documentIdIsUUID").


## [Retrieving latest version of Document](- 'latest')

Assuming our "[hello test](- "#text")" document [was assigned](- "#doc=upload(#text)") ID [ ](- "c:echo=#doc.documentId"), we can issue

  ```GET``` [ ](- "c:echo=#doc.location")```/contents```

to [get the latest version](- "#result = latest(#doc.location)") and

 - The HTTP reponse code should be [200 OK](- "?=#result.status")
 - The result entity should be [hello test](- "?=#result.entity")

## [Retrieving indexed Document file](- 'index')

Assuming our "[hello test](- "#text")" document [was assigned](- "#doc=upload(#text)") ID [ ](- "c:echo=#doc.documentId"), we can issue

  ```GET ``` [ ](- "c:echo=#doc.esLocation")

to [get indexed document](- "#result = index(#doc.documentId)") and

 - The HTTP reponse code should be [200 OK](- "?=#result.status")
 - The json result should contain [hello test](- "?=#result.entity")
