/*
 * $Id: BioPAXModel.java 548 2015-09-18 14:34:42Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/models/BioPAXModel.java $
 *
 * ==========================================================================
 * This file is part of The System Biology Format Converter (SBFC).
 * Please visit <http://sbfc.sf.net> to have more information about
 * SBFC. 
 * 
 * Copyright (c) 2010-2015 jointly by the following organizations:
 * 1. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 2. The Babraham Institute, Cambridge, UK
 * 3. Department of Bioinformatics, BiGCaT, Maastricht University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as 
 * <http://sbfc.sf.net/mediawiki/index.php/License>.
 * 
 * ==========================================================================
 * 
 */

package org.sbfc.converter.models;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;


/**
 * Creates the link between GeneralModel and the BioPAXModel defined in PaxTools
 * 
 * @author jpettit
 * @author dallepep
 */
public class BioPAXModel implements GeneralModel {

	
	private Model model;

	public BioPAXModel() {
		super();
	}
	
	public BioPAXModel(Model model) {
		super();
		this.model = model;
	}
	
	
	public Model getModel() {
		return model;
	}

	public void setModelFromFile(String fileName) throws ReadModelException {
		this.model = modelFromFile(fileName);
	}
	
	public void setModelFromString(String modelString) throws ReadModelException {
		this.model = modelFromString(modelString);		
	}

	

	/* (non-Javadoc)
	 * Convert BioPAXModel into String
	 * @param model
	 * @see org.sbfc.converter.models.GeneralModel#modelToString(org.sbfc.converter.models.GeneralModel)
	 */
	public void modelToFile(String fileName) throws WriteModelException {	

		SimpleIOHandler export = new SimpleIOHandler(getModel().getLevel());

		try {
			export.convertToOWL(model, new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
		  throw new WriteModelException(e);
		} catch (RuntimeException e) {
		  throw new WriteModelException(e);
		}		
	}

	
	public Model modelFromFile(String fileName) throws ReadModelException {
		SimpleIOHandler handler = new SimpleIOHandler();
		try {
			return handler.convertFromOWL(new FileInputStream(new File(fileName)));
		} catch (FileNotFoundException e) {
			throw new ReadModelException(e);
		}
	}


	public Model modelFromString(String modelString) throws ReadModelException {
	  SimpleIOHandler handler = new SimpleIOHandler();
	  try {
	    return handler.convertFromOWL(new ByteArrayInputStream(modelString.getBytes("UTF-8")));
	  } catch (UnsupportedEncodingException e) {
	    throw new ReadModelException(e);
	  }
	}


	public String modelToString() {
		String resModel=null;
		SimpleIOHandler export = new SimpleIOHandler(getModel().getLevel());
		
		try {
			File tempFile = File.createTempFile("BioPaxConvert", "temp");
			FileOutputStream out  = new FileOutputStream(tempFile);
			export.convertToOWL(model,out); 
			resModel = readFileAsString(tempFile.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resModel;
	}
	
	private static String readFileAsString(String filePath) throws java.io.IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    // TODO: Something is missing here with the following code. What is 'f' for? 
	    FileInputStream f = new FileInputStream(filePath);
	    f.read(buffer);
	    
	    return new String(buffer);
	}


	@Override
	public String[] getExtensions() {
		return new String[] { ".owl" };
	}


	@Override
	public boolean isCorrectType(File f) {
		return true;
	}


	@Override
	public String getURI() {
		return "http://identifiers.org/combine.specifications/biopax";
	}

}
