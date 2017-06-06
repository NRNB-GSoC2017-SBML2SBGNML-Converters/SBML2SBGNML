/*
 * $Id: SBML2BioPAX_l3.java 544 2015-09-18 09:41:39Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2biopax/SBML2BioPAX_l3.java $
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

package org.sbfc.converter.sbml2biopax;



/**
 * Convert an SBML file into a BioPax Level 3 owl file.
 * 
 * 
 * @author Arnaud Henry
 * @author Nicolas Rodriguez
 * @author Camille Laibe
 * 
 * @version 2.3
 * 
 */

public class SBML2BioPAX_l3 extends SBML2BioPAX {
	
	@Override
	public String getName() {
		return "SBML2BioPAX_l3";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to BioPAX L3";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to BioPAX L3";
	}
}
