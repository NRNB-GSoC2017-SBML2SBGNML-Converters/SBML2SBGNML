/*
 * $Id: GeneralConverter.java 399 2015-07-13 08:43:45Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/GeneralConverter.java $
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

package org.sbfc.converter;

import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;

import java.util.Map;

import org.sbfc.converter.models.GeneralModel;


/**
 * Abstract class defining the specifications that each Converter must implement.
 * 
 * @author jpettit
 * @author rodrigue
 * @author pdp10
 */
public abstract class GeneralConverter {
	
	/**
	 * The input model to be converted.
	 */
	protected GeneralModel inputModel = null;
	
	/**
	 * The options for the converter. Each option is defined as a pair (name, value). 
	 * For instance, for the converter SBML2SBML, one option is ("sbml.target.level", "3").
	 */
	protected Map<String, String> options;

	/**
	 * Method to convert a GeneralModel into another.
	 * @param model
	 * @return GeneralModel
	 */
	public abstract GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException;
	
	/**
	 * Return the extension of the Result file.
	 * @return String
	 */
	public abstract String getResultExtension();
	
	/**
	 * Set the converter options.
	 * @param options
	 */
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	/** 
	 * Return the input model.
	 * @return the input model
	 */
	public GeneralModel getInputModel() {
		return inputModel;
	}
	
	/**
	 * Return the converter name as it should be displayed.
	 * @return the name
	 */
	public abstract String getName();
	
	/**
	 * Return the converter description.
	 * @return the description
	 */
	public abstract String getDescription();

	/**
	 * Return the converter description in HTML format.
	 * @return the HTML description
	 */
	public abstract String getHtmlDescription();
	
} 
