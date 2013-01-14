docx4j-basex
============

work with docx4j files in basex

findings:

- basex automatically unzips a docx, when their CLI basexclient is used,
  but not when their webdav interface is used.  But it drops the _rels dir!

- Basex XMLDB API is broken

- Webdav seems to work with bitkinex

  sardine client get [Content_Types].xml gives premature end of file even though it looks ok via rest
                 put - server doesn't respond
                 
  milton-client just gives a 501 when getting [Content_Types].xml; and it has no method for getting an inputstream
  milton-client has too many dependencies; for all these reasons.  Remove it from build path.
			
- rest interface is OK.  Gotchyas (1) loading .. sniff result. (2) saving .. have to call 
  conn.getResponseCode() or conn.getResponseMessage() .. not sure which (or it doesn't work!)