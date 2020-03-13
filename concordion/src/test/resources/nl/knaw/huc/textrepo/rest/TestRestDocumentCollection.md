# Test `/rest/documents` collection

To find documents we first create: 
  
  - a document with external ID: [`first-external-id`](- "#externalId1");
  - and a document with external ID: [`second-external-id`](- "#externalId2").
  
[ ](- "createDocument(#externalId1)")
[ ](- "createDocument(#externalId2)")

## Find single document by external ID
An external ID should be unique. 
So when we search documents with a `GET` to [`/rest/documents`](- "#searchEndpoint")

 - with query param: [`externalId`](- "#queryParam") and value: [`first-external-id`](- "#queryParamValue").

[ ](- "#searchSingle=search(#searchEndpoint, #queryParam, #queryParamValue)")

Then:

 - The response status should be: [200](- "?=#searchSingle.status");
 - The response array should contain [1](- "?=#searchSingle.documentCount") document;
 - Its external ID should be [first-external-id](- "?=#searchSingle.externalId");
 - Full response:
 
[ ](- "ext:embed=#searchSingle.body")

## Find documents by external ID
When we search documents with a `GET` to [`/rest/documents`](- "#searchEndpoint")

 - with query param: [`externalId`](- "#queryParam") and value: [`ext`](- "#queryParamValue").

[ ](- "#searchSingle=searchMultiple(#searchEndpoint, #queryParam, #queryParamValue)")

Then:

 - The response status should be: [200](- "?=#searchSingle.status");
 - The response array should contain [2](- "?=#searchSingle.documentCount") documents;
 - The response array should contain [first and second](- "?=#searchSingle.externalIds") external IDs;
 - Full response:
 
[ ](- "ext:embed=#searchSingle.body")

## Request first page
When we request all documents with a `GET` to [`/rest/documents`](- "#searchEndpoint")

 - with query parameter `limit`: [`1`](- "#limit") and `offset`: [`0`](- "#offset").

[ ](- "#firstPage=paginate(#searchEndpoint, #offset, #limit)")

Then:

 - The response status should be: [200](- "?=#firstPage.status");
 - The items array should contain [1](- "?=#firstPage.itemCount") document;
 - Its external ID should be [`first-external-id`](- "?=#firstPage.externalDocumentId") document;
 - Total should be [2](- "?=#firstPage.total");
 - Full response:
 
[ ](- "ext:embed=#firstPage.body")

## Request second page
When we request all documents with a `GET` to [`/rest/documents`](- "#searchEndpoint")

 - with query parameter `limit`: [`1`](- "#limit") and `offset`: [`1`](- "#offset").

[ ](- "#secondPage=paginate(#searchEndpoint, #offset, #limit)")

Then:

 - The response status should be: [200](- "?=#secondPage.status");
 - The items array should contain [1](- "?=#secondPage.itemCount") document;
 - Its external ID should be [`second-external-id`](- "?=#secondPage.externalDocumentId") document;
 - Total should be [2](- "?=#secondPage.total");
 - Full response:
 
[ ](- "ext:embed=#secondPage.body")
