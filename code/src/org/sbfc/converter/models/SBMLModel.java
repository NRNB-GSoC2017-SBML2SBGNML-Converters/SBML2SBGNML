/*
 * $Id: SBMLModel.java 616 2016-02-04 16:36:18Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/models/SBMLModel.java $
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

import org.pathvisio.core.util.RootElementFinder;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.TidySBMLWriter;


/**
 * Class creating the link between GeneralModel and the SBML definition in JSBML
 * 
 * @author jpettit
 * @author rodrigue
 *
 */
public class SBMLModel implements GeneralModel {

	private SBMLDocument document;
	private String fileName;
	
	/**
	 * Class constructor
	 */
	public SBMLModel() {
		super();
	}

	public SBMLModel(SBMLDocument value) {
		document = value;
	}
	
	public Model getModel() {
		if (document != null) {
			return document.getModel();
		}
		
		return null;
	}

	public SBMLDocument getSBMLDocument() {
		if (document != null) {
			return document;
		}
		
		return null;
	}
	
	public void setModelFromFile(String fileName) throws ReadModelException{
		this.document = modelFromFile(fileName);
	}
	

	public void setModelFromString(String modelString) throws ReadModelException {
		this.document = modelFromString(modelString);
	}

	
	@Override
	public String[] getExtensions() {
		return new String[] { ".xml", ".sbml" }; // .xml is preferred.
	}


	public void modelToFile(String fileName) throws WriteModelException {

		try {
			new TidySBMLWriter().writeSBMLToFile(document, fileName);
		} catch (XMLStreamException e) {
			throw new WriteModelException(e);
		} catch (IOException e) {
			throw new WriteModelException(e);
		} catch (SBMLException e) {
			throw new WriteModelException(e);
		}
	}

	public SBMLDocument modelFromFile(String fileName) throws ReadModelException {
		
		try {
			document = JSBML.readSBML(fileName);
			this.fileName = fileName;
		} catch (XMLStreamException e) {
			throw new ReadModelException(e);
		} catch (IOException e) {
			throw new ReadModelException(e);
		}
		return document;
	}


	public SBMLDocument modelFromString(String modelString) throws ReadModelException {
		
		try {
			document = JSBML.readSBMLFromString(modelString);
			fileName = null;
		} catch (XMLStreamException e) {
			throw new ReadModelException(e);
		}
		return document;
	}

	@Override
	public String modelToString() throws WriteModelException {
		String reString =null;
		
		try {
			reString = new TidySBMLWriter().writeSBMLToString(document);
		} catch (XMLStreamException e) {
			throw new WriteModelException(e);
		} catch (SBMLException e) {
			throw new WriteModelException(e);
		}

		return reString;
	}


	/**
	 * Returns the file name that was used to set this {@link GeneralModel} if
	 * it was set using the {@link #setModelFromFile(String)} method, null otherwise.
	 * 
	 * @return the file name that was used to set this {@link GeneralModel} if
	 * it was set using the {@link #setModelFromFile(String)} method, null otherwise.
	 */
	public String getModelFileName() {
		return fileName;
	}


	@Override
	public boolean isCorrectType(File f) {
		String uri;
		try
		{
			// TODO: This depends on Pathvisio. See if it is possible to remove this dependency.
			uri = "" + RootElementFinder.getRootUri(f);
			return uri.startsWith ("http://www.sbml.org/");
		}
		catch (Exception e)
		{
			return false;
		}
	}

	
	@Override
	public String getURI() {
		return "http://identifiers.org/combine.specifications/sbml";
	}	

}
