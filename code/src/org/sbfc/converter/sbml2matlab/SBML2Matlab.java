/*
 * $Id: SBML2Matlab.java 399 2015-07-13 08:43:45Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2matlab/SBML2Matlab.java $
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

package org.sbfc.converter.sbml2matlab;

import org.sbfc.converter.sbml2octave.SBML2Octave;




/**
 * Convert an SBML file into an Matlab file.
 *  
 * @author Piero Dalle Pezze
 * 
 * @version 1.0
 *  
 */
public class SBML2Matlab extends SBML2Octave {

	/**
	 * <b>Constructor SBML2Matlab.</b><br/> Main method of the biological model
	 * export from <a href="http://sbml.org/"><b>SBML</b></a> (Systems Biology
	 * Markup Language) to Matlab</a>.

	 * 
	 */
	public SBML2Matlab() {
		super();

	}

	// The following protected methods differ between Octave and Matlab. 
	// These are overridden in the converter SBML2Matlab 
	protected String headerString() {
		return  "% This file works with MATLAB and is automatically generated with \n" +
				"% the System Biology Format Converter (http://sbfc.sourceforge.net/)\n" +
				"% from an SBML file. \n" +
				"% To run this file with Octave you must edit the comments providing\n" +
				"% the definition of the ode solver and the signature for the \n" +
				"% xdot function.\n" +
				// 	TODO : put the version used
				"%\n" +
				"% The conversion system has the following limitations:\n" +
				"%  - You may have to re order some reactions and Assignment Rules definition\n" +
				"%  - Delays are not taken into account\n" +
				"%  - You should change the lsode parameters (start, end, steps) to get better results\n%\n\n";
		// This seems not to be any longer required by Octave
//				"%\n% NOTE for Octave users ONLY:\n" +
//		        "% To prevent Octave from thinking that this is a function file, \n" + 
//			    "% the following line should be uncommented (instead comment it if using Matlab): \n" +
//				"%1;\n\n";
	}
	
	protected String xdotFunctionSignature() {
		return "% Depending on whether you are using Octave or Matlab,\n" +
			   "% you should comment / uncomment one of the following blocks.\n" +
			   "% This should also be done for the definition of the function f below.\n" +
	           "% Start Matlab code\n" +
			   "function xdot=f(t,x)\n" +
			   "% End Matlab code\n\n" + 

			   "% Start Octave code\n" + 
			   "%function xdot=f(x,t)\n" +
			   "% End Octave code\n\n";
	}
	
	protected String odeSolverCode() {
		return  "% Depending on whether you are using Octave or Matlab,\n" + 
				"% you should comment / uncomment one of the following blocks.\n" +
				"% This should also be done for the definition of the function f below.\n" +
				"% Start Matlab code\n" +
				"\ttspan=[0:0.01:100];\n" +
				"\topts = odeset('AbsTol',1e-3);\n" +
				"\t[t,x]=ode23tb(@f,tspan,x0,opts);\n" +
				"% End Matlab code\n\n" +

				"% Start Octave code\n" +
				"%\tt=linspace(0,100,100);\n" +
				"%\tx=lsode('f',x0,t);\n" +
				"% End Octave code\n\n";
	}
	
	@Override
	public String getName() {
		return "SBML2MATLAB";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to Matlab";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to Matlab";
	}
	
}
