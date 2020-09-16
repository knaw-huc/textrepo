.. |tr| replace:: Text Repository

Dashboard
=========

The |tr| offers a, currently minimal, dashboard API which can be used to get some 'high level'
overview statistics. We `welcome ideas and use cases <index.html#support>`_ to
improve and expand the dashboard API. You can `try out the dashboard
<http://localhost:8080/textrepo/swagger#/dashboard>`_ using swagger.

Get document counts
-------------------
At ``/dashboard``, |tr| gives counts of:

- how many documents are registered;
- how many documents have at least a single file;
- how many documents have at least one item of metadata;
- how many documents have both a file and metadata;
- how many documents have neither a file, nor any metadata.

The latter, in particular, is interesting when it is non-zero. These are
documents that have been registered with |tr| with their external identifier,
but are otherwise content-free. This is probably not what you want.

Find orphaned documents
-----------------------
If ``/dashboard`` indicates there are some documents with no content associated with them ("orphans"),
you can find out which these are at ``/dashboard/orphans``.

Get metadata counts
-------------------
At ``/dashboard/metadata`` you can find out how many documents have / share a given metadata key.
It returns documents broken down by metadata key.

Get metadata value counts
-------------------------
At ``/dashboard/metadata/{key}`` you can find, for a specific metadata key, documents counts
grouped by metadata value for that key.
