/*
 * $Id: SBML2SBML.java 631 2016-03-09 11:52:19Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2sbml/SBML2SBML.java $
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
package org.sbfc.converter.sbml2sbml;


import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;


/**
 * Converts an SBML file into an other SBML level and version.
 * 
 * The converter name that you pass to SBFC as to be SBML2SBML_LxVx where LxVx is the targeted
 * SBML level and version.  
 * 
 * @author rodrigue
 * @version 1.0
 *
 */
public class SBML2SBML extends GeneralConverter {

	int targetLevel = -1;
	int targetVersion = -1;

    private static boolean isLibSBMLAvailable = false;

	static {
		try {
            System.loadLibrary("sbmlj");

            Class.forName("org.sbml.libsbml.libsbml");
        
            isLibSBMLAvailable = true;

		} catch (SecurityException e) {
            // System.out.println("SecurityException exception catched : Could not load libsbml library.");
		    throw e;
        } catch (UnsatisfiedLinkError e) { 
          // always sending an exception so that the SBFC framework know there is a problem and the actual exception message can be displayed
          // System.out.println("UnsatisfiedLinkError exception catched : Could not load libsbml library.");
          // System.out.println("You need to install libsbml before being able to use the SBML2SBML converter.");
          throw new RuntimeException("You need to install libsbml before being able to use the SBML2SBML converter.", e);
        } catch (ClassNotFoundException e) {
        	// e.printStackTrace();
            // System.out.println("ClassNotFoundException exception catched : Could not load libsbml class file.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out
                    .println("Could not load libsbml.\n "
                            + "Control that the libsbmlj.jar that you are using is synchronized with your current libSBML installation.");

        }
	}

	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {
		
		if (! (model instanceof SBMLModel)) {
			return null;
		}
		inputModel = model;
		SBMLModel sbmlModel = (SBMLModel) model;
		
		SBMLDocument sbmlDocument = sbmlModel.getSBMLDocument();
		
		int currentLevel = sbmlDocument.getLevel();
		int currentVersion = sbmlDocument.getVersion();
		
		try {
			targetLevel = Integer.parseInt(options.get("sbml.target.level"));
			targetVersion = Integer.parseInt(options.get("sbml.target.version"));
			
		} catch (NumberFormatException e) {
			// return as we are not able to get the target level and version
			System.out.println("SBML2SBML : cannot read the target level and version : " + e.getMessage());
			return null;
		}
		
		if (targetLevel != currentLevel || targetVersion != currentVersion) {

			String currentSBML;
			try {
				currentSBML = sbmlModel.modelToString();
				
				System.out.println("SBML2SBML : current model size = " + currentSBML.length());
				
//				if (currentSBML != null && currentSBML.length() > 151) {
//				  System.out.println("SBML2SBML : current model : \n" + currentSBML.substring(0, 150));
//				}
//				System.out.println();
			} catch (WriteModelException e1) {
				e1.printStackTrace();
				return null;
			}
			
			if (isLibSBMLAvailable) {
				// Code using libSBML directly
				org.sbml.libsbml.SBMLReader libSBMLReader = new SBMLReader();
				
				org.sbml.libsbml.SBMLDocument libSBMLdoc = libSBMLReader.readSBMLFromString(currentSBML);
				
				System.out.println("SBML2SBML : trying to convert to SBML level " + targetLevel + " version " + targetVersion);
				
				System.out.println("SBML2SBML : L1V2 compatibility = " + libSBMLdoc.checkL1Compatibility());
				
				boolean isSetLVSuccesfull = libSBMLdoc.setLevelAndVersion(targetLevel, targetVersion);
				
				// libsbml.LIBSBML_OPERATION_SUCCESS
				
				System.out.println("SBML2SBML : setLevelAndVersion worked = " + isSetLVSuccesfull);				
				
				// TODO : if setLevelAndVersion returned false, the conversion is not possible
				// and we need to return the list of errors found by libSBML
				// Could be written in the notes of an empty sbml element
				
				if (!isSetLVSuccesfull) {
				  System.out.println("SBML2SBML - Conversion was not possible, here are the errors returned by libSBML:");
				  libSBMLdoc.printErrors();
				}
				
				org.sbml.libsbml.SBMLWriter libSBMLWriter = new SBMLWriter();

				String targetSBML = libSBMLWriter.writeSBMLToString(libSBMLdoc);

				// System.out.println("SBML2SBML : converted model : \n" + targetSBML.substring(0, 150));
				
				SBMLModel targetModel = new SBMLModel();
				try {
					targetModel.modelFromString(targetSBML);

					return targetModel;

				} catch (ReadModelException e) {
					e.printStackTrace();
					return null;
				}
			}
			
			// if libsbml is not available or there has been an exception
			return null;
			
			/*
			// Code with the libSBML WS
			LibSBMLServiceLocator service = new LibSBMLServiceLocator();

			try {

				LibSBML client = service.getLibSBML();
			
				String targetSBML = client.convertSBML(currentSBML, targetLevel, targetVersion);
				
				SBMLModel targetModel = new SBMLModel();
				targetModel.modelFromString(targetSBML);
				
				return targetModel;
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			} catch (ServiceException e) {
				e.printStackTrace();
				return null;
			} catch (ReadModelError e) {
				e.printStackTrace();
				return null;
			}
			*/
		}
		
		// we are here because the targeted level and version are the same as the original model so we return it
		return model;
	}

	@Override
	public String getResultExtension() {
		return "-L" + targetLevel + "V" + targetVersion + ".xml";
	}

	@Override
	public String getName() {
		return "SBML2SBML";
	}
	
	@Override
	public String getDescription() {
		return "Converts an SBML model format to another with different Level/Version";
	}

	@Override
	public String getHtmlDescription() {
		return "Converts an SBML model format to another with different Level/Version";
	}
	
}
