# FileHandling

### [Uploading](- "upload")

When a file with contents "[hello test](- "#text")" is [uploaded](- "#result = upload(#text)") then
 
 * the HTTP status should be [201](- "?=#result.status");
 * the resulting sha224 should be [55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6](- "?=#result.sha224");
 * the Location header is: [http://localhost:8080/textrepo/files/55d4c44f5bc05762d8807f75f3f24b4095afa583ef70ac97eaf7afc6](- "?=#result.location").
