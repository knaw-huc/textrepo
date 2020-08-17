.. |tr| replace:: Text Repository

|tr|
===============

|tr| offers an API to store, index and retrieve texts, including their versions, formats and metadata.

Features
--------

- Store texts in a uniform domain model
   - Keep track of file versions
   - Link all file types to the same source document
   - Store metadata about documents and files
- Use Rest API to create, read, update and delete your texts
- Search files using stock and custom `elasticsearch <https://www.elastic.co/elasticsearch/>`_ indexes
- Explore API with `concordion <https://concordion.org/>`_ and `swagger <https://swagger.io/>`_

Installation
------------

Prerequisites: docker-compose.

To install the |tr| locally, run in a new directory: ::

    $ git clone https://github.com/knaw-huc/textrepo .
    $ docker-compose up


`Read more <usage.html>`_

Documentation
-------------
.. toctree::
    :maxdepth: 2

    Overview <overview>
    Basic usage <usage>
    Components <components>
    Rest and Tasks API <tr-api>
    Indexing <indexing>
    Dashboard <dashboard>
    Experimental <experimental>
    Experimental production volumes <experimental-prod-volumes>

Support
-------

If you are having issues, please let us know at:

https://github.com/knaw-huc/textrepo/issues

Contribute
----------

Want to improve the |tr|? Submit a pull request at:

https://github.com/knaw-huc/textrepo

License
-------

Copyright 2020 Koninklijke Nederlandse Akademie van Wetenschappen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
