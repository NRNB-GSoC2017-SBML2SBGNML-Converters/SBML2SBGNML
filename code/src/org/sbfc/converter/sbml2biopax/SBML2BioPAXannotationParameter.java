/*
 * $Id: SBML2BioPAXannotationParameter.java 594 2015-10-14 15:43:09Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2biopax/SBML2BioPAXannotationParameter.java $
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.identifiers.registry.RegistryLocalProvider;
import org.identifiers.registry.RegistryUtilities;

/**
 * Reader class for the SBML2BioPAX configuration file.
 * 
 * @author ahenry
 * @author rodrigue
 */

// TODO : Fix/Patch the code to be able to use only one SBML2BioPAXannotationParameter class instead of two.

public class SBML2BioPAXannotationParameter {

	/**
	 * <b>Communication class</b> of the SBML2BioPAX program.<br/>
	 * Communicate with the configuration file and read it using the DOM API.<br/>
	 * The configuration file MUST be in the same package that the classes.<br/>
	 * Read the file and communicate with the MIRIAM Web Services to obtain all the occurances of an URI (old, deprecated or official).
	 * By this way, the SBML file can contain oldest information and still be exported.
	 * Currently, four Maps are filled to permit to the program to distinguish the subclasses of references in BioPAX: publicationXref, unificationXref, bioSource<br/>
	 * <ul>
	 * <li>taxonomy: <code><pre>
	 * &lt;taxonomyMap&gt;
	 *  &lt;taxonomy>http://www.taxonomy.org/&lt;/taxonomy&gt;
	 * &lt;/taxonomyMap&gt;
	 * </code></pre></li>
	 * 
	 * <li>publication: <code><pre>
	 * &lt;publicationModel&gt;
	 *  &lt;publication&gt;http://www.doi.org/&lt;/publication&gt;
	 *  &lt;publication&gt;http://www.pubmed.gov/&lt;/publication&gt;
	 * &lt;/publicationModel&gt;
	 * </code></pre></li>
	 * 
	 * <li>other external references: <code><pre>
	 * &lt;speciesAnnotationMap&gt;
	 *   &lt;species>http://www.bind.ca/&lt;/species&gt;
	 *   ...
	 * &lt;/speciesAnnotationMap&gt;
	 * </code></pre></li>
	 * 
	 * <li>molecules mapping SBML/BioPAX: <code><pre>
	 * &lt;annotationSpecies&gt;
	 *  &lt;physicalEntity classBioPAX="complex"&gt;
	 *    &lt;species>http://www.bind.ca/&lt;/species&gt;
	 *    ...
	 *  &lt;/physicalEntity&gt;
	 * &lt;/annotationSpecies&gt;
	 * </code></pre></li>
	 * </ul>
	 * @param annotationSpecies
	 * @param publicationModel
	 * @param speciesAnnotationMAP
	 * @param taxonomyMap 
	 */
	public static void parseSBML2BioPAXConfigFile (Hashtable<String, String> annotationSpecies, Hashtable<String, String> publicationModel, 
			Hashtable<String, String> speciesAnnotationMAP, Hashtable<String, String> taxonomyMap)
	{
		String configFile = "SBML2BioPAXconfigFile_withConstraints.xml";
		RegistryLocalProvider linkMiriam =  SBML2BioPAX.link;

		try{
			// creation of a document factory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			// creation of a document constructor
			DocumentBuilder constructor = factory.newDocumentBuilder();

			// read of the XML content with DOM
			BufferedInputStream xml = new BufferedInputStream(SBML2BioPAXannotationParameter.class.getResourceAsStream(configFile));
			
			//parsing of the xml file
			Document document = constructor.parse(xml);

			//read xml
			Element root = document.getDocumentElement();
			Node annotationParameterNode = getChild(root, "sbml2biopaxAnnotationParameter");

			//publicationModel
			Node publicationModelNode = getChild(annotationParameterNode, "publicationModel");
			Vector<Node> publicationList = getChildList(publicationModelNode, "publication");
			
			for (Node node : publicationList) {
				String publication = node.getTextContent();

				// list of annotation about the publications, {key: URI_database_Publication; value:official_Name}
				annotationMIRIAM(linkMiriam, publication, publicationModel, linkMiriam.getName(publication));
			}
			
			//speciesAnnotationMap
			Node speciesAnnotationMapNode = getChild(annotationParameterNode, "speciesAnnotationMap");
			Vector<Node> speciesList = getChildList(speciesAnnotationMapNode, "species");

			for (int i = 0; i < speciesList.size(); i++) {
				String species = ((Node)speciesList.get(i)).getTextContent();

				// list of species, {key: URI_database_Species; value:official_Name}
				annotationMIRIAM(linkMiriam, species, speciesAnnotationMAP, linkMiriam.getName(species));
			}
			
			//taxonomyMap
			Node taxonomyMapNode = getChild(annotationParameterNode, "taxonomyMap");
			Vector<Node> taxonomyList = getChildList(taxonomyMapNode, "taxonomy");
			
			for (int i = 0; i < taxonomyList.size(); i++) {
				String taxonomy = ((Node) taxonomyList.get(i)).getTextContent();
				
				// list of taxonomy annotation, {key: URI_database_Annotation; value:official_Name}
				annotationMIRIAM(linkMiriam, taxonomy, taxonomyMap, linkMiriam.getName(taxonomy));
			}
		
			// annotationSpecies
			Node annotationSpeciesNode = getChild(annotationParameterNode, "annotationSpecies");
			Vector<Node> physicalEntityList = getChildList(annotationSpeciesNode, "physicalEntity");
			
			for (Node physicalEntityNode : physicalEntityList) {

				String classBioPAX = physicalEntityNode.getAttributes().item(0).getNodeValue();
				Vector<Node> physicalEntitySpecies = getChildList(physicalEntityNode, "species");

				for (Node speciesNode : physicalEntitySpecies) {
					String species = speciesNode.getTextContent();
					annotationMIRIAM(linkMiriam, species, annotationSpecies, classBioPAX);
				}
				
				// System.out.println("SBML2BioPAXannotationParameter : parseSBML2BioPAXConfigFile : official constraints = ");

				Vector<Node> physicalEntityConstraintList = getChildList(physicalEntityNode, "constraint");
				
				for (Node physicalEntityConstraintNode : physicalEntityConstraintList) {
					String constraint = physicalEntityConstraintNode.getTextContent();
					
					// Could be done by hand to speed up the application
					String officialConstraint = linkMiriam.getMiriamURI(constraint);
					
					// logger.debug("constraint = " + constraint + ", miriam uri = " + officialConstraint);
					
					if (officialConstraint != null) 
					{
						annotationSpecies.put(officialConstraint, classBioPAX);
					}
					else 
					{
						officialConstraint = RegistryUtilities.convertURN(constraint);
						annotationSpecies.put(officialConstraint, classBioPAX);
						// System.out.println("constraint = " + constraint + ", miriam uri (BY HAND) = " + officialConstraint);
						
					}
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
//		System.out.println("annotationSpecies:");
//		for (Enumeration e = annotationSpecies.keys() ; e.hasMoreElements() ;) {
//		String s = (String) e.nextElement();
//		System.out.println("key: "+s+"   value: "+annotationSpecies.get(s));
//		}
	}

	/*=== pathway ===
	 *  taxonomy
	 * http://www.taxonomy.org/
	 *  biosource/tissue
	 * http://www.who.int/classifications/icd/
	 *  publication
	 * http://www.doi.org/
	 * http://www.pubmed.gov/
	 * http://www.ncbi.nlm.nih.gov/OMIM/
	 *  reference
	 * http://www.genome.jp/kegg/pathway/
	 * http://www.reactome.org/
	 * http://www.geneontology.org/
	 */
	/*=== reaction ===
	 * http://www.ebi.ac.uk/intact
	 * http://www.genome.jp/kegg/reaction/
	 * http://www.reactome.org/
	 * http://www.geneontology.org/
	 */

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
	 * Put the URI official and the BioPAX correspondance.<br/>
	 * For the molecule mapping, this method will be correspond all the URI of a database with the correspondance in BioPAX format<br/>
	 * exemple:
	 * <code><pre>
	 * annotationMIRIAM(linkMiriam, "http://www.bind.ca/", annotationSpecies, "smallMolecule");
	 * 
	 * fill the molecule mapping Map with:
	 * 
	 * annotationSpecies{key: "http://www.bind.ca/", "smallMolecule"}
	 * annotationSpecies{key: all the URLs for BIND, "smallMolecule"}
	 * 
	 * </code></pre>
	 * @param linkMiriam Object MIRIAM to link the Web service
	 * @param URI URI, address official of the database
	 * @param table Map to fill
	 * @param type value in the MAP
	 */
	public static void annotationMIRIAM (RegistryLocalProvider linkMiriam, String URI, Hashtable<String, String> table, String type) {

		// System.out.println("annotationMIRIAM :  URI = " + URI);

		String name = linkMiriam.getName(URI);
		
		String officialURI = linkMiriam.getOfficialDataTypeURI(URI);
		SBML2BioPAX.officialURIs.put(URI, officialURI);
		SBML2BioPAX.officialNames.put(URI, name);		
		SBML2BioPAX.officialNames.put(officialURI, name);
		
		if (name.equals("")){
			System.out.println(URI+" URI is not contain in MIRIAM database, no answer possible");
		}
		else {
			String[] URIs = linkMiriam.getDataTypeURIs(name);
			for (int i = 0; i < URIs.length; i++) {
				table.put(URIs[i], type);
				// System.out.println("key: "+URIs[i]+"   value: "+ type);
			}
		}
		
	}
}
