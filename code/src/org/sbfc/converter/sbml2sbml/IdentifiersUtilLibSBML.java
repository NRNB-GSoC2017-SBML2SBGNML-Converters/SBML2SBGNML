/*
 * $Id: IdentifiersUtilLibSBML.java 603 2016-01-28 17:08:46Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2sbml/IdentifiersUtilLibSBML.java $
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.sbml.libsbml.CVTerm;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.UnitDefinition;
import org.identifiers.registry.RegistryLocalProvider;

/**
 * This class will provide utility methods to transform all the annotations in am SBML model to use either only identifiers.org URL
 * or miriam URN.
 * 
 * @author rodrigue
 *
 */
public class IdentifiersUtilLibSBML {

	static private String urnPrefix = "urn:miriam";
	static private String urlPrefix = "http://identifiers.org";
	
	static private RegistryLocalProvider link; 
	
	static {
		// Creation of the link to the Web Services
        // link = new MiriamLink();
        link = new RegistryLocalProvider();

        // Sets the address to access the Web Services
        // link.setAddress("http://www.ebi.ac.uk/miriamws/main/MiriamWebServices");

	}

	private static boolean isLibSBMLAvailable = false;

	static {
		try {
            System.loadLibrary("sbmlj");

            Class.forName("org.sbml.libsbml.libsbml");
        
            isLibSBMLAvailable = true;

		} catch (SecurityException e) {
        	// e.printStackTrace();
            // System.out.println("SecurityException exception catched : Could not load libsbml library.");
		  throw e;
        } catch (UnsatisfiedLinkError e) {
        	// e.printStackTrace();
            // System.out.println("UnsatisfiedLinkError exception catched : Could not load libsbml library.");
            // System.out.println("You need to install libsbml before being able to use the IdentifiersUtilLibSBML class.");
          throw new RuntimeException("You need to install libsbml before being able to use the IdentifiersUtilLibSBML class.", e);
        } catch (ClassNotFoundException e) {
        	// e.printStackTrace();
            System.out.println("ClassNotFoundException exception catched : Could not load libsbml class file.");
        } catch (RuntimeException e) {
            // e.printStackTrace();
            System.out
                    .println("Could not load libsbml.\n "
                            + "Control that the libsbmlj.jar that you are using is synchronized with your current libSBML installation.");
        }
	}

	
	static private void listOfAnnotationsUpdate(ListOf listOf, String annotationPrefix) {

		if (listOf == null) {
			return;			
		}
		
		sBaseAnnotationsUpdate(listOf, annotationPrefix);
		
		long n = listOf.size();
		
		for (int i = 0; i < n; i++) {
			SBase sbase = listOf.get(i);
			sBaseAnnotationsUpdate(sbase, annotationPrefix);
		}
		
		
	}

	static private void sBaseAnnotationsUpdate(SBase sbase, String annotationPrefix) {

		if (sbase.getNumCVTerms() > 0) {
			
			boolean urn2url = false;
			boolean url2urn = false;
			boolean update = false;
			
			if (annotationPrefix == null) {
				update = true;
			} else if (annotationPrefix.equals(urlPrefix)) {
				url2urn = true;
			} else if (annotationPrefix.equals(urnPrefix)) {
				urn2url = true;
			}
			
			long n = sbase.getNumCVTerms();
			
			for (int i = 0; i < n; i++) {
				
				CVTerm cvTerm = sbase.getCVTerm(i);
				List<String> updatedUris = new ArrayList<String>();
				boolean updated = false;
				long nbURIs = cvTerm.getNumResources();
				
				for (int j = 0; j < nbURIs; j++ ) {
					
					String annotationString = cvTerm.getResourceURI(j);
					String newURI = annotationString;
					
					if ((url2urn || urn2url) && (!annotationString.startsWith(annotationPrefix))) {

						// using Miriam web service to transform it !?
						if (url2urn) {
							newURI = link.convertURN(annotationString);
						} else if (urn2url) {
							// TODO : add a test to convertURL from identifiers.org when the method is available in the webService
							// newURI = link.convertURL(annotationString);
							continue;
						} 
					} else if (update) {
						
						newURI = link.getMiriamURI(annotationString);
					}

					if (newURI == null) {
						System.out.println("Error : the uri '" + annotationString + "' is not recognized by miriamws !!");
						newURI = annotationString;
					} else if (! newURI.equals(annotationString)) {
						updated = true;
					}
					updatedUris.add(newURI);
				}

				if (updated) {
					
					// TODO : update the uris
					// uris.clear();
					// uris.addAll(updatedUris);
				}
			}
		}

		/*
		// L3 packages elements
		if (sbase.isExtendedByOtherPackages()) {

			for (SBasePlugin extSBase : sbase.getExtensionPackages().values()) {
				for (int i = 0; extSBase.getChildCount() < i; i++) {
					
					Object child = extSBase.getChildAt(i);
					
					if (child instanceof ListOf<?>) {
						listOfAnnotationsUpdate((ListOf<?>) extSBase.getChildAt(i), annotationPrefix);
					} else if (child instanceof SBase) {
						sBaseAnnotationsUpdate((SBase) extSBase.getChildAt(i), annotationPrefix);
					}
				}
			}
		}
		*/
	}

	/**
	 * 
	 * 
	 * @return
	 */
	static public SBMLDocument urnToUrl(SBMLDocument doc){
		return documentAnnotationsUpdate(doc, urlPrefix);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	static public SBMLDocument urlToUrn(SBMLDocument doc){
		return documentAnnotationsUpdate(doc, urnPrefix);
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	static public SBMLDocument updateAnnotations(SBMLDocument doc){
		return documentAnnotationsUpdate(doc, null);
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	static private SBMLDocument documentAnnotationsUpdate(SBMLDocument doc, String annotationPrefix){

		sBaseAnnotationsUpdate(doc, annotationPrefix);

		Model model = doc.getModel();
		sBaseAnnotationsUpdate(model, annotationPrefix);

		listOfAnnotationsUpdate(model.getListOfFunctionDefinitions(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfUnitDefinitions(), annotationPrefix);
		
		// unit annotations
		long n = model.getListOfUnitDefinitions().size();
		for (int i = 0; i < n; i++) {
			UnitDefinition unitDefinition = model.getUnitDefinition(i);
			listOfAnnotationsUpdate(unitDefinition.getListOfUnits(), annotationPrefix);
		}
		
		listOfAnnotationsUpdate(model.getListOfSpeciesTypes(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfCompartmentTypes(), annotationPrefix);
		
		listOfAnnotationsUpdate(model.getListOfSpecies(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfCompartments(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfParameters(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfInitialAssignments(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfRules(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfConstraints(), annotationPrefix);
		listOfAnnotationsUpdate(model.getListOfReactions(), annotationPrefix);
		
		// reaction sub-element annotations
		n = model.getListOfReactions().size();
		for (int i = 0; i < n; i++) {
			Reaction reaction = model.getReaction(i);
			listOfAnnotationsUpdate(reaction.getListOfModifiers(), annotationPrefix);
			listOfAnnotationsUpdate(reaction.getListOfProducts(), annotationPrefix);
			listOfAnnotationsUpdate(reaction.getListOfReactants(), annotationPrefix);
			
			if (reaction.isSetKineticLaw()) {
				KineticLaw kineticLaw = reaction.getKineticLaw();
				
				sBaseAnnotationsUpdate(kineticLaw, annotationPrefix);
				listOfAnnotationsUpdate(kineticLaw.getListOfLocalParameters(), annotationPrefix);				
			}
		}
		
		listOfAnnotationsUpdate(model.getListOfEvents(), annotationPrefix);

		// event sub-element annotations
		n = model.getListOfEvents().size();
		for (int i = 0; i < n; i++) {
			Event event = model.getEvent(i);
		
			listOfAnnotationsUpdate(event.getListOfEventAssignments(), annotationPrefix);
		}

		return doc;
	}

	public static void main(String[] args) {
		
		if (!isLibSBMLAvailable) {
			System.out.println("LibSBML does not seems to be correctly configured !!");
			System.exit(2);
		}
		
		if (args.length < 2) {
			System.out.println(
			  "Usage : java org.sbml.jsbml.xml.stax.SBMLWriter [-m|-i|-u] sbmlFileName [suffix]");
			System.out.println("\n\t\tThe order of the options is important.");
			System.out.println("\n\t\t-m will update the given sbml file to use miriam urn-uris");
			System.out.println("\n\t\t-i will update the given sbml file to use miriam url-uris (identifiers.org urls)");
			System.out.println("\n\t\t-u will update the given sbml file to the correct and up-to-date miriam urn-uris");

			System.exit(1);
		}

		long init = Calendar.getInstance().getTimeInMillis();
		System.out.println(Calendar.getInstance().getTime());
		
		String annoPrefixOption = args[0];
		String annotationPrefix = urlPrefix;
		String fileName = args[1];
		String fileNameSuffix = "-identifiers.org"; 
		
		if (annoPrefixOption.equals("-m")) {
			fileNameSuffix = "-miriam-urn";
			annotationPrefix = urnPrefix;
		} else if (annoPrefixOption.equals("-u")) {
			fileNameSuffix = "-updated-annotations";
			annotationPrefix = null;			
		}
		
		if (args.length >= 3) {
			fileNameSuffix = args[3];
		}

		String jsbmlWriteFileName = fileName.replaceFirst(".xml", fileNameSuffix + ".xml");
		
		System.out.printf("Reading %s and writing %s\n", 
		  fileName, jsbmlWriteFileName);

		SBMLDocument testDocument;
		long afterRead = 0;
		long afterAnnoUpdate = 0;


		testDocument = new SBMLReader().readSBMLFromFile(fileName);
		System.out.printf("Reading done\n");
		System.out.println(Calendar.getInstance().getTime());
		afterRead = Calendar.getInstance().getTimeInMillis();

		documentAnnotationsUpdate(testDocument, annotationPrefix);
		afterAnnoUpdate = Calendar.getInstance().getTimeInMillis();

		System.out.printf("Starting writing\n");

		new SBMLWriter().writeSBMLToFile(testDocument, jsbmlWriteFileName);

		System.out.println(Calendar.getInstance().getTime());
		long end = Calendar.getInstance().getTimeInMillis();
		long nbSecondes = (end - init)/1000;
		long nbSecondesRead = (afterRead - init)/1000;
		long nbSecondesAnnoUpdate = (afterAnnoUpdate - afterRead)/1000;
		long nbSecondesWrite = (end - afterAnnoUpdate)/1000;
		
		if (nbSecondes > 120) {
			System.out.println("It took " + nbSecondes/60 + " minutes.");
		} else {
			System.out.println("It took " + nbSecondes + " secondes.");			
		}
		System.out.println("Reading : " + nbSecondesRead + " secondes.");
		System.out.println("Writing : " + nbSecondesWrite + " secondes, AnnoUpdate : " + nbSecondesAnnoUpdate + " secondes.");

	}
	
	
	/**
	 * 
	 * @return
	 */
	public static String anyStringAnnotationsUpdate(String anyString) {
		
		// TODO : matches the annotations to change
		
		return anyString;
	}
}
