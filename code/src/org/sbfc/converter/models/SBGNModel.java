/*
 * $Id: SBGNModel.java 420 2015-07-29 14:53:38Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/models/SBGNModel.java $
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

import java.io.File;

import javax.xml.bind.JAXBException;

import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;
import org.sbfc.converter.sbml2sbgnml.SBML2SBGNML;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.SBMLDocument;

/**
 * 
 * @author jalowcki
 *
 */
public class SBGNModel implements GeneralModel {
	
	private Sbgn sbgnModel;
	private String fileName;
	
	public SBGNModel(){
		super();
	}
	
	public SBGNModel(Sbgn model) {
		super();
		this.sbgnModel = model;
	}
	
	@Override
	public void modelToFile(String fileName) throws WriteModelException {
		File f = new File(fileName);
		try {
			SbgnUtil.writeToFile(sbgnModel, f);
		} catch (JAXBException e) {
			throw new WriteModelException(e);
		} catch(Exception e) {
			throw new WriteModelException(e);
		}
	}

	@Override
	public String modelToString() throws WriteModelException {
		// TODO Is that necessary to do this function?
		return null;
	}

	@Override
	public void setModelFromFile(String fileName) throws ReadModelException {
		this.sbgnModel = modelFromFile(fileName);
	}


	@Override
	public void setModelFromString(String modelString) throws ReadModelException {
		this.sbgnModel = modelFromString(modelString);		
	}

	/**
	 * Return a {@link Sbgn} model from a filename
	 * @param modelString
	 * @return
	 */
	private Sbgn modelFromString(String modelString) throws ReadModelException {
		SBMLModel model = new SBMLModel();
		SBMLDocument sbmlDoc = model.modelFromString(modelString);
		SBML2SBGNML converter = new SBML2SBGNML();
		Sbgn sbgnModel = converter.convertSBGNML(sbmlDoc);
		return sbgnModel;
	}


	/**
	 * Return a {@link Sbgn} model from a filename
	 * @param fileName2
	 * @return
	 */
	private Sbgn modelFromFile(String fileName2) throws ReadModelException {
		// we have to load a SBML model from the file...
		SBMLModel model = new SBMLModel();
		
		SBMLDocument sbmlDoc = model.modelFromFile(fileName2);

		// ... and then call a Sbgn object.
		SBML2SBGNML converter = new SBML2SBGNML();
		
		Sbgn sbgnModel = converter.convertSBGNML(sbmlDoc);
		
		return sbgnModel;
	}

	@Override
	public String[] getExtensions() {
		return new String[] { ".xml", ".sbgn", ".sbgnml" }; // .xml is preferred.
	}

	@Override
	public boolean isCorrectType(File f) {
		return true;
	}

	@Override
	public String getURI() {
		return "application/sbgn";
	}
	
}
