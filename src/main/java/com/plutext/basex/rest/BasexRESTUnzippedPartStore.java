/*
 *  Copyright 2012, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package com.plutext.basex.rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.w3c.dom.Document;

/**
 * Load an unzipped package from the file system;
 * save it to some output stream.
 * 
 * @author jharrop
 */
public class BasexRESTUnzippedPartStore implements PartStore {
	
	private static Logger log = Logger.getLogger(BasexRESTUnzippedPartStore.class);
	
	private static String URI = "http://localhost:8984/rest/";
		
	String docxColl;  // eg "/db/docx4/apple"
		
	String user;
	String password;
	
	public BasexRESTUnzippedPartStore(String docxColl, String user, String password) throws Docx4JException {
		
		if (docxColl.endsWith("/")) {
			this.docxColl = docxColl;
		} else {
			this.docxColl = docxColl + "/";			
		}
	}

	private PartStore sourcePartStore;	

	/**
	 * Set this if its different to the target part store
	 * (ie this object)
	 */
	public void setSourcePartStore(PartStore partStore) {
		this.sourcePartStore = partStore;
	}
	
	/////// Load methods

	public boolean partExists(String partName) throws Docx4JException {
		
		String partPrefix="";
		String resource; 
		int pos = partName.lastIndexOf("/");
		if (pos>-1) {
			partPrefix = "/" + partName.substring(0, pos);
			resource = partName.substring(pos+1);
		} else {
			resource = partName;
		}
		System.out.println(URI + docxColl + partPrefix);
		//String urlEncoded = URLEncoder.encode(URI + docxColl + partPrefix);
		System.out.println(resource);
		
		String urlStr = URI + docxColl + partPrefix + "/" + URLEncoder.encode(resource);
//		String url = URI + docxColl + partPrefix + "/" + partName;
		
		
		System.out.println(urlStr);    
		
		try { 			
			URL url = new URL(urlStr);
	
		    // Establish the connection to the URL
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("HEAD");
	
		    // Print the HTTP response code
		    int code = conn.getResponseCode();
		    System.out.println("\n* HTTP response: " + code +
		        " (" + conn.getResponseMessage() + ')');

		    // Check if request was successful
		    return (code == HttpURLConnection.HTTP_OK);
			
		} catch (Exception e) {
			throw new Docx4JException(e.getMessage(), e);
		} 
	}
	
	public InputStream loadPart(String partName) throws Docx4JException {
		
		String partPrefix="";
		String resource; 
		int pos = partName.lastIndexOf("/");
		if (pos>-1) {
			partPrefix = "/" + partName.substring(0, pos);
			resource = partName.substring(pos+1);
		} else {
			resource = partName;
		}
		System.out.println(URI + docxColl + partPrefix);
		System.out.println(resource);
				
		String urlStr = URI + docxColl + partPrefix + "/" + URLEncoder.encode(resource);
//		String url = URI + docxColl + partPrefix + "/" + partName;
		
		
		System.out.println(urlStr);    
		
		try { 			
			URL url = new URL(urlStr);
	
		    // Establish the connection to the URL
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    //conn.setRequestMethod("PUT");
	
		    // Print the HTTP response code
		    int code = conn.getResponseCode();
		    System.out.println("\n* HTTP response: " + code +
		        " (" + conn.getResponseMessage() + ')');

		    // Check if request was successful
		    if(code == HttpURLConnection.HTTP_OK) {
		      InputStream is = new BufferedInputStream(conn.getInputStream());
		      
		      is.mark(0);
		      
		      // DEBUG
//		      java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
//		      System.out.println(s.hasNext()? s.next() : "EMPTY");	
		      
		      // Is it just crap?
		      java.util.Scanner s = new java.util.Scanner(is).useDelimiter("rest:database");
		      
		      String scanned = s.next();
		      if (scanned.equals("<")) {
		    	  is.close();
		    	  return null;
		      }
		      
		      
		      is.reset();
		      
		      return is;
		      
		    } else {
		    	return null;
		    }
			
		} catch (Exception e) {
			throw new Docx4JException(e.getMessage(), e);
		} 
	}

	///// Save methods
	
	/**
	 * Does nothing 
	 */
	public void setOutputStream(OutputStream os) {
		// Nothing to do
	}

	File dir;
	
	public void saveContentTypes(ContentTypeManager ctm) throws Docx4JException {
		
		try {
	        
	        String urlStr = URI + docxColl + URLEncoder.encode("[Content_Types].xml");
//	        String url = URI + docxColl + "ContentTypes.xml";
	        System.out.println(urlStr);

			URL url = new URL(urlStr);
    		OutputStream out = getOutputStream(url);
	        ctm.marshal(out);
	        out.flush();
	        out.close();
	        
	        closeConnection();
	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling Content_Types ", e);
		}
	
	}
	
	public void saveJaxbXmlPart(JaxbXmlPart part) throws Docx4JException {

		String partName;
		if (part.getPartName().getName().equals("_rels/.rels")) {
			partName = part.getPartName().getName();			
		} else {
			partName = part.getPartName().getName().substring(1);						
		}
		
		try {

			String urlStr = URI + docxColl + partName;
			System.out.println(urlStr);
			URL url = new URL(urlStr);

			if (part.isUnmarshalled() ) {
			
	    		OutputStream out = getOutputStream(url);
		        part.marshal(out);

		        out.flush();
		        out.close();
		        
		        closeConnection();
		        
	        } else {

	        	if (!partExists(partName)
	        			&& this.sourcePartStore==null) {
	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");
	        	} else {
	        		InputStream is = sourcePartStore.loadPart(partName);
	        		
		    		OutputStream out = getOutputStream(url);
	        		
	        		int read = 0;
	        		byte[] bytes = new byte[1024];
	        	 
	        		while ((read = is.read(bytes)) != -1) {
	        			out.write(bytes, 0, read);
	        		}
	        	 
	        		is.close();

	    	        out.flush();
	    	        out.close();
	        		
	    	        closeConnection();
	        	 	        		
	        	}
	        	
	        }
	        	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart " + part.getPartName(), e);
		}
	}
	
	private void closeConnection() {
		
        // Print the HTTP response code -- it doesn't work unless you do this
		// (although there is no error).  Very bizarre!!!
        try {
			System.out.println("\n* HTTP response: " + conn.getResponseCode() +
			    " (" + conn.getResponseMessage() + ')');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		conn.disconnect();
		
	}
	
	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part) throws Docx4JException {
		
		String partName = part.getPartName().getName().substring(1);
				
		try {
			
			String urlStr = URI + docxColl + partName;
			System.out.println(urlStr);
			URL url = new URL(urlStr);
    		OutputStream out = getOutputStream(url);

    		part.getData().writeDocument( out );

	        closeConnection();
	        	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart " + part.getPartName(), e);
		}
	}
	
	public void saveXmlPart(XmlPart part) throws Docx4JException {
	
		String partName = part.getPartName().getName().substring(1);
				
		try {
			String urlStr = URI + docxColl + partName;
			System.out.println(urlStr);
			URL url = new URL(urlStr);
    		OutputStream out = getOutputStream(url);

    		Document doc = part.getDocument();

			/*
			 * With Crimson, this gives:
			 * 
				Exception in thread "main" java.lang.AbstractMethodError: org.apache.crimson.tree.XmlDocument.getXmlStandalone()Z
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(DOM2TO.java:373)
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:127)
					at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.parse(DOM2TO.java:94)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transformIdentity(TransformerImpl.java:662)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:708)
					at com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl.transform(TransformerImpl.java:313)
					at org.docx4j.model.datastorage.CustomXmlDataStorageImpl.writeDocument(CustomXmlDataStorageImpl.java:174)
			 * 
			 */
			DOMSource source = new DOMSource(doc);
			XmlUtils.getTransformerFactory().newTransformer()
					.transform(source, new StreamResult(out));

	        closeConnection();

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart "
					+ part.getPartName(), e);
		}
	}
	
	public void saveBinaryPart(Part part) throws Docx4JException {
		
		// Drop the leading '/'
		String partName = part.getPartName().getName().substring(1);
		
		try {
			String urlStr = URI + docxColl + partName;
			System.out.println(urlStr);
			URL url = new URL(urlStr);
	        	        
	        if (((BinaryPart)part).isLoaded() ) {
			
	            java.nio.ByteBuffer bb = ((BinaryPart)part).getBuffer();
	            byte[] bytes = null;
	            bytes = new byte[bb.limit()];
	            bb.get(bytes);	        

        		OutputStream out = getOutputStream(url);
        		conn.setRequestProperty("Content-Type", part.getContentType() );
        		out.write(bytes);
        		
    	        closeConnection();
		        
	        } else {
		        
	        	boolean partExists = partExists(partName);
	        	if (!partExists
	        			&& this.sourcePartStore==null) {
	        		
	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");
	        		
	        	} else if (partExists
	        			&& this.sourcePartStore==this) {
	        		
		        	// No need to save

	        	} else {
	        		OutputStream out = getOutputStream(url);
	        		
	        		InputStream is = sourcePartStore.loadPart(part.getPartName().getName().substring(1));
	        		int read = 0;
	        		byte[] bytes = new byte[1024];
	        		
	        		while ((read = is.read(bytes)) != -1) {
	        			out.write(bytes, 0, read);
	        		}
	        		is.close();

	    	        closeConnection();

	        	}
	        }

			
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}
		
		log.info( "success writing part: " + partName);		
		
	}
	HttpURLConnection conn;
	
	  private OutputStream getOutputStream(URL url) throws IOException {

		    // Establish the connection to the URL
		    conn = (HttpURLConnection) url.openConnection();
		    // Set an output connection
		    conn.setDoOutput(true);
		    // Set as PUT request
		    conn.setRequestMethod("PUT");

		    // Get and cache output stream
		    OutputStream out = new BufferedOutputStream(conn.getOutputStream());
//		    OutputStream out = conn.getOutputStream();
		    
		    return out;
		    
		  }
	
	
	/**
	 * Does nothing
	 */
	public void finishSave() throws Docx4JException {
		// Nothing to do
	}

}
