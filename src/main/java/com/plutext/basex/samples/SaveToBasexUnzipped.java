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

package com.plutext.basex.samples;


import org.docx4j.openpackaging.io3.Load3;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.io3.stores.PartStore;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.samples.AbstractSample;

import com.plutext.basex.xmldb.BasexXMLDBUnzippedPartStore;


/**
 * Example of loading an unzipped file from the file system.
 * 
 * @author jharrop
 * @since 3.0
 *
 */
public class SaveToBasexUnzipped extends AbstractSample {
	
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/OUT";
		}		
		
		// Create a docx
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		mdp.addStyledParagraphOfText("Title", "Hello basex are you working?");
		
		// Save it unzipped
		PartStore zps = 
//				new com.plutext.basex.webdav.BasexWebdavMiltonUnzippedPartStore("/db2", "admin", "admin");
//				new com.plutext.basex.webdav.BasexWebdavSardineUnzippedPartStore("/db2", "admin", "admin");
//				new BasexXMLDBUnzippedPartStore("/test1", "admin", "admin");
				new com.plutext.basex.rest.BasexRESTUnzippedPartStore("/db2/zip7", "admin", "admin");
		zps.setSourcePartStore(wordMLPackage.getPartStore());
		
		Save saver = new Save(wordMLPackage, zps);
		saver.save(null);
		
	}
		

}
