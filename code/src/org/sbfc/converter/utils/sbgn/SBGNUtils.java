/*
 * $Id: SBGNUtils.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/utils/sbgn/SBGNUtils.java $
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
package org.sbfc.converter.utils.sbgn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sbfc.converter.sbml2dot.ConstraintFileReader;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;


/**
 * Enable to get a shape in a given format of a {@link SBase} SBML element. The constructor has to be defined with the name of 
 * the converter and then the function getShape can be called.
 * To add others converters/formats, add the option in FORMAT and a mapping function.
 * It is also possible to add new classes in the constraint file.
 * 
 * This class :
 * - gets a SBGN class through MIRIAM annotations or SBOterm and the sbo2sbgn.properties file.
 * - maps the SBGN-ML classes into another language or format using a property file (there is one per language or format). 
 * 
 * Available formats : dot, sbgnml
 * 
 * @author jalowcki
 *
 */
public class SBGNUtils {

	// Set a logger for debug message
	private static Logger logger = Logger.getLogger(SBGNUtils.class);	
	
	private String[] FORMATS = new String[2];
	private String conversionFormatOutput;
	private static Properties properties = readProperties();

	// build the tree of SBOterms which are contained in the properties file
	private static ArrayList<String> tree = buildTree();

	// we call the SBGNUtils constructor as a converter from an SBML element to a shape in another graphical format.
	public SBGNUtils (String conversionFormatOutput) {
		
		FORMATS[0] = "dot";
		FORMATS[1] = "sbgnml";
		this.conversionFormatOutput = conversionFormatOutput;
		
	}
	
	// Reading the constraint file to extract a mapping between MIRIAM uri and SBGN classes
	public static Hashtable<String, String> speciesAnnotationMap = new Hashtable<String, String>(); 
	
	static {
		// We are not using the 3 others hash tables.
		ConstraintFileReader.parseSBML2SBGNConfigFile(speciesAnnotationMap, new Hashtable<String, String>(), 
				new Hashtable<String, String>(), new Hashtable<String, String>(), "classSBGN");
	}
	
	/**
	 * Get the SBGN class of a glyph by reading the annotations. Return null if no SBGN class has been found 
	 * or if the given {@link Annotation} was null.
	 * @param {@link Annotation}
	 * @return sbgnClass
	 */
	public static String getSBGNClassThroughAnnotation(Annotation annotation) {
		
		String sbgnClass = null;
		
		if (annotation == null) {
			logger.warn("ShapeUtils : getSBGNClassThroughAnnotation : annotation string is null !");
			return sbgnClass;
		}
		
		if (annotation.getNumCVTerms() > 0) {
			
			// loop over the list of CVTerm (bqbiol:xxx or bqmodel:xxx RDF construct)
			for (CVTerm cvTerm : annotation.getListOfCVTerms()) {
				
				// Test is the qualifier is bqbiol:hasPart
				if (cvTerm.isBiologicalQualifier() && cvTerm.getBiologicalQualifierType().equals(CVTerm.Qualifier.BQB_HAS_PART)) {
					return "complex";
				}
				
				Integer i = 0;
				String biopaxPhysicalEntityType = null;
				String previousSBGNClass = null;
				boolean canCompare = false;
				
				// Loop over all the uris of the given cvTerm
				for (String uri : cvTerm.getResources()) {
					i += 1;
					
					logger.debug("Number of uri : " + i.toString() + " " + uri);// eg urn:miriam:interpro:IPR002394
					
					// ignore annotation beginning with # (added because of the inchi annotations)
					if (uri.matches("^#.*")) {
						continue;
					}
					
					if (speciesAnnotationMap.containsKey(uri)) {
						
						logger.debug("entire uri recognized");
						
						// TODO official annotation and old ones: how to update and recognize them all?
						
						// recovering biopax type
						biopaxPhysicalEntityType = speciesAnnotationMap.get(uri);
						
					} else {
						// the complete annotation is not being recognized, let's truncate it
						
						// parsing the annotation
						int lastIndex = uri.lastIndexOf(":");
						String firstPartUri = uri.substring(0, lastIndex);
						logger.debug("firstPartUri : " + firstPartUri);
						
						// checking in the constraint file
						if (speciesAnnotationMap.containsKey(firstPartUri)){
							biopaxPhysicalEntityType = speciesAnnotationMap.get(firstPartUri);
							logger.debug("parsed annotation recognized is "+biopaxPhysicalEntityType);
						}
					}
					
					if (biopaxPhysicalEntityType == null || biopaxPhysicalEntityType.trim().length() == 0) {
						logger.warn("Warning : cannot find a valid class for the uri '" + uri + "'.");
						continue;
					}
					
					// Detecting an anomaly in the annotations by comparing their shapes.
					sbgnClass = biopaxTosbgnClass(biopaxPhysicalEntityType);

					// Check sbgnClass
					
					if (canCompare && (sbgnClass != previousSBGNClass)) {
						logger.warn("Warning: annotations give different SBGN-ML class");
					}
					previousSBGNClass = sbgnClass;
					canCompare = true;
				}
				
				// mapping the biopax and sbgn classes
				sbgnClass = biopaxTosbgnClass(biopaxPhysicalEntityType);
			}
		}
		
		return sbgnClass;
	}
	
	/**
	 * For a given {@link SBase} element, return the SBGN-ML class of the element or of this nearest parent. The SBGN-ML class returned 
	 * will have a meaning in terms of SBGN graphical representation.
	 * SBGN-ML classes relevant for SBGN representation are stored in the properties file.
	 * @param sBase, properties
	 * @return sboClass
	 */
	public static String getSBGNClassThroughSboterm(SBase sBase){

		if (sBase.isSetSBOTerm()){

			int sboTermInt = sBase.getSBOTerm();
			String sboTerm = sBase.getSBOTermID();
			
			if (!SBO.checkTerm(sboTerm)){
				logger.warn("Warning : Bad sboTerm : " + sboTerm);// maybe useless because checked in intToString
			}
			else {
				
				String sboClass = properties.getProperty(sboTerm);
				
				// chercher avec tree si on le trouve ou si un des SBO term du tree l' a pour enfant
				if (sboClass == null) {
					
					// looping over the SBOTerms tree, looking for a putative parent

					// TODO : explain why it can work fine
					for (String parent : tree) {
						int parentSboTerm = SBO.stringToInt(parent);
						// if a parent is found, it will be then returned
						if (SBO.isChildOf(sboTermInt, parentSboTerm)){
							return properties.getProperty(parent);
						}
					}
				} else {
					
					return sboClass;
				}
			}
		}
		// sboTerm cannot be represented by any shapes : annotations will be used.
		return null;
	}
	
	
	/**
	 * Return the corresponding sbo {@link String} from a given SBO class. Checked classes are only relevant for SBGN-ML.
	 * If not found, null is returned.
	 * @return
	 */
	public static Integer getSBOTermThroughSBGNClass(String SBGNClass) {
		
		Iterator<Object> itKey = properties.values().iterator();
		
		// loop over the values
		while (itKey.hasNext()) {
			
			String key = (String) itKey.next();
			String value = properties.getProperty(key);
			
			if (value.equals(SBGNClass)) {
				
				// return the associated SBO term as an Integer 
				return SBO.stringToInt(key);
			}
		}		

		// return null otherwise
		return null;
	}
	
	
	/**
	 * This function reads "sbo2sbgn.properties" file and return the associated {@link Properties} object.
	 * The file has to be located near the SBGNUtils file class.Return {@link IOException} otherwise
	 * @return {@link Properties}
	 */
	public static Properties readProperties() {

		// Building the dictionary of sboTerms to SBGN classes
		Properties properties = new Properties();
		try {
		    properties.load(SBGNUtils.class.getResourceAsStream("sbo2sbgn.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return properties;
		
	}
	
	/**
	 * Return the nearest SBGN class for the given {@link SBase} element. Check the sboTerm first to get this class, annotations otherwise.
	 * Return null if no class available.
	 * @param {@link SBase}
	 * @return SBGNclass or null
	 */
	public static String getSBGNClass(SBase sBase){
		
		String sbgnClassBySboTerm = getSBGNClassThroughSboterm(sBase);
		logger.debug("for the reaction "+sBase.getSBOTermID()+" sbgnClassBySboTerm is = "+sbgnClassBySboTerm);
		
		String sbgnClassByAnnotation = getSBGNClassThroughAnnotation(sBase.getAnnotation());
		logger.debug("SBGN class by the annotation is = "+sbgnClassByAnnotation);
		
		if (sbgnClassBySboTerm != null) 
		{
			// warn the user that sboTerm and annotations give different SBGN classes
			if (sbgnClassByAnnotation != null && !sbgnClassBySboTerm.equals(sbgnClassByAnnotation)) 
			{
				logger.warn("Warning: for MetaID: '" + sBase.getMetaId() + "', the shape found with sboTerm is '" + 
						sbgnClassBySboTerm + "' instead of '" + sbgnClassByAnnotation + "' with annotations");
			}
			
			return sbgnClassBySboTerm;
		}
		
		if (sbgnClassByAnnotation != null) 
		{
			return sbgnClassByAnnotation;
		}

		return null;
	}
	
	/**
	 * Return for a bioPax type the associated SBGN class.
	 * @param biopaxType
	 * @return sbgnClass
	 */
	public static String biopaxTosbgnClass(String biopaxType){
		/* BioPax - SBGN
		 * complex - complex
		 * protein - macromolecule
		 * rna - macromolecule
		 * dna - macromolecule
		 * smallMolecule - simple chemical
		 */
		HashMap<String, String> link = new HashMap<String, String>(5);
		link.put("complex", "complex");
		link.put("protein", "macromolecule");
		link.put("rna", "macromolecule");
		link.put("dna", "macromolecule");
		link.put("smallMolecule", "simple chemical");
		
		String sbgnClass = link.get(biopaxType);
				
		return sbgnClass;
	}


	/**
	 * Read the .properties file and build a {@link ArrayList} where all the SBO ontology is sorted as a tree.
	 * The {@link ArrayList} returned is an easy way to browse all SBOterms by keeping their order as in the properties file.
	 * @return tree or null if the file does not exist.
	 */
	private static ArrayList<String> buildTree() {
		InputStream is = SBGNUtils.class.getResourceAsStream("sbo2sbgn.properties");

		if (is == null) {
			System.out.println("The file 'sbo2sbgn.properties' was not found !!!");
			return new ArrayList<String>();
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isr);
		String line = "";

		ArrayList<String> tree = new ArrayList<String>(0);
		try {
			while ((line = in.readLine()) != null) {

				String[] sboTab = line.split("=", 1);

				// escaping dash, comments, blank lines...
				if (!line.startsWith("#") && !line.equals("") && sboTab.length == 2) {
					// the : character is read by the properties file like a split character
					String correctSboTerm = sboTab[0].replace("\\:", ":");
					if (SBO.checkTerm(correctSboTerm)) {
						tree.add(correctSboTerm);
					}
				}
			}
		} catch (IOException e) {
			// file read from the jar file, error should never happen
			e.printStackTrace();
		}
		return tree;
	}
	
	
	/**
	 * This method returns a shape regarding the selected format. This result is based on a SBGN shape retrieval.
	 * However null will be returned if the converter is wrongly define.
	 * Null can be put as defaultSBGNClass but therefore null will be returned if the process fails, defaultShape is a security.
	 * @param {@link SBase}
	 * @param defaultShape
	 * @return shape
	 */
	public String getOutputFromClass(SBase sBase, String defaultSBGNClass) {

		String sbgnClass = getSBGNClass(sBase);
		
		// a lack of annotation in the file and the sbgnClass can be null
		if (sbgnClass == null) {
			sbgnClass = defaultSBGNClass;

			// we cannot give a shape, we return null and a warning
			if (defaultSBGNClass == null) {
				logger.info("Shape for metaId: " + sBase.getMetaId() + " cannot be given.");
				return null;
			}
			
		}
		
		
		// logic arcs are not taken into consideration.
		// Controls process nodes classes
		
		if (sBase.getClass().toString().equals("class org.sbml.jsbml.Reaction")) {
			
			// ARCS of the type: from/to SpeciesReference to/from SpecieReference
			String regex = "^consumption$|^production$";
			if (defaultSBGNClass.matches(regex)) {
				
				// here we have an arc, if the reaction class is discordant, the default class will be given instead
				if (!sbgnClass.matches(regex)) {
					sbgnClass = defaultSBGNClass;
				}
			}
			
			// ARCS of the type: from ModifierSpecieReference to a process node
			regex = "^modulation$|(^necessary |^)stimulation$|^catalysis$|^inhibition$";
			if (defaultSBGNClass.matches(regex)) {
				
				if (!sbgnClass.matches(regex)) {
					sbgnClass = defaultSBGNClass;
				}
			}
			
			// PROCESS NODES classes
			regex = "(^uncertain |^omitted |^)process$|^association$|^dissociation$|^phenotype$";
			if (defaultSBGNClass.matches(regex)) {
				
				if (!sbgnClass.matches(regex)) {
					sbgnClass = defaultSBGNClass;
				}
			}
			// Another possibility would be to check the classes SpeciesRef and ModifierSpeciesRef
		}
			
		
		// When we will have more formats we will have to improve this loop
		if (conversionFormatOutput.equalsIgnoreCase("dot")) {
			// return the shape in dot
			HashMap<String, String> map = sbgn2DotMap();
			return map.get(sbgnClass);
			
		} else if (conversionFormatOutput.equalsIgnoreCase("sbgnml")) {
			// return the shape in SBGN-ML, we return the SBGN-ML class
			return sbgnClass;
		}
		
		// Returning null. This enable the null statement to be handled otherwise we can throw an Exception and sys.exit(1)
		System.out.println("Warning : format " + conversionFormatOutput + " does not exist.");
		return null;
	}
	
	/**
	 * Return a {@link HashMap} which is mapping sbgn-ml classes and dot shapes.
	 * @param hm
	 */
	private static HashMap<String, String> sbgn2DotMap() {

		String imagePath = "src/org/sbfc/converter/sbml2dot/dotImages/";// TODO web link to images

		HashMap<String, String> hm = new HashMap<String, String>();
		
		// TODO multimers customs postscript shapes, link: http://www.graphviz.org/Documentation/html/shapehowto.html
		// it is possible to get more than one stroke for multimers with peripheries=2 but the result in not SBGN compliant
		hm.put("macromolecule multimer", "\"rectangle\", style=rounded");
		hm.put("complex multimer", "\"octagon\"");
		// image can not be retrieve in png or svg file, a hyperlink could be a solution. For the moment they are removed.
		hm.put("nucleic acid feature multimer", "\"ellipse\"");
//		hm.put("nucleic acid feature multimer", "none, image=\""+imagePath+"multimerNAF.png\"];\n//");
		hm.put("simple chemical multimer", "\"circle\"");

		// TODO decorations have to be made, the svg can be modified for that purpose
		hm.put("complex", "\"octagon\"");
		// TODO shape has to be made, the svg can be modified for that purpose
		hm.put("nucleic acid feature", "\"ellipse\"");
//		hm.put("nucleic acid feature", "none, image=\""+imagePath+"geneticEntity.png\"];\n//");
		hm.put("empty set", "plaintext, label=\"Ø\", fontsize=15,fontcolor=blue];");
		hm.put("simple chemical", "\"circle\"");
		hm.put("compartment", "\"rectangle\", style=rounded");
		hm.put("unspecified entity", "\"ellipse\"");
		hm.put("macromolecule", "\"rectangle\", style=rounded");
		// TODO image for the shape, maybe encoded directly in svg?
		hm.put("perturbation", "\"ellipse\"");
//		hm.put("perturbation", "none, image=\""+imagePath+"perturbation.png\"];\n//");

		hm.put("association", "\"circle\", style=filled, fillcolor=black");
		hm.put("dissociation", "\"doublecircle\", fixedsize=true, width=0.001, height=0.001]//");
		
		hm.put("submap", "\"rectangle\"");// TODO tags inside
		hm.put("omitted process", "\"square\", label=\"\\\\\\\\\" ");
		hm.put("uncertain process", "\"square\", label=\"?\"");
		hm.put("phenotype", "\"hexagon\"");
		hm.put("process", "\"square\"");

		hm.put("and", "\"circle\", label=\"AND\"");
		hm.put("or", "\"circle\", label=\"OR\"");
		hm.put("not", "\"circle\", label=\"NOT\"");
		
		// TODO for production and consumption arcs: add arcs label cardinality
		hm.put("consumption", "\"none\"");
		hm.put("production", "\"normal\"");
		hm.put("necessary stimulation", "\"teeonormal\"");
		hm.put("catalysis", "\"odot\"");
		hm.put("stimulation", "\"onormal\"");
		hm.put("inhibition", "\"tee\"");
		hm.put("modulation", "\"odiamond\"");
		hm.put("logic arc", "\"none\"");
		
		return hm;
	}
}
