# TestVersions

## [Uploading multiple versions](- 'uploadMultipleVersions')

When:

 - creating a file with contents [`hello test`](- "#contents") at [`/files`](- "#filesEndpoint");
 - updating it with [hello test2](- "#newContent") at [`/files/{fileId}/contents`](- "#fileContentsEndpoint");

[ ](- "#file=uploadMultipleVersions(#contents, #filesEndpoint, #newContent, #fileContentsEndpoint)")
Then:

 - the HTTP response code of creating the file should be [`201`](- "?=#file.status");
 - the HTTP response code of updating the file should be [`200`](- "?=#file.statusUpdate");
 - the first version should have hash [`55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6`](- "?=#file.version1Sha");
 - the latest version should have hash [`1643dc4c13b2acc63d95706442d2456a07790ba6404baa36a1a4dd80`](- "?=#file.version2Sha");
 - the index should contain the latest file version: [`hello test2`](- "?=#file.indexContentAfterUpdate").
