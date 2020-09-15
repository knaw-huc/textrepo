# Text Repository Acceptance Tests

 - [Health checks](TestHealthChecks.md "c:run")
 - [Autocomplete indexer](TestAutocompleteIndexer.md "c:run")

## Rest API

A user of the Text Repository can retrieve, create, update and delete resources using a REST-full API:

 - [Document collection](rest/TestRestDocumentCollection.md "c:run")
 - [Documents](rest/TestRestDocuments.md "c:run")
 - [Document metadata](rest/TestRestDocumentMetadata.md "c:run")
 - [Document files](rest/TestRestDocumentFiles.md "c:run")
 - [Types](rest/TestRestTypes.md "c:run")
 - [Files](rest/TestRestFiles.md "c:run")
 - [File metadata](rest/TestRestFileMetadata.md "c:run")
 - [File versions](rest/TestRestFileVersions.md "c:run")
 - [Versions](rest/TestRestVersions.md "c:run")
 - [Version contents](rest/TestRestVersionContents.md "c:run")
 - [Contents](rest/TestRestContents.md "c:run")

# Task API

The Text Repository offers `task`-endpoints to perform a single, complex task within a single request:

 - [Get document metadata by external ID](task/TestFindDocumentMetadataByExternalId.md "c:run")
 - [Get file metadata by external ID and file type](task/TestFindFileMetadataByExternalId.md "c:run")
 - [Get latest file contents by external ID and file type](task/TestFindFileContentsByExternalId.md "c:run")
 - [Index files by type](task/TestIndexFilesByType.md "c:run")
 - [Index files by index name](task/TestIndexFilesByIndexName.md "c:run")
 
# Dashboard

The Text Repository offers various diagnostics about the state of documents:

 - [Show count of documents lacking files and / or metadata](dashboard/TestDashboard.md "c:run")
