# TestMetadata

## [Creating and updating metadata](- 'createAndUpdateMetadata')

When a document is:

 - created with file [test1.txt](- "#filename");
 - populated with [{"foo":"bar","spam":"eggs"}"](- "#metadata");
 - and updated with file [test2.txt](- "#newFilename") [ ](- "#doc=createAndUpdateMetadata(#filename, #metadata, #newFilename)"):
  
Then:

 - [bar](- "?=#doc.foo");
 - [eggs](- "?=#doc.spam");
 - [test1.txt](- "?=#doc.filename");

 - [bar](- "?=#doc.stillFoo");
 - [eggs](- "?=#doc.stillSpam");
 - [test2.txt](- "?=#doc.updatedFilename");

 