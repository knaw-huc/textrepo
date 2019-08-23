# FileHandling

## [Uploading a file](- "upload")

When a file with contents "[hello test](- "#text")" is [```POST```ed](- "#result = upload(#text)") then
 
 - the HTTP status should be [201](- "?=#result.status");
 - the resulting sha224 should be [55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6](- "?=#result.sha224");
 - the Location header is: [http://localhost:8080/textrepo/files/55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6](- "?=#result.location").
 
## [Retrieving an existing file](- "retrieve-existing")

When [```/files/55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6```](- "#uri") is subsequently
[retrieved](- "#result = retrieve(#uri)"):

  - the HTTP status should be [200](- "?=#result.status");
  - the contents should be "[hello test](- "?=#result.content")"

## [Retrieving a non-existent file](- "retrieve-non-existent")

When [```/files/deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefcafebabe```](- "#uri") is
[retrieved](- "#result = retrieve(#uri)"):

  - the HTTP status should be [404](- "?=#result.status");
  - the resulting object should contain the error message "[File not found](- "?=#result.message")"
