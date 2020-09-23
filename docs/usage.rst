.. |tr| replace:: Text Repository

Basic Usage
===========

Run locally
-----------

Production setup
****************

The easiest way would be to start the |tr| in 'prod' setup, as found in ``examples/development``.
Run: ::

  cd examples/production
  ./run-prod.sh

The production setup does not build images but downloads the images from docker hub.

Development setup
*****************

When you want to start developing or debugging, you can use the 'dev' setup, as found in ``examples/development``.
When starting, docker-compose will build all |tr| images from scratch.
When stopping, all containers, volumes and network are removed.

See ``examples/development/README.md`` for installation details.

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

