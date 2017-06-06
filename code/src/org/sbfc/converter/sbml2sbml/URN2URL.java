/*
 * $Id: URN2URL.java 622 2016-02-05 15:06:27Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2sbml/URN2URL.java $
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
 * Converts an SBML file annotation from Miriam URN to identifiers.org URL
 * 
 * The converter name that you pass to SBFC has to be URN2URL
 * 
 * @author Dalle Pezze
 * @version 1.0
 *
 */
public class URN2URL extends GeneralConverter {

	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {
		
		if (! (model instanceof SBMLModel)) {
			return null;
		}
		inputModel = model;
		SBMLModel sbmlModel = (SBMLModel) model;
		SBMLDocument SBMLdoc = sbmlModel.getSBMLDocument();

		System.out.println("URN2URL : trying to convert annotation from Miriam URN to identifiers.org URL");
		SBMLDocument targetSBMLdoc = IdentifiersUtil.urnToUrl(SBMLdoc);
		
		SBMLModel targetModel = new SBMLModel(targetSBMLdoc);
		return targetModel;
		
	}
		

	@Override
	public String getResultExtension() {
		return "-urn2url.xml";
	}

	@Override
	public String getName() {
		return "URN2URL";
	}
	
	@Override
	public String getDescription() {
		return "It converts an SBML model annotation format from Miriam URN to Identifiers.org URL";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts an SBML model annotation format from Miriam URN to Identifiers.org URL";
	}
	
}
