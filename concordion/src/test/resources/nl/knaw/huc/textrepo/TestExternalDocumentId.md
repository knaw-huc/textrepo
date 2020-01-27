# TestFiles

## [Creating a new document with external document id](- 'upload')

When a document with:

 - external id "[external-test-id](- "#externalId")" 
 - type "[text](- "#type")" 
 - contents "[hello test](- "#text")" 

is [```POST```ed](- "#file=upload(#externalId, #type, #text)") then

 - The HTTP response code should be [201 Created](- "?=#file.status");
 - The result [has a Location header](- "?=#file.hasLocationHeader"): [ ](- "c:echo=#file.location");
 - The last part of the Location header, [ ](- "c:echo=#file.fileId"), is the file ID
 which is a [valid UUID](- "?=#file.fileIdIsUUID").

## [Updating file by external id](- 'update')

When an updated file with:

 - external id "[external-test-id](- "#externalId")" 
 - type "[text](- "#type")" 
 - contents "[hello test 2](- "#text")" 

is [```PUT```](- "#file=update(#externalId, #type, #text)") then

// TODO
