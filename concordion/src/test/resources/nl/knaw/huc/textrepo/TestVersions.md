# TestZipDocuments

## [Uploading multiple versions](- 'uploadMultipleVersions')

When a document is created with content [hello test](- "#content")" and updated with [hello test2](- "#newContent")" [ ](- "#doc=uploadMultipleVersions(#content, #newContent)"), then

 - the HTTP response code of creating the document should be [201](- "?=#doc.status"): [ ](- "c:echo=#doc.documentUuid");
 - the HTTP response code of updating the document should be [200](- "?=#doc.statusUpdate");
 - the first version should have hash: [55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6](- "?=#doc.version1Sha");
 - the latest version should have hash: [1643dc4c13b2acc63d95706442d2456a07790ba6404baa36a1a4dd80](- "?=#doc.version2Sha");
 - the index should contain the latest file version: [hello test2](- "?=#doc.indexContentAfterUpdate").
 