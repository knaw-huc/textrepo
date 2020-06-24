.. |tr| replace:: Text Repository

|tr| API
========

The |tr| has a REST-full api to retrieve, create, update and delete resources. For certain complex activities a `task`-endpoint has been created.

To explore the API, start up the |tr| locally, and checkout swagger or the integration :ref:`endpoints`.

Tasks
-----

The REST API can be a bit laborious for certain activities. For example, to retrieve the latest contents of a document by its external document ID and file type, a user would have to perform a requests for each of the following steps:

- find a document ID by its external ID;
- find a file ID by its parent document ID and file type;
- find the latest version of a file by its parent file ID;
- get the contents of the latest version by its parent version ID or its hash.

To simplify such a workflow, the |tr| offers `task`-endpoints to perform a complex task within a single request. The advantage is simplicity and ease of use. However do not expect these tasks to be 'REST-compliant'.
Eg, the list of requests above can be replaced with a single request to: `/task/find/{externalId}/document/metadata`.
