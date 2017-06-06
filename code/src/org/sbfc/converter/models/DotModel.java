/*
 * $Id: DotModel.java 390 2015-07-09 13:29:56Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/models/DotModel.java $
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

import org.sbfc.converter.exceptions.ReadModelException;

import java.io.File;


public class DotModel extends StringModel {
	
	public DotModel() {
		
	}
	
	public DotModel(String model) throws ReadModelException {
		setModelFromString(model);
	}
		
	@Override
	public String[] getExtensions() {
		return new String[] { ".dot" };
	}

	@Override
	public boolean isCorrectType(File f) {
		return true;
	}

	@Override
	public String getURI() {
		return "text/vnd.graphviz";
	}
}
