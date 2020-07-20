.. |tr| replace:: Text Repository

Basic Usage
===========

Run locally
-----------

|tr| can be started using ``docker-compose up``. However, in order to prevent any unexpected behaviour due to
misconfigured ports, volumes, etc, the ``./scripts`` directory also contains some example scripts.

To start and stop the |tr|: ::

  ./scripts/start.sh
  ./scripts/stop.sh

Note: These scripts are quite rigorous and all data will be lost when restarting, so *use for development purposes only*.

When you do not want to rebuild docker images, use: ``docker-compose up``.

Explore locally
---------------
After running the docker-compose setup, you can:

- Find basic use cases in the integration test `results <http://localhost:8080/concordion/nl/knaw/huc/textrepo/Textrepo.html>`_
- Add some test data: ``./scripts/populate.sh``
- Explore REST-API of |tr| using `swaggger <http://localhost:8080/textrepo/swagger>`_
- Search in `full-text <http://localhost:8080/index/full-text>`_ and `autocomplete <http://localhost:8080/index/autocomplete>`_ indexes

FAQ
---

Elasticsearch
*************

If you run into this Elasticsearch warning: ::

  max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]

you may have to: ::

  sysctl -w vm.max_map_count=262144

(Source: https://github.com/docker-library/elasticsearch/issues/111)

