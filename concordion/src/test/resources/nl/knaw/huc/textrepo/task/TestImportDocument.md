# Test `/task/import/documents/{externalId}/{typeName}`

File contents can be directly uploaded to a document, referenced by its `externalId`. This yields a (possibly new)
version of a `typeName` typed file for that document.

If the document with `externalId` does not exist yet, you can have the repository create it by passing
`allowNewDocument=true`.

Otherwise, using `allowNewDocument=false`, the import task will verify that you are uploading to an already existing
document, or deny the request. Note that this is also the default if you leave `allowNewDocument` out.

If you upload the same contents of a version already registered for a particular document and type, no new version will
be created by default (`asLatestVersion=false`). This _idempotent_ behavior ensures that if, e.g., versions `1-2-3-4`
have been imported, and you offer the contents of `2` with `asLatestVersion=false`, there will be no changes to the
version trail.

However, if you _do_ want to have an **earlier** version, that was superseded by another import, to become the
**latest** version again, you can pass `asLatestVersion=true`. In the above example of versions `1-2-3-4` being in the
repository, if you were to import the contents of `2` with `asLatestVersion=true`, the version trail will be
`1-2-3-4-2` designating `2` as the latest version.

## Importing contents for a document that does not exist (yet)

We try to import a document

- with external ID: [document_1234](- "#externalId");
- using type: [text](- "#typeName");
- uploading a file containing [example content](- "#originalContents");

### 1. Using `allowNewDocument=false` (default)

When we `POST` the file to
[/task/import/documents/document_1234/text](- "#importEndpoint")

(or equivalently by explicitly appending the query param `?allowNewDocument=false`)

[ ](- "#result=retrieve(#importEndpoint, #externalId, #typeName, #originalContents)")

Then:

- The response status should be: [404](- "?=#result.status");
- Full response:

[ ](- "ext:embed=#result.body")

This helps prevent you from accidentally creating new documents when you know the document should already exist.

### 2. Using `allowNewDocument=true`

However, when we `POST` our file to

[/task/import/documents/document_1234/text?allowNewDocument=true](- "#importEndpoint2")

[ ](- "#result=retrieve(#importEndpoint2, #externalId, #typeName, #originalContents)")

Then:

- The response status should be: [201](- "?=#result.status");
- Headers should contain [restful relationship link to version](- "?=#result.versionLink");
- Headers should contain [restful relationship link to file](- "?=#result.fileLink");
- Headers should contain [restful relationship link to document](- "?=#result.documentLink");
- Headers should contain [restful relationship link to contents](- "?=#result.contentsLink");
- Link headers:

[ ](- "ext:embed=#result.headers")

- Body should contain [valid fileId](- "?=#result.fileId");
- Body should contain [valid documentId](- "?=#result.documentId");
- Body should contain [valid versionId](- "?=#result.versionId");
- Body should contain [valid contentsSha](- "?=#result.contentsSha");
- Body should indicate [a new version was created](- "?=#result.isNewVersion");
- Full response:

[ ](- "ext:embed=#result.body")

## Importing a version which may already be in the version history

### 1. Using `asLatestVersion=false (default)`

Now that `document_1234` has a version created in the test above, when we `POST` the same file
***again*** to

[/task/import/documents/document_1234/text](- "#importEndpoint3")

[ ](- "#result=retrieve(#importEndpoint3, #externalId, #typeName, #originalContents)")

Then:

- The response status should be: [200](- "?=#result.status");

And we get the same headers and body.

- Headers should contain [restful relationship link to version](- "?=#result.versionLink");
- Headers should contain [restful relationship link to file](- "?=#result.fileLink");
- Headers should contain [restful relationship link to document](- "?=#result.documentLink");
- Headers should contain [restful relationship link to contents](- "?=#result.contentsLink");
- Link headers:

[ ](- "ext:embed=#result.headers")

- Body should contain [valid fileId](- "?=#result.fileId");
- Body should contain [valid documentId](- "?=#result.documentId");
- Body should contain [valid versionId](- "?=#result.versionId");
- Body should contain [valid contentsSha](- "?=#result.contentsSha");
- Body should indicate [no new version was created](- "?=#result.isNewVersion");
- Full response:

[ ](- "ext:embed=#result.body")

This means that you can upload the same contents however often you like, no new version(s) will be created for contents
already present for a document.

### 2. Using `asLatestVersion=true` when uploaded contents is already latest version

When we `POST` our file again to

[/task/import/documents/document_1234/text?asLatestVersion=true](- "#importEndpoint4")

[ ](- "#result=retrieve(#importEndpoint4, #externalId, #typeName, #originalContents)")

Then:

- The response status should be: [200](- "?=#result.status");

Nothing was created, because the current version is ***already the latest version***.

- Headers should contain [restful relationship link to version](- "?=#result.versionLink");
- Headers should contain [restful relationship link to file](- "?=#result.fileLink");
- Headers should contain [restful relationship link to document](- "?=#result.documentLink");
- Headers should contain [restful relationship link to contents](- "?=#result.contentsLink");
- Link headers:

[ ](- "ext:embed=#result.headers")

- Body should contain [valid fileId](- "?=#result.fileId");
- Body should contain [valid documentId](- "?=#result.documentId");
- Body should contain [valid versionId](- "?=#result.versionId");
- Body should contain [valid contentsSha](- "?=#result.contentsSha");
- Body should indicate [no new version was created](- "?=#result.isNewVersion");
- Full response:

[ ](- "ext:embed=#result.body")

### 3. Using `asLatestVersion=true` when uploaded contents is an older version

When we `POST` some [other content](- "#otherContents") to

[/task/import/documents/document_1234/text](- "#importEndpoint5")

[ ](- "#result=retrieve(#importEndpoint5, #externalId, #typeName, #otherContents)")

Then:

- The response status should be: [201](- "?=#result.status")

for brevity, we omit testing headers and body for this intermediate version.

And if we then `POST` our original contents again to

[/task/import/documents/document_1234/text?asLatestVersion=true](- "#importEndpoint6")

[ ](- "#result=retrieve(#importEndpoint6, #externalId, #typeName, #originalContents)")

Then, finally, we get:

- The reponse status should be: [201](- "?=#result.status")

because a new version was created for our older, existing, contents.

- Headers should contain [restful relationship link to version](- "?=#result.versionLink");
- Headers should contain [restful relationship link to file](- "?=#result.fileLink");
- Headers should contain [restful relationship link to document](- "?=#result.documentLink");
- Headers should contain [restful relationship link to contents](- "?=#result.contentsLink");
- Link headers:

[ ](- "ext:embed=#result.headers")

- Body should contain [valid fileId](- "?=#result.fileId");
- Body should contain [valid documentId](- "?=#result.documentId");
- Body should contain [valid versionId](- "?=#result.versionId");
- Body should contain [valid contentsSha](- "?=#result.contentsSha");
- Body should indicate [a new version was created](- "?=#result.isNewVersion");
- Full response:

[ ](- "ext:embed=#result.body")
