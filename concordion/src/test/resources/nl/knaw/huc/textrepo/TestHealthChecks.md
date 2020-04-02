# Test Health Checks

The Text Repository provides a set of healtch checks of vital components like Elasticsearch, Postgres and Indexer services.

## Check health

When we run and request the health checks with a `GET` to [`/healthcheck`](- "#endpoint") using admin port [`8081`](- "#adminport");

[ ](- "#response=checkHealth(#endpoint, #adminport)")

Then:

 - Postgres database should be [healthy](- "?=#response.postgres")
 - Dropwizards deadlocks check should be [healthy](- "?=#response.deadlocks")
 - Full text _index_ should be [healthy](- "?=#response.index")
 - Full text _indexer_ should be [healthy](- "?=#response.indexer")
 - Full response:
 
[ ](- "ext:embed=#response.body")
