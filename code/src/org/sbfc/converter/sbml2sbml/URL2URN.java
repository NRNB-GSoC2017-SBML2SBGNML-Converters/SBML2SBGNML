/*
 * $Id: URL2URN.java 622 2016-02-05 15:06:27Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2sbml/URL2URN.java $
 *
 * ==========================================================================
 * This file is part of The System Biology Format Converter (SBFC).
 * Please visit <http://sbfc.sf.net> to have more information about
 * SBFC. 
 * 
 * Copyright (c) 2010-2016 jointly by the following organizations:
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
package org.sbfc.converter.sbml2sbml;

import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbml.jsbml.SBMLDocument;


/**
 * Converts an SBML file annotation from identifiers.org URL to Miriam URN
 * 
 * The converter name that you pass to SBFC has to be URL2URN
 * 
 * @author Dalle Pezze
 * @version 1.0
 *
 */
public class URL2URN extends GeneralConverter {

	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {
		
		if (! (model instanceof SBMLModel)) {
			return null;
		}
		inputModel = model;
		SBMLModel sbmlModel = (SBMLModel) model;
		SBMLDocument SBMLdoc = sbmlModel.getSBMLDocument();

		System.out.println("URL2URN : trying to convert annotation from identifiers.org URL to Miriam URN");
		SBMLDocument targetSBMLdoc = IdentifiersUtil.urlToUrn(SBMLdoc);
				
		SBMLModel targetModel = new SBMLModel(targetSBMLdoc);
		return targetModel;
	}
		
	@Override
	public String getResultExtension() {
		return "-url2urn.xml";
	}

	@Override
	public String getName() {
		return "URL2URN";
	}
	
	@Override
	public String getDescription() {
		return "It converts an SBML model annotation format from Identifiers.org URL to Miriam URN";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts an SBML model annotation format from Identifiers.org URL to Miriam URN";
	}
	
}
