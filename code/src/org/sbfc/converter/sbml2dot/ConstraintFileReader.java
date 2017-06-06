/*
 * $Id: ConstraintFileReader.java 594 2015-10-14 15:43:09Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2dot/ConstraintFileReader.java $
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
package org.sbfc.converter.sbml2dot;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.identifiers.registry.RegistryLocalProvider;

/**
 * Reader class for the SBML2BioPAX configuration file.
 * 
 * @author ahenry
 * @author rodrigue
 * @author jalowcki
 */
public class ConstraintFileReader {

	public static final String MIRIAM_WS_URL = "http://www.ebi.ac.uk/compneur-srv/miriamws-main/MiriamWebServices";

	// creation of the link to the web services
	public static RegistryLocalProvider miriamWS;
	public static HashMap<String, String> officialURIs = new HashMap<String, String>(); 
	public static HashMap<String, String> checkedURIs = new HashMap<String, String>();

	private static Logger logger = Logger.getLogger(ConstraintFileReader.class);

	static {
		miriamWS = new RegistryLocalProvider();
		// miriamWS.setAddress(MIRIAM_WS_URL);
	}

	public static void parseSBML2SBGNConfigFile (Hashtable<String, String> annotationSpecies) {
		// call the other functions, adding the necessary empty hash map : parseSBML2SBGNConfigFile(annotationSpecies, null, null, null, "SBGN");
	}
	/**
	 * Reads the constraint file.
	 * 
	 * Uses the MIRIAM Web Services to obtain all the occurrences of an URI (old, deprecated and official).
	 * 
	 * It allows the SBML file to contain oldest information and still be exported.
	 * 
	 * @param constraintMap 
	 */
	public static void parseSBML2SBGNConfigFile (Hashtable<String, String> annotationSpecies, Hashtable<String, String> publicationModel, 
			Hashtable<String, String> speciesAnnotationMAP, Hashtable<String, String> taxonomyMap, String attributeClass)
	{
		String configFile = "SBML2SBGNConstraintFile.xml";
		
		try{
			// creation of a document factory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			// creation of a document constructor
			DocumentBuilder constructor = factory.newDocumentBuilder();

			// read of the XML content with DOM
			BufferedInputStream xml = new BufferedInputStream(SBGNUtils.class.getResourceAsStream(configFile));
			
			//parsing of the xml file
			Document document = constructor.parse(xml);

			//read xml
			Element root = document.getDocumentElement();
			Node annotationParameterNode = getChild(root, "constraints");

			//publicationModel
			Node publicationModelNode = getChild(annotationParameterNode, "publicationModel");
			Vector<Node> publicationList = getChildList(publicationModelNode, "publication");
			
			for (Node node : publicationList) {
				String publication = node.getTextContent();

				// list of annotation about the publications, {key: URI_database_Publication; value:official_Name}
				annotationMIRIAMName(miriamWS, publication, publicationModel, miriamWS.getName(publication));
			}
			
			//speciesAnnotationMap
			Node speciesAnnotationMapNode = getChild(annotationParameterNode, "speciesAnnotationMap");
			Vector<Node> speciesList = getChildList(speciesAnnotationMapNode, "species");

			for (int i = 0; i < speciesList.size(); i++) {
				String species = ((Node)speciesList.get(i)).getTextContent();

				// list of species, {key: URI_database_Species; value:official_Name}
				annotationMIRIAMName(miriamWS, species, speciesAnnotationMAP, miriamWS.getName(species));
			}
			
			//taxonomyMap
			Node taxonomyMapNode = getChild(annotationParameterNode, "taxonomyMap");
			Vector<Node> taxonomyList = getChildList(taxonomyMapNode, "taxonomy");
			
			for (int i = 0; i < taxonomyList.size(); i++) {
				String taxonomy = ((Node) taxonomyList.get(i)).getTextContent();
				
				// list of taxonomy annotation, {key: URI_database_Annotation; value:official_Name}
				annotationMIRIAMName(miriamWS, taxonomy, taxonomyMap, miriamWS.getName(taxonomy));
			}
		
			// annotationSpecies
			Node annotationSpeciesNode = getChild(annotationParameterNode, "annotationSpecies");
			Vector<Node> physicalEntityList = getChildList(annotationSpeciesNode, "physicalEntity");
			
			for (Node physicalEntityNode : physicalEntityList) {

				// Possible options for attributeClass are "classSBGN" "classBioPAX"
				String classOfEntity = ((Element) physicalEntityNode).getAttribute(attributeClass);
				
				logger.debug("\nConstraintFileReader : parse : " + attributeClass + " = " + classOfEntity);
				
				Vector<Node> physicalEntitySpecies = getChildList(physicalEntityNode, "species");

				for (Node speciesNode : physicalEntitySpecies) {
					String species = speciesNode.getTextContent();
					annotationMIRIAM(miriamWS, species, annotationSpecies, classOfEntity);// change here classBioPAX to classSBGN
				}
				
				logger.debug("ConstraintFileReader : parse config file : official constraints :");

				Vector<Node> physicalEntityConstraintList = getChildList(physicalEntityNode, "constraint");
				
				for (Node physicalEntityConstraintNode : physicalEntityConstraintList) {
					String constraint = physicalEntityConstraintNode.getTextContent();
					
					logger.debug(" file constraint URI = " + constraint);
					
					// Could be done by hand to speed up the application
					// TODO : Function that is doing that already there ???!!!
					String officialConstraintURI = miriamWS.getMiriamURI(constraint);
					
					logger.debug(" official constraint URI : " + officialConstraintURI + " = " + classOfEntity);
					
					if (officialConstraintURI == null) { // TODO : find a work-around for the ensembl constraint
						officialConstraintURI = constraint;
					}
					
					annotationSpecies.put(officialConstraintURI, classOfEntity);
				}
			}

		}catch(ParserConfigurationException pce){
			System.err.println("DOM parser configuration error, while the call factory.newDocumentBuilder();");
		}catch(SAXException se){
			System.err.println("Error during the document parsing, while the call construtor.parse(xml)");
		}catch(IOException ioe){
			ioe.printStackTrace();
			System.err.println("Input/Output Error, while the call construtor.parse(xml)\nCheck that the project contain the SBML2BioPAXconfigFile.xml");;
		}

//		//print the content of the MAP
//		logger.debug("annotationSpecies:");
//		for (Enumeration e = annotationSpecies.keys() ; e.hasMoreElements() ;) {
//		String s = (String) e.nextElement();
//		logger.debug("key: "+s+"   value: "+annotationSpecies.get(s));
//		}
	}


	/**
	 * <b>Browse a DOM document</b><br/>
	 * Get the first node's which name is the pattern
	 * @param node Node
	 * @param  pattern Pattern
	 * @return Node
	 */
	public static Node getChild(Node node, String pattern){
		NodeList nodeList = node.getChildNodes();
		Node child = null;
		int i=0;
		while (i < nodeList.getLength() && !(nodeList.item(i).getNodeName().equals(pattern))){
			i++;
		}
		if (i<nodeList.getLength()){
			child = nodeList.item(i);
		}
		return child;
	}

	/**
	 * <b>Browse a DOM document</b><br/>
	 * Get a Node list of the node's child which name are the pattern
	 * 
	 * @param node
	 * @param pattern
	 * @return Vector list of child node which contained the pattern
	 */
	public static Vector<Node> getChildList(Node node, String pattern){
		NodeList nodeList = node.getChildNodes();
		
		Vector<Node> childlist = new Vector<Node>();
		
		for (int i=0; i < nodeList.getLength(); i++){
			if (nodeList.item(i).getNodeName().equals(pattern)){
				childlist.add(nodeList.item(i));
			}
		}
		return childlist;
	}

	/**
	 * Method to <b>fill the Maps</b> used in the program using MIRIAM WS.<br/>
	 * Provide it the Miriam link object create in the constructor.<br/>
	 * Put the URI official and the SBGN correspondance.<br/>
	 * For the molecule mapping, this method will be correspond all the URI of a database with the correspondance in SBGN format<br/>
	 * exemple:
	 * <code><pre>
	 * annotationMIRIAM(linkMiriam, "http://www.bind.ca/", annotationSpecies, "simple chemical");
	 * 
	 * fill the molecule mapping Map with:
	 * 
	 * annotationSpecies{key: "http://www.bind.ca/", "simple chemical"}
	 * annotationSpecies{key: all the URLs for BIND, "simple chemical"}
	 * 
	 * </code></pre>
	 * @param linkMiriam Object MIRIAM to link the Web service
	 * @param URI URI, address official of the database
	 * @param table Map to fill
	 * @param type value in the MAP
	 */
	public static void annotationMIRIAM (RegistryLocalProvider linkMiriam, String URI, Hashtable<String, String> table, String type) {

		// logger.debug("annotationMIRIAM :  URI = " + URI + ", type = " + type);

		if (checkedURIs.get(URI) != null) {
			return;
		} else {
			checkedURIs.put(URI, "done");
		}
		
		String name = linkMiriam.getName(URI);
		
		String officialURN = linkMiriam.getOfficialDataTypeURI(URI);
		officialURIs.put(URI, officialURN);

		if (name.equals("")){
			logger.debug(URI+" URI is not contain in MIRIAM database, no answer possible");
		} else {
			String[] URIs = linkMiriam.getDataTypeURIs(name);
			for (int i = 0; i < URIs.length; i++) {
				table.put(URIs[i], type);
				logger.debug("key: ." + URIs[i] + ".   value: " + type);
			}
		}
		
	}
	
	public static void annotationMIRIAMName (RegistryLocalProvider linkMiriam, String URI, Hashtable<String, String> table, String miriamName) {

		// logger.debug("annotationMIRIAM :  URI = " + URI + ", type = " + type);

		String name = linkMiriam.getName(URI);
		
		String officialURN = linkMiriam.getOfficialDataTypeURI(URI);
		officialURIs.put(URI, officialURN);

		if (name.equals("")){
			logger.debug(URI+" URI is not contain in MIRIAM database, no answer possible");
		} else {
			String[] URIs = linkMiriam.getDataTypeURIs(name);
			for (int i = 0; i < URIs.length; i++) {
				table.put(URIs[i], miriamName);
				// logger.debug("key: ." + URIs[i] + ".   value: " + type);
			}
		}
		
	}

}
