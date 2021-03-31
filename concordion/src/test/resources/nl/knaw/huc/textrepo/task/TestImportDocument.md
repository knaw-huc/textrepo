# Test `/task/import/documents/{externalId}/{typeName}`

File contents can be directly uploaded to a document, referenced by its `externalId`. This yields a (possibly new)
version of a `typeName` typed file for that document.

If the document with `externalId` does not yet exist, you can have the repository create it during import by passing
`allowNewDocument=true`.

Otherwise, using `allowNewDocument=false`, the import will verify that you are uploading to an already existing
document, or deny the request. Note that this is also the default if you leave `allowNewDocument` out.

If you offer the same contents of a version already registered for this document during another import, by
default (`asLatestVersion=false`), no new version will be created as it already exists for that particular document and
type. This _idempotent_ behavior ensures that if, e.g., versions `1-2-3-4` have been imported so far, and you offer the
contents of `2` in subsequent import requests with `asLatestVersion=false`, there will be no changes to the versions.

However, if you _do_ want to have an **earlier** version, that was superseded by another import, to become the
**latest** version again, you can pass `asLatestVersion=true` with your import request. In the above example of
versions `1-2-3-4` being in the repository, if you were to import the contents of `2` again with `asLatestVersion=true`,
the versions in the repository will look like `1-2-3-4-2` designating version `2` as the latest version again.

## Importing contents for a document that does not exist yet

We try to import a document

- with external ID: [`document_1234`](- "#externalId");
- using type: [`text`](- "#typeName");
- uploading a file containing [`example content`](- "#contents");

### 1. Refusing new documents to be made

When we `POST` the file to
[`/task/import/documents/dochment_1234/text`](- "#importEndpoint")

(or equivalently by explicitly appending the query param `?allowNewDocument=false`)

[ ](- "#result=retrieve(#importEndpoint, #externalId, #typeName, #contents)")

Then:

- The response status should be: [404](- "?=#result.status");
- Full response:

[ ](- "ext:embed=#result.body")

### 2. Allowing new documents to be made

However, when we `POST` our file to

[`/task/import/documents/document_1234/text?allowNewDocument=true`](- "#importEndpoint")

[ ](- "#result=retrieve(#importEndpoint, #externalId, #typeName, #contents)")

Then:

- The response status should be: [201](- "?=#result.status");
- Link headers:

[ ](- "ext:embed=#result.headers")

- Full response:

[ ](- "ext:embed=#result.body")

