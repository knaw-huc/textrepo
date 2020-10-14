# Test Autocomplete Indexer

The autocomplete indexer indexes every file using elasticsearchs Completion Suggester. 
New file versions will overwrite older versions in the es index. 

To retrieve suggestions, we first create three document with three text files: 
  
[ ](- "#docId1=createDocument()")
[ ](- "#fileId1=createFile(#docId1)")
[ ](- "#docId2=createDocument()")
[ ](- "#fileId2=createFile(#docId2)")
[ ](- "#docId3=createDocument()")
[ ](- "#fileId3=createFile(#docId3)")

  - File ID: [ ](- "c:echo=#fileId1")
  - File ID: [ ](- "c:echo=#fileId2")
  - File ID: [ ](- "c:echo=#fileId3")  

## Uploading file versions

When new file versions with contents 
"[hello Tesseract](- "#text1")", 
"[hello teStament](- "#text2")" and 
"[hello testosterone](- "#text3")" 
are [```POST```ed](- "#versions=upload(#fileId1, #text1, #fileId2, #text2, #fileId3, #text3)"),
then we should have three [valid versions](- "?=#versions.validVersions"):

  - [ ](- "c:echo=#versions.validUuid1")
  - [ ](- "c:echo=#versions.validUuid2")
  - [ ](- "c:echo=#versions.validUuid3")

## [Searching with autocomplete](- 'searchAutocomplete')

When we search in the autocomplete index using the following query:

[ ](- "ext:embed=getEsQuery()")

  - where the `%prefix%` is: [`tes`](- "#prefix");

[ ](- "#suggestions=searchAutocomplete(#prefix)")

then we get three suggestions: 

  - [Tesseract](- "?=#suggestions.suggestion1") 
  - [teStament](- "?=#suggestions.suggestion2") 
  - [testosterone](- "?=#suggestions.suggestion3") 

Full response:
[ ](- "ext:embed=#suggestions.body")
