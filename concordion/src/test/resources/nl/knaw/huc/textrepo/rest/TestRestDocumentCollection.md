# Test `/rest/documents` collection

## Retrieve documents by external ID
To find documents we first create: 
  
  - a document with external ID: [`first-external-id`](- "#externalId1");
  - and a document with external ID: [`second-external-id`](- "#externalId2").

[ ](- "createDocument(#externalId1)")
[ ](- "createDocument(#externalId2)")

## Find single document
An external ID should be unique. 
So when we search documents with a `PUT` to [`/rest/documents`](- "#searchEndpoint")

 - with query param: [`externalId`](- "#queryParam") and value: [`first-external-id`](- "#queryParamValue").

[ ](- "#searchSingle=search(#searchEndpoint, #queryParam, #queryParamValue)")

Then:

 - The response status should be: [200](- "?=#searchSingle.status");
 - The response array should contain [1](- "?=#searchSingle.documentCount") document;
 - Its external ID should be [first-external-id](- "?=#searchSingle.externalId");
 - Full response:
 
[ ](- "ext:embed=#searchSingle.body")

## Find multiple documents
An external ID should be unique. 
So when we search documents with a `PUT` to [`/rest/documents`](- "#searchEndpoint")

 - with query param: [`externalId`](- "#queryParam") and value: [`ext`](- "#queryParamValue").

[ ](- "#searchSingle=searchMultiple(#searchEndpoint, #queryParam, #queryParamValue)")

Then:

 - The response status should be: [200](- "?=#searchSingle.status");
 - The response array should contain [2](- "?=#searchSingle.documentCount") document;
 - The response array should contain [first and second](- "?=#searchSingle.externalIds") external IDs;
 - Full response:
 
[ ](- "ext:embed=#searchSingle.body")
