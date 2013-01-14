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
package com.plutext.basex.webdav;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.httpclient.Folder;
import io.milton.httpclient.Host;
import io.milton.httpclient.HttpException;
import io.milton.httpclient.Resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class BasexWebdavMiltonUnzippedPartStore implements PartStore {
	
	private static Logger log = Logger.getLogger(BasexWebdavMiltonUnzippedPartStore.class);
	
	private static String URI = "/webdav";
		
	String docxColl;  // eg "/db/docx4/apple"
	
	Host host;
	
	String user;
	String password;
	
	public BasexWebdavMiltonUnzippedPartStore(String docxColl, String user, String password) throws Docx4JException {
		
//		if (docxColl.endsWith("/")) {
//			this.docxColl = docxColl;
//		} else {
//			this.docxColl = docxColl + "/";			
//		}
		this.docxColl = docxColl;
		
		try { 
			
			host = new Host("localhost", 8984,
					user, password,null); // null is for no proxy			
			
		} catch (Exception e) {
			throw new Docx4JException(e.getMessage(), e);
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
		
		String url = URI + docxColl + partPrefix + "/" + resource;  // don't URLEncoder.encode(
//		String url = URI + docxColl + partPrefix + "/" + partName;
		System.out.println(url);
				
		try { // get the collection
			
			Resource res = host.find(url);
			return (res!=null);
			
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
				
		String url = URI + docxColl + partPrefix + "/" + resource;  // don't URLEncoder.encode(
//		String url = URI + docxColl + partPrefix + "/" + URLEncoder.encode(resource);
//		String url = URI + docxColl + partPrefix + "/" + partName;
		System.out.println(url);
		try { 			
			Resource res = host.find(url);
			if (res!=null) {
				System.out.println("so far so good");
			}
			return null;
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
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	        ctm.marshal(baos);
	        
//			sardine.put(docxColl + URLEncoder.encode("[Content_Types].xml"), baos.toByteArray());
	        
//	        String url = docxColl + URLEncoder.encode("[Content_Types].xml");
	        String url = URI + docxColl + "ContentTypes.xml";
	        System.out.println(url);
	        // Sardine doesn't like /db2/%5BContent_Types%5D.xml
//			sardine.put(url, baos.toByteArray());
	        
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
		
		String partPrefix="";
		String resource; 
		int pos = partName.lastIndexOf("/");
		if (pos>-1) {
			partPrefix = "/" + partName.substring(0, pos);
			resource = partName.substring(pos+1);
		} else {
			resource = partName;
		}
		System.out.println(docxColl + partPrefix);
		System.out.println(resource);
		
		try {


			if (part.isUnmarshalled() ) {
			
				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		        part.marshal(baos);

//		        		URLEncoder.encode(resource), 
//				sardine.put(docxColl + partPrefix + resource, baos.toByteArray());
		        
	        } else {

	        	if (!partExists(partName)
	        			&& this.sourcePartStore==null) {
	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");
	        	} else {
	        		InputStream is = sourcePartStore.loadPart(partName);
	        		
					ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	        		
	        		int read = 0;
	        		byte[] bytes = new byte[1024];
	        	 
	        		while ((read = is.read(bytes)) != -1) {
	        			baos.write(bytes, 0, read);
	        		}
	        	 
	        		is.close();
	        		
//					sardine.put(docxColl + partPrefix + resource, baos.toByteArray());
	        	 	        		
	        	}
	        	
	        }
	        	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart " + part.getPartName(), e);
		}
	}
	
	public void saveCustomXmlDataStoragePart(CustomXmlDataStoragePart part) throws Docx4JException {
		
		String partName = part.getPartName().getName().substring(1);
		
		String partPrefix="";
		String resource; 
		int pos = partName.lastIndexOf("/");
		if (pos>-1) {
			partPrefix = "/" + partName.substring(0, pos);
			resource = partName.substring(pos+1);
		} else {
			resource = partName;
		}
		System.out.println(docxColl + partPrefix);
		System.out.println(resource);
		
		try {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	        part.getData().writeDocument( baos );

//			sardine.put(docxColl + partPrefix + resource, baos.toByteArray());
	        	        
		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart " + part.getPartName(), e);
		}
	}
	
	public void saveXmlPart(XmlPart part) throws Docx4JException {
	
		String partName = part.getPartName().getName().substring(1);
		
		String partPrefix="";
		String resource; 
		int pos = partName.lastIndexOf("/");
		if (pos>-1) {
			partPrefix = "/" + partName.substring(0, pos);
			resource = partName.substring(pos+1);
		} else {
			resource = partName;
		}
		System.out.println(docxColl + partPrefix);
		System.out.println(resource);
		
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
					.transform(source, new StreamResult(baos));

//			sardine.put(docxColl + partPrefix + resource, baos.toByteArray());

		} catch (Exception e) {
			throw new Docx4JException("Error marshalling JaxbXmlPart "
					+ part.getPartName(), e);
		}
	}
	
	public void saveBinaryPart(Part part) throws Docx4JException {
		
		// Drop the leading '/'
		String partName = part.getPartName().getName().substring(1);
		
		String partPrefix="";
		String resource; 
		int pos = partName.lastIndexOf("/");
		if (pos>-1) {
			partPrefix = "/" + partName.substring(0, pos);
			resource = partName.substring(pos+1);
		} else {
			resource = partName;
		}
		System.out.println(docxColl + partPrefix);
		System.out.println(resource);
		

		try {
	        	        
	        if (((BinaryPart)part).isLoaded() ) {
			
	            java.nio.ByteBuffer bb = ((BinaryPart)part).getBuffer();
	            byte[] bytes = null;
	            bytes = new byte[bb.limit()];
	            bb.get(bytes);	        

//				sardine.put(docxColl + partPrefix + resource, bytes, part.getContentType() );	        
		        
	        } else {
		        
	        	boolean partExists = partExists(partName);
	        	if (!partExists
	        			&& this.sourcePartStore==null) {
	        		
	        		throw new Docx4JException("part store has changed, and sourcePartStore not set");
	        		
	        	} else if (partExists
	        			&& this.sourcePartStore==this) {
	        		
		        	// No need to save

	        	} else {
	    			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        		
	        		InputStream is = sourcePartStore.loadPart(part.getPartName().getName().substring(1));
	        		int read = 0;
	        		byte[] bytes = new byte[1024];
	        		
	        		while ((read = is.read(bytes)) != -1) {
	        			baos.write(bytes, 0, read);
	        		}
	        		is.close();
	        		
//					sardine.put(docxColl + partPrefix + resource,       
//			        		baos.toByteArray(), part.getContentType() );	        

	        	}
	        }

			
		} catch (Exception e ) {
			throw new Docx4JException("Failed to put binary part", e);			
		}
		
		log.info( "success writing part: " + partName);		
		
	}
	
	/**
	 * Does nothing
	 */
	public void finishSave() throws Docx4JException {
		// Nothing to do
	}

}
