# Test `/task/find/{externalId}/file/contents?type={name}`

The contents of the latest file version can be retrieved with the `find` task, an external ID and file type.

To retrieve file metadata we first create:

- a document with external ID: [`test-external-id`](- "#externalId");
- with a file of type: [`text`](- "#fileType");
- and a file version containing: [`example content`](- "#contents");

[ ](- "#docId=createDocument(#externalId)")
[ ](- "#fileId=createFile(#docId)")
[ ](- "createVersion(#fileId, #contents)")

### Retrieve file contents

When retrieving the contents of a file with a `GET`
to [`/task/find/{externalId}/file/contents?type={typeName}`](- "#findEndpoint")

- where `{externalId}` is [ ](- "c:echo=#externalId");
- where `{typeName}` is [ ](- "c:echo=#fileType");

[ ](- "#retrieveResult=retrieve(#findEndpoint, #externalId, #fileType)")

Then:

- The response status should be: [200](- "?=#retrieveResult.status");
- Contents should be: [`example content`](- "?=#retrieveResult.body");
- Headers should contain link to [version history](- "?=#retrieveResult.versionHistory");
- Headers should contain link to [parent resource](- "?=#retrieveResult.parent");
- Headers should contain link to [type resource](- "?=#retrieveResult.type");
- Link headers:

[ ](- "ext:embed=#retrieveResult.headers")

- Full response:

[ ](- "ext:embed=#retrieveResult.body")
