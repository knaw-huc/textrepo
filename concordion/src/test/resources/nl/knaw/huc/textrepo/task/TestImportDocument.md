# Test `/task/import/documents/{externalId}/{typeName}`

File contents can be directly uploaded to a document, referenced by its `externalId`. This yields a (possibly new)
version of a `typeName` typed file for that document.

If the document with `externalId` does not yet exist, you can have the repository create it during import by passing
`allowNewDocuments=true`.

Otherwise, using `allowNewDocuments=false`, the import will verify that you are uploading to an already existing
document, or deny the request. Note that this is also the default if you leave `allowNewDocuments` out.

If you offer the same contents of a version already registered for this document during another import, by
default (`asLatestVersion=false`), no new version will be created as it already exists for that particular document and
type. This _idempotent_ behavior ensures that if, e.g., versions `1-2-3-4` have been imported so far, and you offer the
contents of `2` in subsequent import requests with `asLatestVersion=false`, there will be no changes to the versions.

However, if you _do_ want to have an **earlier** version, that was superseded by another import, to become the
**latest** version again, you can pass `asLatestVersion=true` with your import request. In the above example of
versions `1-2-3-4` being in the repository, if you were to import the contents of `2` again with `asLatestVersion=true`,
the versions in the repository will look like `1-2-3-4-2` designating version `2` as the latest version again.
