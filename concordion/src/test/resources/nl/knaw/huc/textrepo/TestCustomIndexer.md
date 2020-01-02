# TestCustomIndexer

Testing the autocomplete indexer

## [Uploading files](- 'upload')

When files with contents 
"[hello Tesseract](- "#text1")", 
"[hello teStament](- "#text2")" and 
"[hello testosterone](- "#text3")" 
are [```POST```ed](- "#file=upload(#text1, #text2, #text3)"),
then we should have three [valid UUIDs](- "?=#file.validUuids"):

  - [ ](- "c:echo=#file.validUuid1")
  - [ ](- "c:echo=#file.validUuid2")
  - [ ](- "c:echo=#file.validUuid3")

## [Searching with autocomplete](- 'searchAutocomplete')

When "[tes](- "#pre")" 
is [searched for](- "#suggestions=searchAutocomplete(#pre)") in the autocomplete index, 
we get three suggestions: 

  - [Tesseract](- "?=#suggestions.suggestion1") 
  - [teStament](- "?=#suggestions.suggestion2") 
  - [testosterone](- "?=#suggestions.suggestion3") 
