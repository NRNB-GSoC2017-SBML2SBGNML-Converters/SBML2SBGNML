/*
 * $Id: BioPAX2BioPAXL3.java 630 2016-03-08 17:07:11Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/biopaxL3converter/BioPAX2BioPAXL3.java $
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

package org.sbfc.converter.biopaxL3converter;

import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.BioPAXModel;


/**
 * Convert a model from BioPAX L1/L2 to BioPAX L3.
 * 
 * 
 * @author Nicolas Rodriguez
 * @author Piero Dalle Pezze
 * 
 * @version 1.0
 * 
 */
public class BioPAX2BioPAXL3 extends GeneralConverter {

	
	/**
	 * Convert a model from BioPax L1/L2 to BioPax L3.
	 * 
	 * @param model to be converted
	 * @return a BioPax L3 model
	 * @throws ConversionException
	 * @throws ReadModelException
	 */
	public GeneralModel biopaxExport(BioPAXModel model)
			throws ConversionException, ReadModelException {

		if (model.getModel().getLevel().equals(BioPAXLevel.L1) ||
			model.getModel().getLevel().equals(BioPAXLevel.L2)) {

			LevelUpgrader levelUpgrader = new LevelUpgrader();
			Model level3Model = levelUpgrader.filter(model.getModel());		
			BioPAXModel bioPaxModelOut = new BioPAXModel(level3Model);
			
			return bioPaxModelOut;
		}
		
		// we return the model if it is already L3
		return model;
	}

	
	@Override
	public GeneralModel convert(GeneralModel model) 
	throws ConversionException, ReadModelException {
		try {
			inputModel = model;
			return biopaxExport((BioPAXModel)model);
		} catch (ReadModelException e) {
			throw e;
		} catch (ConversionException e) {
			throw e;
		}
	}

	@Override
	public String getResultExtension() {
		return "-biopaxL3.owl";
	}
	
	@Override
	public String getName() {
		return "BioPAX to BioPAX L3";
	}
	
	@Override
	public String getDescription() {
		return "Converts a model from BioPAX L1/L2 to BioPAX L3, using paxtools.";
	}

	@Override
	public String getHtmlDescription() {
		return "Converts a model from <a href=\"http://www.biopax.org\">BioPAX</a> L1/L2 to <a href=\"http://www.biopax.org\">BioPAX</a> L3, using <a href=\"http://www.biopax.org\">paxtools</a>.";
	}

}
