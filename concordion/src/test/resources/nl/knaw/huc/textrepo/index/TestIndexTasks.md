# Test Index Tasks

// TODO:

When importing and deleting documents with tasks, you can choose to skip indexing, and to index later on using one of the index tasks. 

## Import without indexing

To speed up a |tr| import, you can skip the indexing step using ``index=false``. 

## Index single index

One can index files that were not indexed before or reindex a subset of files using one of the index tasks.

## Delete file without index delete

To speed up the deletion of |tr| files, you can skip the indexing step using ``index=false``.

## Remove deleted files from indices

You can remove all ES docs with IDs that do not exist in the files table. 

