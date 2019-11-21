# TestFiles

## [Uploading a file](- 'upload')

When a file with contents "[hello test](- "#text")" is [```POST```ed](- "#file=upload(#text)") then

 - The HTTP response code should be [201 Created](- "?=#file.status");
 - The result [has a Location header](- "?=#file.hasLocationHeader"): [ ](- "c:echo=#file.location");
 - The last part of the Location header, [ ](- "c:echo=#file.fileId"), is the file ID
 which is a [valid UUID](- "?=#file.fileIdIsUUID").


## [Retrieving latest version of File](- 'latest')

Assuming our "[hello test](- "#text")" file [was assigned](- "#file=upload(#text)") ID [ ](- "c:echo=#file.fileId"), we can issue

  ```GET``` [ ](- "c:echo=#file.location")```/contents```

to [get the latest version](- "#result = latest(#file.location)") and

 - The HTTP reponse code should be [200 OK](- "?=#result.status")
 - The result entity should be [hello test](- "?=#result.entity")

## [Retrieving indexed File contents](- 'index')

Assuming our "[hello test](- "#text")" file [was assigned](- "#file=upload(#text)") ID [ ](- "c:echo=#file.fileId"), we can issue

  ```GET ``` [ ](- "c:echo=#file.esLocation")

to [get indexed file](- "#result = index(#file.fileId)") and

 - The HTTP reponse code should be [200 OK](- "?=#result.status")
 - The json result should contain [hello test](- "?=#result.entity")
