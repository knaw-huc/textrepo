# Test `/rest/documents` collection

To find documents we first create: 
  
  - a document with external ID: [first-external-id](- "#externalId1");
  - and a document with external ID: [second-external-id](- "#externalId2").
  
[ ](- "createDocument(#externalId1)")
[ ](- "createDocument(#externalId2)")

## Find single document by external ID
An external ID should be unique. 
When we search documents with a `GET` to [/rest/documents?externalId={externalId}](- "#searchEndpoint")

 - where `{externalId}` is [ ](- "ext:embed=code(#externalId1)").

[ ](- "#searchSingle=search(#searchEndpoint, #externalId1)")

Then:

 - The response status should be: [200](- "?=#searchSingle.status");
 - The response array should contain [1](- "?=#searchSingle.documentCount") document;
 - Its external ID should be [first-external-id](- "?=#searchSingle.externalId");
 - Full response:
 
[ ](- "ext:embed=#searchSingle.body")

## Partial or non-existent external ID
When we search documents with a `GET` to [/rest/documents?externalId={externalId}](- "#searchEndpoint")

 - where `{externalId}` is non-existant external ID [ext](- "#partial").

[ ](- "#searchSingle=search(#searchEndpoint, #partial)")

Then:

 - The response status should be: [200](- "?=#searchSingle.status");
 - The response array should contain [0](- "?=#searchSingle.documentCount") document;
 - Full response:
 
[ ](- "ext:embed=#searchSingle.body")

## Paginate documents
Results are divided in pages using offset and limit and are sorted by creation date, newest documents first.

When we request all documents with a `GET` to [/rest/documents?offset={offset}&limit={limit}](- "#searchEndpoint")

 - where `{offset}` is [0](- "#offset") and `{limit}` is [1](- "#limit").

[ ](- "#firstPage=paginate(#searchEndpoint, #offset, #limit)")

Then:

 - The response status should be: [200](- "?=#firstPage.status");
 - The items array should contain [1](- "?=#firstPage.itemCount") document;
 - Its external ID should be [second-external-id](- "?=#firstPage.externalDocumentId") document;
 - Total should be [2](- "?=#firstPage.total");
 - Full response:
 
[ ](- "ext:embed=#firstPage.body")

## Request second page
When we request all documents with a `GET` to [/rest/documents?offset={offset}&limit={limit}](- "#searchEndpoint")

 - where `{offset}` is [1](- "#offset") and `{limit}` is [1](- "#limit").

[ ](- "#secondPage=paginate(#searchEndpoint, #offset, #limit)")

Then:

 - The response status should be: [200](- "?=#secondPage.status");
 - The items array should contain [1](- "?=#secondPage.itemCount") document;
 - Its external ID should be [first-external-id](- "?=#secondPage.externalDocumentId") document;
 - Total should be [2](- "?=#secondPage.total");
 - Full response:
 
[ ](- "ext:embed=#secondPage.body")

### Filter with 'createdAfter'
The query parameter `createdAfter` filters out any documents that where created before the specified date. 

To test this filter we create a new document after a certain delay.
[ ](- "#delayedDocumentId=createDocumentWithDelay()")


When retrieving the documents with a `GET` to [/rest/documents?createdAfter={date}](- "#getEndpoint")
[ ](- "#date=getCreatedAt(#delayedDocumentId)")

 - where `{date}` is [ ](- "ext:embed=code(#date)").

[ ](- "#paginateResult=filterByCreatedAfter(#getEndpoint, #date, #delayedDocumentId)")

Then:

 - The response status should be: [200](- "?=#paginateResult.status");
 - The response should only contain the [new](- "?=#paginateResult.hasNew") document;
 - Total should be [1](- "?=#paginateResult.total");
 - Full response:

[ ](- "ext:embed=#paginateResult.body")

