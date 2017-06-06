/*
 * $Id: GeneralModel.java 390 2015-07-09 13:29:56Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/models/GeneralModel.java $
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

import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;



/**
 * Interface defining the specifications that each Model must implement.
 * @author jpettit
 * @author rodrigue
 * @author pdp10
 */
public interface GeneralModel {
	
	/**
	 * Set the Model from a file in the file system.
	 * @param fileName path to the file containing the model
	 * @throws ReadModelException
	 */
	public void setModelFromFile(String fileName) throws ReadModelException;
	
	/**
	 * Set the model from a String.
	 * @param modelString Model
	 * @throws ReadModelException
	 */
	public void setModelFromString(String modelString) throws ReadModelException;
	
	/**
	 * Write the Model into a new file.
	 * @param fileName path at which the new file will be created
	 * @throws WriteModelException
	 */
	public void modelToFile(String fileName) throws WriteModelException;
	
	/**
	 * Return the Model as a String.
	 * @return Model
	 * @throws WriteModelException
	 */
	public String modelToString() throws WriteModelException;
	
	/**
	 * Returns an array of model file type extension (ex: [.xml, .sbml] for SBML, [.owl] for BIOPAX)
	 * The first is the preferred extension.
	 *
	 * @return file type extensions
	 */
	public String[] getExtensions();
	
	/**
	 * This method is used to distinguish between converters with the same file extension.
	 * For example, a file ending with .xml could be either an SBML model, or some other XML file type. 
	 * <p>
	 * Implementers should perform a quick heuristic, not a full validation. For example
	 * XML file types may examine the root element to determine if it has the correct name or namespace.
	 * <p>
	 * Implementers should <b>only</b> return false if they are <b>certain</b> that the file
	 * type is wrong. If the correctness could not be determined for sure, the method should 
	 * always return true. 
	 * @return false if the file is not the correct type to be used with this GeneralModel
	 */
	public boolean isCorrectType(File f);

	/**
	 * returns a URI for the model
	 * e.g. MIME types: image/png, application/matlab, text/xpp
	 * e.g. COMBINE spec ids: http://identifiers.org/combine.specifications/sbml
	 * 
	 * @return the model URI
	 */
	public String getURI();
	
	
}
