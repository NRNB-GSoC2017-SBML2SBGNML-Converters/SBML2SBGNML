/*
 * $Id: SvgRenderer.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/svg/SvgRenderer.java $
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
package org.sbfc.converter.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



//import uk.ac.ebi.compneur.xml.ElementUtilities;

/**
 * Here we edit simple svg file (from the conversion from .dot) and render them
 * 
 * doc for svg: http://www.w3schools.com/svg/svg_examples.asp
 * 
 * @author jalowcki
 * 
 */
public class SvgRenderer {




	/**
	 * Read the attributes of a given {@link Node} and return a {@link HashMap}.
	 * Return null if the given node has no attributes.
	 * Gives a warning if it will return null for given {@link Node}.
	 * Example: TODO
	 * 
	 * @param tree
	 * @return {@link HashMap} or null
	 */
	private static HashMap<String, String> accessAttributesOfNodes(Node node) {

		System.out.println("in access function");
		
		if (!node.hasAttributes()) {
			System.out.println("warning node " + node.getLocalName() + " has no attributes.");
			return null;
		}
		
		Integer numberOfAttr = node.getAttributes().getLength();

		HashMap<String, String> attributesMap = new HashMap<String, String>(numberOfAttr);
		
		for (int i = 0; i < numberOfAttr; i++) {
			
			String attr = node.getAttributes().item(i).toString();

			System.out.println("attribut of node = " + attr);
			
			String[] couple = attr.split("=");
			attributesMap.put(couple[0], couple[1]);
			
		}
		return attributesMap;
	}

	
	
	
	/**
	 * Recursive function which depth-first search svg grouping {@link Node}s, render them and modify the dom file.
	 * @param {@link Node}
	 * @param {@link Document}
	 */
	private static void renderGroupingNodes(Node parentNode, Document dom) {

		System.out.println("in renderGroupingNodes");
		// fonction recursive qui doit faire un parcours en profondeur
		// a chaque GNodes rencontre il faut le render et modifier le dom file
		// ne prend que des GNodes
		
		HashMap<String, String> nodeAttributes = accessAttributesOfNodes(parentNode);
		
		// Retrieving node attributes
		for (Entry<String, String> entry : nodeAttributes.entrySet()) {
			
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println("key = " + key + " value = " + value);
			
		}
		
		// Rendering the node and each children of this node
		if (parentNode.hasChildNodes()) {

			NodeList childrenList = parentNode.getChildNodes();
			Integer numberOfChildren = childrenList.getLength();
			
			System.out.println("numberOfChildren = " + numberOfChildren);
			for (int index = 0; index < numberOfChildren; index++) {
				
				Node child = childrenList.item(index);

				if (child == null) {
					continue; // due to the fact that we remove children
				}
				
				// if the child is a grouping node, we launch again the function
				if (child.getNodeName() == "g") {
					
					System.out.println("launching again");
					renderGroupingNodes(child, dom);
				}
				
				// otherwise we do the rendering by modifying the other children
				
				// Getting the other children
				System.out.println("    child is a = " + child.getNodeName() + " parent " + index);
				
				// IMAGE PART (do not have to appear in the svg renderer, better to improve the dot export and mapping the shapes.)
				// removing all images from svg TODO
				if (child.getNodeName().equals("image")) {
					
					// faire toutes les shapes de remplacement et les tester
					// faire un mapping entre ces shapes et les noms des images
					// lire les attributs de image
					
					HashMap<String, String> imageAttr = accessAttributesOfNodes(child);
					for (Entry<String, String> entry : imageAttr.entrySet()) {
						System.out.println("		cle = " + entry.getKey() + " ### valeur = " + entry.getValue());
						if (entry.getKey().equals("xlink:href")) {
							
							String pathImage = entry.getValue();
							String imageName = pathImage.replaceAll("^.*/", "");
							System.out.println("in the perturbation road...1 = " + imageName);
							
							if (imageName.matches("^perturbation.*")) {
								
								System.out.println("in the perturbation road...2");
								
								// Creating a new Element
								Element newChild = dom.createElement("polygon");
								// Replace image node by polygon node
								parentNode.replaceChild(newChild, child);
								// Add attributes for this new node, TODO layout problem for the shape here
								newChild.setAttribute("points", "100,100 125,125 100,150 200,150 175,125 200,100 100,100");
								
							} else if (imageName.matches("^geneticEntity.*")) {
								// http://my.opera.com/lugansk/blog/2008/07/20/rounded-corners-in-svg-making-not-all-corners-round
								// here the half rounded rectangle is built for the naf
								
								// a rounded rectangle and two rectangles for normal corners
								Element roundedRect = dom.createElement("rect");
								Element rect1 = dom.createElement("rect");
								Element rect2 = dom.createElement("rect");
								parentNode.replaceChild(rect2, child);
								parentNode.insertBefore(rect1, rect2);// TODO find a way to add #TEXT for carriage return lol
								parentNode.insertBefore(roundedRect, rect1);
								
								// adding attributes for rectangle
								setAttributesRoundedRect(roundedRect, "", "", "", "", "", "");
								setAttributesRect(rect1, "", "", "", "");
								setAttributesRect(rect2, "", "", "", "");
								
								// three lines to cover stroked borders
								Element linkLine = dom.createElement("line");
								Element upLine = dom.createElement("line");
								Element rightLine = dom.createElement("line");
								parentNode.appendChild(linkLine);
								parentNode.appendChild(upLine);
								parentNode.appendChild(rightLine);
								
								// adding attributes for line
								setAttributesLine(linkLine, "", "", "", "");
								setAttributesLine(upLine, "", "", "", "");
								setAttributesLine(rightLine, "", "", "", "");

							} else if (imageName.equals("multimerNAF.png")) {
								
								System.out.println("in the perturbation road...3");

							}
							
						}
							
					}
				}

				// NEW CUSTOM SVG SHAPES
				
		 		// remplace shape Ø (text without shape) of empty set (TODO move this step: it is not related to rendering but dot --> svg)
				if (child.getNodeName().equals("text") && child.getTextContent().equals("Ø")) {
					
					// replace the node by a circle
					Element circle = dom.createElement("circle");
					parentNode.replaceChild(circle, child);
					setAttributesCircle(circle, "", "", "");
					
					// add a 45 degrees sloped line
					Element line = dom.createElement("line");
					parentNode.appendChild(line);
					setAttributesLine(line, "", "", "", "");
					
				}
				
				// COMPLEXES
				// i do not think there is a way to recognize if in svg we have a complex.
				if (child.getNodeName().equals("complex")) { // impossible condition
					
					// shape of a complex: octogon
					Element octa = dom.createElement("polygon");
					parentNode.appendChild(octa);
					octa.setAttribute("points", "");
				
					// decorations????????? !
					// take the glyphs shapes of the complex components and put them in the complex octa
					// this step will be in another converter (from ? to svg)
					
				}
				
				// MULTIMERS
				// complex shapes: will be based on the primary shape with an additional basic shape
				if (child.getNodeName().equals("multimer")) { // impossible condition
					
					// retrieve the g node of the normal node
					
					// then add underlining shape
					
					// add also a cardinality tag with N:<int>
					
					
				}
				
				// CLONES MARKERS
				// not in the rendering part (SBGN --> SVG)
				// solution gradient, see examples in dir
				// offset: http://www.learnsvg.com/html/bitmap/chapter07/page07-1.php
				
				
				// SUBMAP COMPLICATION
				// This has to be generated dynamically: js
				// creation of only one shape, which will be rotated for our purposes then we add a text tag.
				// add also the corresponding arcs
				/*
<svg xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink">
<g>
    <rect x="50" y="50" height="110" width="110"
          style="stroke:#ff0000; fill: #ccccff"
          transform="translate(30) rotate(45 50 50)"
            >
    </rect>
    <text x="70" y="100">Hello World</text>
</g>
</svg>
				 */
				
				// PROCESS NODES
				// CONNECTING ARCS
				// LOGICAL OPERATORS
				
				
				// ADDING rendering
				// we have to add js functions in order to have a dynamic way
				
				
				
			}

			// ... and modifying the dom if necessary

		}

		// do some others modifications...

	}

	
	
	
	/**
	 * Render the tree {@link Element} by modifying its attributes.
	 * Modify the given {@link Document} dom.
	 * 
	 */
	private static void renderTree(Element tree, Document dom) {

		System.out.println("in render tree");
		
		if (tree.getNodeName() != "svg") {
			return;
		}
		
		HashMap<String, String> treeAttributes = accessAttributesOfNodes(tree);
		if (treeAttributes == null) {
			return;
		}
		
		// Retrieving the attributes in the HashMap
		for (Entry<String, String> entry : treeAttributes.entrySet()) {
			
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println("cle = " + key + " value = " + value);
			
			// test of attribute modification
			if (key.equals("height")) {
				System.out.println("confirmation passage in LOOP IF");
				tree.setAttribute("height", "1111cm");
				
			}
			
		}
		
	}

	/**
	 * Set circle attributes. Do not make any layout calculations.
	 * @param circle
	 * @param {@link String} cx, {@link String} cy, {@link String} r
	 */
	private static void setAttributesCircle(Element circle, String cx, String cy, String r) {
		
		circle.setAttribute("cx", cx);
		circle.setAttribute("cy", cy);
		circle.setAttribute("r", r);
		return;
	}
	
	
	/**
	 * Set the attributes of a SVG line. Do not make any layout calculations.
	 * @param Attributes: {@link String} x1, {@link String} y1, {@link String} x2, {@link String} y2
	 * @param line
	 */
	private static void setAttributesLine(Element line, String x1, String y1, String x2, String y2) {

		line.setAttribute("x1", x1);
		line.setAttribute("y1", y1);
		line.setAttribute("x2", x2);
		line.setAttribute("y2", y2);
		return;
	}
	
	/**
	 * Set the attributes of a rectangle. Do not make any layout calculations.
	 * @param rect
	 * @param {@link String} x, {@link String} y, {@link String} width, {@link String} height, {@link String} rx, {@link String} ry
	 */
	private static void setAttributesRect(Element rect, String x, String y, String width, String height) {
		
		rect.setAttribute("x", x);
		rect.setAttribute("y", y);
		rect.setAttribute("width", width);
		rect.setAttribute("height", height);
		return;
	}
	
	/**
	 * Set the attributes of a rounded rectangle. Do not make any layout calculations.
	 * @param rect
	 * @param {@link String} x, {@link String} y, {@link String} width, {@link String} height, {@link String} rx, {@link String} ry
	 */
	private static void setAttributesRoundedRect(Element rect, String x, String y, String width, String height, String rx, String ry) {
		
		rect.setAttribute("x", x);
		rect.setAttribute("y", y);
		rect.setAttribute("width", width);
		rect.setAttribute("height", height);
		rect.setAttribute("rx", rx);
		rect.setAttribute("ry", ry);
		return;
	}
	
	
	/**
	 *
	 * Convert a DOM document into xml file
	 * @param doc
	 * @param filename
	 */
	public static void writeXmlFile(Document doc, String filename) {
		// code from http://www.exampledepot.com/egs/javax.xml.transform/WriteDom.html
		System.out.println("writing...");
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			// Prepare the output file
			File file = new File(filename);
			Result result = new StreamResult(file);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			
			System.out.println("dom doc...");
			xformer.transform(source, result);// error in the writing step, check XML namespaces 
			// TODO xlink namespace was undefined xmlns:xlink
			System.out.println("... done");
			
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
	}
	
	
	public static void main(String[] args) throws FileNotFoundException {

		// defining input and output file
		//String inputFileName = SvgRenderer.class.getResource("example_out.svg").toString();
		// remove "file\:"
		//inputFileName = inputFileName.substring(5);
		
		String inputFileName = 
			"/automount/nas17b_vol-vol_homes-homes/jalowcki/workspace/sbfc/sbfc/trunk/src/org/sbfc/converter/svg/example_out.svg";
		String outputFileName = 
			"/automount/nas17b_vol-vol_homes-homes/jalowcki/workspace/sbfc/sbfc/trunk/src/org/sbfc/converter/svg/example_out2.svg";
		
		// String outputFileName = SvgRenderer.class.getResource("example_out2.svg").toString();
		// outputFileName = outputFileName.substring(5);
		System.out.println("outputfilename = "+ outputFileName);

		
		
		if (inputFileName == null) {
			System.out.println("In main: inputFileName == null");
			System.exit(1);
		}

		System.out.println("inputFileName = " + inputFileName);

		
		//build the DOM document
		Document dom; // = JAXPFacade.getInstance().create(new FileInputStream(inputFileName), false);

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);

			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder builder = factory.newDocumentBuilder();
			System.out.println("Namespace aware : " + builder.isNamespaceAware());
			System.out.println("Validating : " + builder.isValidating());

			dom = builder.parse(new FileInputStream(inputFileName));

		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}

		// get a tree element, ROOT element (name should be: svg), root=all children are under
		Element tree = dom.getDocumentElement();
		
		System.out.println("ROOT? = " + tree.getNodeName());
		
		// Modifying the tree node
		renderTree(tree, dom);
		

		// Looping over all children of the tree
		if (tree.hasChildNodes()) {

			// on dresse la liste des childNodes
			NodeList nl = tree.getChildNodes();
			System.out.println("length NodeList of tree = " + nl.getLength()); // just FYI
			
			// looping over the children
			for (int index=0; index < nl.getLength(); index++) {

				Node node = nl.item(index);
				System.out.println(node.getNodeName());

				if (node.getNodeName() == "g") {
					
					renderGroupingNodes(node, dom);
				
				}// else : what can happen???

			}

		} else {
			
			System.out.println("Warning: root element do not have any child!");
		}

		writeXmlFile(dom, outputFileName);
		
		//do not forget this class: ElementUtilities.
	}
	
	
}
		/*
		 * 
		 * for each node:
		 * 		
		 * 		// verifier les shapes
		 * 
		 * 
		 * 
		 * 		// remplacer les images de shapes par d'autres shapes en svg
		 * 
		 * <g id="node2" class="node"><title>cLc</title>
<image xlink:href="/automount/nas17b_vol-vol_homes-homes/jalowcki/workspace/sbfc/sbfc/bin/org/sbfc/converter/sbml2dot/dotImages/multimerNAF.png" width="238px" height="87px" preserveAspectRatio="xMinYMin meet" x="191" y="-451.5"/>
<text text-anchor="middle" x="310" y="-405.5" font-family="Times Roman,serif" font-size="10.00">LHY protein in cytoplasm</text>
</g>
		 * 
		 * 		// ajouter gradient de couleur
		 * 
		 * 		// ajouter stroke de couleur
		 * 
		 * 
		 * 		// render les strokes
		 * 
		 * 
		 * 		// ajouter on mouse effects
		 * 			on nodes
		 * 
		 * 			on process nodes, highlight les arcs des reactants, products and modifiers when onmouse process nodes.
		 * 
		 * 			+ effet sur les nodes en question (highlight + leger)
		 * 			
		 * 		// display Reaction parameters : KineticLaw, Math Local Parameter
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */



		//TAGS: js in svg http://apike.ca/prog_svg_jsanim.html

		//Cleansing the dot file:
		//TODO replace the images by svg high-level handmade shapes



		//svg windows: http://www.carto.net/papers/svg/samples/
		//svg editor: http://www.resource-solutions.de/svgeditor/
		//and finally tutos: http://www.kevlindev.com/tutorials/basics/index.htm


		//Rendering part
		//TODO nodes color background, stroke
		//TODO nodes hightlight onmouse
		//TODO nodes change color if clicked
		//TODO arcs color?
		//TODO reaction hightlight
		/*TODO add filters for shadow...
		 * see : http://www.w3schools.com/svg/tryit.asp?filename=feoffset_1&type=svg
		 * and : http://www.w3schools.com/svg/tryit.asp?filename=filter_6&type=svg
		 */


		//TODO Accessing the annotations
		//TODO By clicking on the element concerned, links should appear


		//Zooming
		//TODO traditionnal zoom on the graph
		//TODO zoom by diplaying informations or not, it will then be possible to extend/collapse submaps




		//search elements
		// seems possible to have html embbed in svg eg: http://starkravingfinkle.org/blog/2007/07/firefox-3-svg-foreignobject/
		//TODO  a search tab
		//TODO  a search tree (species, processes)
		// tree in dhtml/js
		// simple ex: http://www.java2s.com/Code/JavaScript/GUI-Components/Explorerbasedontree.htm
		// simple tree, hightlighting design changment: http://www.java2s.com/Code/JavaScript/GUI-Components/Changetreeexpandandcollapseicons.htm
		// this one add expand posibilities for whole tree, branch
		// http://www.java2s.com/Code/JavaScript/GUI-Components/ExpandCollapseCloseOpenselectedTreeitemandbranch.htm

		//Motions
		//TODO be able to move objects

		//TODO be able to set the coloring for each part of the graph
		//TODO ep nodes, arcs, process nodes, total background




