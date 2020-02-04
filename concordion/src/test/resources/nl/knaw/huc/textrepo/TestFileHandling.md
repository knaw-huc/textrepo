# FileHandling

When a file with contents "[hello test](- "#text")" is [```POST```ed](- "#result = upload(#text)") then
 
 - the HTTP status should be [201](- "?=#result.status");
 - the resulting sha224 should be [55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6](- "?=#result.sha224");

## [Retrieving an existing file](- "retrieve-existing")

When [```/rest/contents/55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6```](- "#uri") is 
[retrieved](- "#result = retrieve(#uri)"):

  - the HTTP status should be [200](- "?=#result.status");
  - the contents should be "[hello test](- "?=#result.content")"

## [Retrieving a non-existent file](- "retrieve-non-existent")

When [```/rest/contents/deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefcafebabe```](- "#uri") is
[retrieved](- "#result = retrieve(#uri)"):

  - the HTTP status should be [404](- "?=#result.status");
  - the resulting object should contain the error message "[Contents not found](- "?=#result.message")"
