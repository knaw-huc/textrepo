# TestFiles

## [Creating a new document with external document id](- 'upload')

When a document with:

 - external id "[external-test-id](- "#externalId")" 
 - type "[text](- "#type")" 
 - contents "[hello test](- "#text")" 

is [```POST```ed](- "#doc=upload(#externalId, #type, #text)") then

 - The HTTP response code should be [201 Created](- "?=#doc.status");
 - The result [has a Location header](- "?=#doc.hasLocationHeader"): [ ](- "c:echo=#doc.location");
 - The last part of the Location header, [ ](- "c:echo=#doc.docId"), is the file ID
 which is a [valid UUID](- "?=#doc.docIdIsUUID").

## Get external id of document

When a doc with id [ ](- "c:echo=#doc.docId") is [retrieved](- "#get=get(#doc.docId)"):

 - The HTTP response code should be [200 OK](- "?=#get.status");
 - The external ID should be [external-test-id](- "?=#get.externalId");

## [Updating file by external id](- 'update')

When this document is updated with a file:

 - external id "[external-test-id](- "#externalId")" 
 - type "[text](- "#type")" 
 - contents "[hello test 2](- "#text")" 

using [```PUT```](- "#doc2=update(#externalId, #type, #text)") then
 - The HTTP response code should be [200 OK](- "?=#doc2.status");

