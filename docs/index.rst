.. |tr| replace:: Text Repository

|tr|
===============

|tr| offers an API to store, index and retrieve texts, including their versions, formats and metadata.

Features
--------

- Store texts in a uniform domain model
   - Keep track of file versions
   - Link all file types to the same source document
   - Add metadata to documents and files
- Use Rest API to create, read, update and delete your texts
- Search files using stock and custom `elasticsearch <https://www.elastic.co/elasticsearch/>`_ indexes
- Explore API with `concordion <https://concordion.org/>`_ and `swagger <https://swagger.io/>`_

Installation
------------

To install the |tr| locally, run: ::

    $ git clone https://github.com/knaw-huc/textrepo .
    $ docker-compose up --build

Prerequisites: docker-compose.

`Read more <usage.html>`_

Documentation
-------------
.. toctree::
    :maxdepth: 2

    Overview <overview>
    Basic usage <usage>
    Components <components>
    Search and index <indexing>

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

|tr| uses the GNU General Public License version 2
