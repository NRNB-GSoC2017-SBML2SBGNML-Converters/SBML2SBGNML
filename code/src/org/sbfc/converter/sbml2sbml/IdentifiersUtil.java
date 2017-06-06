/*
 * $Id: IdentifiersUtil.java 594 2015-10-14 15:43:09Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2sbml/IdentifiersUtil.java $
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.sbml.jsbml.xml.stax.SBMLWriter;

import org.identifiers.registry.RegistryLocalProvider;
import org.identifiers.registry.RegistryUtilities;

/**
 * This class will provide utility methods to transform all the annotations in am SBML model to use either only identifiers.org URL
 * or miriam URN.
 * 
 * @author rodrigue
 *
 */
public class IdentifiersUtil {

	private static final String EC_CODE_NAME = "Enzyme Nomenclature";
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
	
	@SuppressWarnings("unchecked")
	static private void sBaseAnnotationsUpdate(SBase sbase, String annotationPrefix) {

		if (sbase.getCVTermCount() > 0) {
			
			boolean urn2url = false;
			boolean url2urn = false;
			boolean update = false;
			
			if (annotationPrefix == null) {
				update = true;
			} else if (annotationPrefix.equals(urlPrefix)) {
				urn2url = true;
			} else if (annotationPrefix.equals(urnPrefix)) {
				url2urn = true;
			}
			
			for (CVTerm cvTerm : sbase.getCVTerms()) {
				
				List<String> uris = cvTerm.getResources();
				List<String> updatedUris = new ArrayList<String>();
				boolean updated = false;
				
				for (String annotationString : uris) {
					String newURI = annotationString;

					// Tests and correction on the uri before sending it to the converter
					String databaseId = RegistryUtilities.getElementPart(annotationString);
					String databasePart = RegistryUtilities.getDataPart(annotationString);
					String databaseName = link.getName(databasePart);
					boolean deleteAnnotation = false;

					// System.out.println(annotationPrefix + " @" + annotationString + "@ ==> @" + databaseId + "@ - @" + databaseName + "@");

					// if the identifier is not found and database is ec-code, we delete the annotation
					// we don't delete all annotations where we do not find the identifier to avoid to delete
					// valid annotations that do not conform to the miriam registry data
					if ((databaseId == null || databaseId.trim().length() == 0) && (databaseName != null && databaseName.equals(EC_CODE_NAME)))
					{
						updated = true;
						deleteAnnotation = true;
						System.out.println("Error : the uri '" + annotationString + "' does not contain any identifier !!");
					}
					
					// if the identifier is not empty and database is ec-code, we need to do some extra checks
					if (databaseName != null && databaseName.equals(EC_CODE_NAME) && !deleteAnnotation)
					{
						if (databaseId.matches("[0-9]+"))
						{
							System.out.print("Warning : adding '.-' to the ec-code uri '" + annotationString + "'.");
							annotationString = annotationString + ".-.-.-";
							System.out.println(" ==> '" + annotationString + "'.");							
						}
						else if (databaseId.matches("[0-9]+\\.[0-9]+"))
						{
							System.out.print("Warning : adding '.-' to the ec-code uri '" + annotationString + "'.");							
							annotationString = annotationString + ".-.-";
							System.out.println(" ==> '" + annotationString + "'.");							
						}
						else if (databaseId.matches("[0-9]+\\.[0-9]+\\.[0-9]+"))
						{
							System.out.print("Warning : adding '.-' to the ec-code uri '" + annotationString + "'.");							
							annotationString = annotationString + ".-";
							System.out.println(" ==> '" + annotationString + "'.");							
						}
						else if (databaseId.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9A-Z]+") && !databaseId.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+"))
						{
							System.out.print("Warning : adding '.-' to the ec-code uri '" + annotationString + "'.");							
							annotationString = annotationString.substring(0, annotationString.lastIndexOf(".")) + ".-";
							System.out.println(" ==> '" + annotationString + "'.");							
						}
					}
					
					if ((url2urn || urn2url && (!annotationString.startsWith(annotationPrefix))))
					{
						if (url2urn)
						{
							newURI = link.convertURL(annotationString);
						}
						else if (urn2url)
						{
							newURI = link.convertURN(annotationString);
						} 
					}
					else if (update || annotationString.startsWith(annotationPrefix))
					{
						newURI = link.getMiriamURI(annotationString);
					}

					// System.out.println(annotationPrefix + " @" + annotationString + "@ ==> @" + newURI + "@");
					
					if (newURI == null && !deleteAnnotation) {
						// keeping invalid annotations and reporting them in the log
						System.out.println("Error : the uri '" + annotationString + "' is not recognized by miriamws !!");
						newURI = annotationString;
					} else if (!deleteAnnotation && !newURI.equals(annotationString)) {
						updated = true;
					}
					if (!deleteAnnotation)
					{
						updatedUris.add(newURI);
					}
				}

				if (updated) {
					uris.clear();
					uris.addAll(updatedUris);
				}
			}
		}
		
		// Going through all children element, including L3 packages elements
		if (sbase.getChildCount() > 0) {

			for (Object treeNode : Collections.list(sbase.children())) {
				
				if (treeNode instanceof SBase) {
					sBaseAnnotationsUpdate((SBase) treeNode, annotationPrefix);					
				}
			}
		}
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

		return doc;
	}

	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println(
			  "Usage : java org.sbfc.converter.sbml2sbml.IdentifiersUtil [-m|-i|-u] sbmlFileName [suffix]");
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
			fileNameSuffix = args[2];
		}

		String jsbmlWriteFileName = fileName.replaceFirst(".xml", fileNameSuffix + ".xml");
		
		System.out.printf("Reading %s and writing %s\n", fileName, jsbmlWriteFileName);

		SBMLDocument testDocument;
		long afterRead = 0;
		long afterAnnoUpdate = 0;
		try {
			testDocument = new SBMLReader().readSBMLFile(fileName);
			System.out.printf("Reading done\n");
			System.out.println(Calendar.getInstance().getTime());
			afterRead = Calendar.getInstance().getTimeInMillis();
			
			documentAnnotationsUpdate(testDocument, annotationPrefix);
			afterAnnoUpdate = Calendar.getInstance().getTimeInMillis();
			
			System.out.printf("Starting writing\n");
			
			new SBMLWriter().write(testDocument, jsbmlWriteFileName);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
	public static String anyStringAnnotationsUpdate(String anyURI) 
	{		
		return link.getMiriamURI(anyURI);
	}
}
