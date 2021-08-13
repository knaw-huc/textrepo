.. |tr| replace:: Text Repository

FAQ
===

Elasticsearch
-------------

If you run into this Elasticsearch warning: ::

  max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]

you may have to: ::

  sysctl -w vm.max_map_count=262144

(Source: https://github.com/docker-library/elasticsearch/issues/111)

----

If you run into this Elasticsearch error: ::

  max file descriptors [4096] for elasticsearch process is too low, increase to at least [65535]

you may have to increase the ulimits.nofile of elasticsearch in your docker-compose.yml: ::

  services:
    elasticsearch:
      ulimits:
        nofile:
          soft: 65535
          hard: 262144

(Source: https://stackoverflow.com/a/58024178)

