.. |tr| replace:: Text Repository

Content Coordinate System
=========================

Caveat lector: `HC SVNT DRACONES`__

__ https://en.wikipedia.org/wiki/Here_be_dragons

Addressing Content
------------------

Suppose a document (e.g., a ``txt`` file) is in the |tr|, how can we then refer to portions of the text inside the document?

Let's try some URIs.

Suppose that::

  curl $prefix/documents/$id

will get you all of the document's ``txt`` contents. It would be nice if we could address a `character range`, e.g.,
from characters ``10-15`` in a manner such as::

  curl $prefix/documents/$id/text/chars?start=10&end=15

or::

  curl $prefix/documents/$id/text/chars?start=10&length=6

Similarly we could be interested in `lines` ``2..4`` at address::

 curl $prefix/documents/$id/text/lines?start=2&end=4

The idea being that the ``text`` part in ``$prefix/documents/$id/text/chars?start=10&length=6`` signifies that we
are interested in interpreting the document from a ``txt`` perspective, looking at lines, words, characters, etc.
(as opposed to, e.g., an XML / TEI context where we could be interested in getting the author from the metadata, ...)

Things get interesting once we challenge ourselves to the idea that we could be interested in
viewing a document from this ``text`` perspective, irrespective of the actual format that was used to upload
the document. So, even if a ``TEI`` (or ``PageXML``, ``hOCR``, ...) document is uploaded, we still want
to address its `textual content` via ``/text/chars/...`` and we consider it a |tr| responsibility to be
able to (in this case) yield the requested `character range` (lines, words, ...) from the ``TEI`` document.

Perspectives
------------

In ``$prefix/documents/$id/text/{chars,lines,words,...}`` we will call ``text`` the `perspective`.

**TODO**: `conjure up terminology for the` ``{chars,lines,words}`` `part`, perhaps ``selector``?

``$prefix/documents/$id/<perspective>/<selector>?params``

Sidestepping to current |tr| implementation, ``perspective`` could be a Resource class in the WebApp stack,
translating the URI / addressing scheme. Then a separate `Perspective` class hierarchy is responsible for mapping
various file formats to text (e.g., flatten an the tree and yield the characters for a generic XML file, do something
more intelligent for a TEI file, use "Rutger's" implementation for PageXML/hOCR, etc.). Then a `Selector` class
hierarchy works in the ``txt`` domain and selects the requested fragment.

**TODO**: diagrams :-)
