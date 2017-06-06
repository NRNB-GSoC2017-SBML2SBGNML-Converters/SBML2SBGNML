/*
 * $Id: SBGNML2Processing.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/processing/SBGNML2Processing.java $
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
package org.processing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
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

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase;
import org.sbgn.bindings.Sbgn;
import org.sbgn.bindings.Arc.Next;
import org.sbgn.bindings.SBGNBase.Extension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This class enables to read a SBGN-ML file in order to prepare graph data for processing.
 * The SBGN-ML file is read using libSBGN milestone 1.
 * 
 * @author jalowcki
 *
 */
public class SBGNML2Processing {

	// link sbgn and processing shapes
	private static final HashMap<String, String> mapping = buildMappingForProcessing();
	// the name of the input SBGN-ML file
	// ex: /automount/nas17b_vol-vol_homes-homes/jalowcki/ML_files/mapk_cascade_SBGN.xml
	private String sbgnmlFileName = "";
	// the name of the output processing file which will contain the data
	private String processingFileName = "graphData.pde";
	// a Sbgn object in which SBGN-ML data will be loaded
	private Sbgn sbgnObj = new Sbgn();
	// This array will gather every data contained in the SBGN-ML file through the Sbgn object.
	private static ArrayList<String> graphData = new ArrayList<String>();
	// define the width and the height of the processing display window (total size of the SBGN graph)
	private static int WINDOW_WIDTH = 0;
	private static int WINDOW_HEIGHT = 0;
	// width between the graph and the border
	private static int WINDOW_YBORDER = 1000;
	private static int WINDOW_XBORDER = 1000;
	
	// This array will contain the minimum data for the javascript tree which will be related to the graph
	private static List<String> treeData = new ArrayList<String>();
	
	public SBGNML2Processing() {
	}
	
	
	/**
	 * Take a {@link File} and build a {@link ArrayList} which will be used next for processing.
	 * @param fileInput
	 */
	private void readSbgnmlFile(File fileInput) {
		
		// xml and sbgn tags
		try {
			sbgnObj = SbgnUtil.readFromFile(fileInput);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		// write the processing header at the beginning of the file
		graphData.add( "/*\n  PROCESSINGJS.COM HEADER ANIMATION\n  MIT License - Hyper-Metrix.com/F1LT3R\n" +
				"  Native Processing compatible\n */ \n\n" );
		
		// write a comment for the COUNT processing variable
		graphData.add( "// Defining and limiting the number of nodes\n" );
		
		// get the list of glyphs
		List<Glyph> listOfGlyphs = sbgnObj.getMap().getGlyph();
		
		// get the list of arcs
		List<Arc> listOfArcs = sbgnObj.getMap().getArc();
			
		// we start now to write the beginning of the global array of processing shapes
		graphData.add( "Shape[] SHAPES = {\n" );
		
		// Glyph shape constructor:
		// shape specificity / cloning / label / bbox / [tag/port/comp label] / extension
		
		// read all the SBGN-ML glyphs
		int glyphCounter = printListOfGlyphs(listOfGlyphs, 0);
		
		// again arcs counting for the same purpose as for glyphs
		int counter = 0;
		
		// get the arcs
		for (Arc a : listOfArcs) {

			//////////// this part is meant to fill the ArrayList treeData for the xml tree
			
			// retrieve first name and id of the arc for the processing tree, the order of the adds has to be kept.
			treeData.add(a.getClazz());

			String textSource = "";
			String textTarget = "";
			// temporary solution for id of Arcs
			String idOfArc = "";
			
			// Depending if the source/target is a port or a glyph, we will retrieve the text from either port or label.
			if ( (a.getSource().getClass() == Glyph.class) && (a.getTarget().getClass() == Glyph.class) ) {
				
				Label labelSource = ((Glyph) a.getSource()).getLabel();
				Label labelTarget = ((Glyph) a.getTarget()).getLabel();
				
				if ( labelSource != null ) {
					textSource = labelSource.getText(); 
				} else {
					textSource = ((Glyph) a.getSource()).getId();
				}
				
				if ( labelTarget != null ) {
					textTarget = labelTarget.getText();
				} else {
					textTarget = ((Glyph) a.getTarget()).getId();
				}
				
				treeData.add(textSource+"->"+textTarget);
				// build the id for this case, this should be temporary, when arcs will will implement getId()
				idOfArc = ((Glyph) a.getSource()).getId() + "_" + ((Glyph) a.getTarget()).getId();
				
			} else if ( (a.getSource().getClass() == Port.class) && (a.getTarget().getClass() == Glyph.class) ) {
				
				Label labelTarget = ((Glyph) a.getTarget()).getLabel();
				
				if ( labelTarget != null ) {
					textTarget = labelTarget.getText();
				} else {
					textTarget = ((Glyph) a.getTarget()).getId();
				}
				
				treeData.add(((Port) a.getSource()).getId()+"->"+textTarget);
				// build the id for this case, this should be temporary, when arcs will will implement getId()
				idOfArc = ((Port) a.getSource()).getId() + "_" + ((Glyph) a.getTarget()).getId();

			} else if ( (a.getSource().getClass() == Glyph.class) && (a.getTarget().getClass() == Port.class) ) {

				Label labelSource = ((Glyph) a.getSource()).getLabel();
				
				if ( labelSource != null ) {
					textSource = labelSource.getText(); 
				} else {
					textSource = ((Glyph) a.getSource()).getId();
				}
				
				treeData.add(textSource+"->"+((Port) a.getTarget()).getId());
				// build the id for this case, this should be temporary, when arcs will will implement getId()
				idOfArc = ((Glyph) a.getSource()).getId() + "_" + ((Port) a.getTarget()).getId();
				
			} else {
				
				System.out.println("Error: Source and target are together related to Port objects");
				System.exit(1);
			}

			
			// cast glyph object for source and target
			if ( (a.getSource().getClass() == Glyph.class) && (a.getTarget().getClass() == Glyph.class) ) {
				
				treeData.add(((Glyph) a.getSource()).getId()+"_"+((Glyph) a.getTarget()).getId());
				
			} else if ( (a.getSource().getClass() == Port.class) && (a.getTarget().getClass() == Glyph.class) ) {
				
				treeData.add(((Port) a.getSource()).getId()+"_"+((Glyph) a.getTarget()).getId());

			} else if ( (a.getSource().getClass() == Glyph.class) && (a.getTarget().getClass() == Port.class) ) {
			
				treeData.add(((Glyph) a.getSource()).getId()+"_"+((Port) a.getTarget()).getId());
				
			} else {
				
				System.out.println("Error: Source and target are together related to Port objects");
				System.exit(1);
			}
			
			////////// end of the xml tree generation
			
			// building a glyph string for each arc
			String glyph = "new " + mapping.get(a.getClazz());
			
			// Add an id for the processing object, ids on arcs are now compulsory in SBGN-ML
			// but this feature is for the next milestone
			/*
			if (a.getId() != null) {
				
				glyph += a.getId() + ",";
			} else {
				// if no id, file is invalid and conversion cannot happen
				System.out.println("An SBGN-ML arc has no id!");
				System.exit(1);
			}
			*/
			// so we build an artificial id with source and _ and target TODO remove this trick and replace it by a.getId()
			glyph += "\"" + idOfArc + "\",";
			
			
			// we admit that modulation arcs do not point on port but on glyph
			
			// add coordinates of the first control point from the source...
			glyph += a.getStart().getX() + "," + a.getStart().getY() + ",";
			
			// ... and modify the size of the window if necessary
			adaptWindowSize(a.getStart().getX(), a.getStart().getY());
			getWindowScaleFrame(a.getStart().getX(), a.getStart().getY());
			
			// add coordinates of the second control point from the end...
			glyph += a.getEnd().getX() + "," + a.getEnd().getY();

			// ... and adapt the size of the window if in need
			adaptWindowSize(a.getEnd().getX(), a.getEnd().getY());
			getWindowScaleFrame(a.getEnd().getX(), a.getEnd().getY());
			
			// check if the class is a modulation by looking in the subtree of 168. Not needed for the moment
			// if (SBO.isChildOf(SBGNUtils.getSBOTermThroughSBGNClass(a.getClazz()), 168)) {}
			
			// get putative anchor points
			if (a.getNext() != null) {
				
				// open a processing float array
				glyph += ", new float[] {";
				
				int numberCounter = 0;
				for (Next n : a.getNext()) {
					
					numberCounter++;
					
					// add a processing float array to store anchors coordinates...
					glyph += n.getX() + "," + n.getY();
		
					// ... and adapt the size window if needed
					adaptWindowSize(n.getX(), n.getY());
					getWindowScaleFrame(n.getX(), n.getY());
					
					// add a comma until the last float is read
					if ( numberCounter != a.getNext().size() ) {
						glyph += ",";
					}
				}
				
				// close the processing float array
				glyph += "}";
			}
			
			// close the arc constructor, add a comma for the next one
			glyph += "),";
			
			// add carriage return each 2 glyphs to improve human readability
			if ( counter % 2 == 0 ) {
				glyph += "\n" ;
			}
			
			// add the arc as string in the data array
			graphData.add(glyph);
		}
		
		// take the last glyph and remove useless comma
		String last = graphData.get(graphData.size()-1);
		
		// remove the last element
		graphData.remove(graphData.size()-1);
		
		// re-put it w/o the comma
		graphData.add(last.replaceAll( ",$", "" ));
		
		// close the processing array of shapes
		graphData.add("};\n\n");
		
		// each SBGN-ML glyph and arc will give one shape in processing so we deduce the total of shapes
		int count = glyphCounter + listOfArcs.size();
		graphData.add( "static int COUNT = " + count + ";\n\n" );
		
		// add the width and height of the map
		int ww = WINDOW_WIDTH + WINDOW_XBORDER;
		int wh = WINDOW_HEIGHT + WINDOW_YBORDER;
		System.out.println("ww = "+ww+" wh = "+wh);
		graphData.add(
				"static int WINDOW_WIDTH = " + ww + ";\n" + 
				"static int WINDOW_HEIGHT = " + wh + ";"
				);
		
	}
	
	/**
	 * Print glyphs from a given list of glyphs. The integer counter is returned for recursion.
	 * @return counter
	 */
	private static int printListOfGlyphs(List<Glyph> log, int counter) {

		// loop over the list of glyphs, get the first in line
		for (Glyph g : log) {

			// retrieve first name and id of the glyph for the processing tree, the order of the add has to be kept.
			treeData.add(g.getClazz());
			if (g.getLabel() != null) {
				// give the label for tree leaves
				treeData.add(g.getLabel().getText());
			} else {
				// give the id if there is no label available
				treeData.add(g.getId());
			}
			treeData.add(g.getId());
			
			// print the parent/current glyph

			counter++;

			System.out.println("glyph name = "+g.getClazz());

			// warn and stop the program if a mapping cannot be retrieved
			if ( mapping.get(g.getClazz()) == null ) {
				System.out.println("SBGN class \""+g.getClazz()+"\" has not found any equivalent in the mapping function.");
				System.exit(1);
			}

			// this string will be the definition of the shape in processing
			String glyph = "new " + mapping.get(g.getClazz());

			// In practice only EPNs can support a clone auxiliary unit
			if (canBeCloned(g.getClazz())) {

				// if there is a clone tag
				if (g.getClone() != null) {

					glyph += "1";
				} else {

					glyph += "0";
				}

				glyph += ",";
			}

			// add an id for this object, the same as in SBGN-ML
			if (g.getId() != null) {
				
				glyph += "\"" + g.getId() + "\",";
			} else {
				// if no id, file is invalid and conversion cannot happen
				System.out.println("An SBGN-ML glyph has no id!");
				System.exit(1);
			}
			
			
			// if the glyph can support a label (= has processing constructors with labels)
			if (canBeLabeled(g.getClazz())) {

				// get the label of the glyph
				Label lab = g.getLabel();

				// if a label is set...
				if (lab != null) {

					System.out.println("glyph "+g.getClazz()+" has label = "+g.getLabel().getText());

					// add the text of the label to constructors
					// the label for the unit of information is comprised
					String label = "new String[] {";

					// escape every carriage return for processing labels
					String[] labelPieces = lab.getText().split("\n");

					
					for (int i=0; i<labelPieces.length; i++) {

						// if the line is the last (or only) line of the label
						if (i+1 == labelPieces.length) {
							
							label += "\"" + labelPieces[i] + "\"";
						} else {
						
							// add a carriage return for processing
							label += "\"" + labelPieces[i] + "\\n\",";
						}
					}
					// label are contained in a String[] in order to know how many line(s) the label will have
					glyph += label + "},";

					// in the case of the compartment, a bounding box has to be set
					if (g.getClazz().equals("compartment")) {

						// create a rectangular glyph which has to behave like a unit of information
						Bbox b = lab.getBbox();
						if (b == null) {
							System.out.println("Error: Label has no specific bounding box.");
							System.exit(1);
						}
						
						// calculate the size of the window
						adaptWindowSize(b.getX() + b.getW(), b.getY() + b.getH());
						getWindowScaleFrame(b.getX(), b.getY());
						
						glyph += b.getX() + "," + b.getY() + "," + b.getW() + "," + b.getH() + ",";
					}

					
				} else {

					// add label for state variable (it is not a SBGN label stricto sensu)
					if (g.getClazz().equals("state variable")) {

						if (g.getState() != null) {

							// state variable + value
							if (g.getState().getVariable() != null) {
								glyph += "new String[] {" + "\"" + g.getState().getValue() + "@" + g.getState().getVariable() + "\"},";
							} else {

								glyph += "new String[] {" + "\"" + g.getState().getValue() + "\"},";
							}
						} else {
							
							System.out.println("state value cannot be null");
							glyph += "new String[] {\"\"},";
						}
					} else {
						
						// add a void label to match processing constructors
						glyph += "new String[] {\"\"},";
					}
				}
			}
			

			// get the bounding box of the glyph
			Bbox b = g.getBbox();

			// add coordinates as well
			glyph += b.getX() + "," + b.getY() + "," + b.getW() + "," + b.getH();

			// and also modify the global size of the window if necessary
			adaptWindowSize(b.getX() + b.getW(), b.getY() + b.getH());
			getWindowScaleFrame(b.getX(), b.getY());
			
			// for the tag, specify the direction of the glyph
			if ( g.getClazz().equals("tag") || g.getClazz().equals("terminal") ) {

				String or = g.getOrientation();
				if (or.equals("right")) {
					glyph += ",0"; // right oriented
				} else {
					glyph += ",1"; // left oriented
				}

			} else if (g.getClazz().matches("process|association|dissociation|and|or|not")) {
				// SBGN classes which can carry 2 ports

				// the first port
				String first = "";
				// the second port
				String second = "";

				// get both ports coordinates for processes
				for ( Port p : g.getPort() ) {

					if (p.getId().matches(g.getId() + ".1")) {

						// id of the first port
						first += "," + p.getX() + "," + p.getY();
					} else {

						// id of the second port
						second += "," + p.getX() + "," + p.getY();
					}
				}

				// add the ports to the constructor
				glyph += first + second;
			}

			// add an additionnal comma because there will be at least a void annotation
			glyph += ",";

			// if there an additional comment in Extension tag
			if (g.getExtension() != null) {

				// function that will return a string with:
				//  1: the content mathML ready to use by html
				//  2: ? (annotation content with qualifier/uri/url)
				glyph += readExtension(g.getExtension());
			} else {

				// we write a void annotation so that we do not have to create other constructors in processing
				glyph += "new String[] {\"\"}";
			}

			// close the glyph constructor and add a carriage return to improve the human readability
			glyph += "),\n";

			System.out.println("    "+glyph);

			// add the new glyph as string in the array
			graphData.add(glyph);

			// if the current glyph has one/more child, recall the function
			if (!g.getGlyph().isEmpty()) {
				counter = printListOfGlyphs(g.getGlyph(), counter);
			}
		}
		// return glyph counter for recursion
		return counter;
	}

	
	/**
	 * Return a string with the content mathML, ready to be used in processing contructors, and annotations at last.
	 * @param {@link Extension} from the {@link SBGNBase}
	 * @return
	 */
	private static String readExtension(Extension ext) {
		
		String extensionString = "";
		
		// loop over the children of the extension tag
		System.out.println("size = " + ext.getAny().size());

		// define string for mathML and annotation
		String math = "";
		String anno = "";

		for (Element e : ext.getAny()) {
			
			System.out.println("element = "+e.getTagName());
			
			System.out.println("test name attr = "+e.getNodeName());
			
			// retrieve content of interest (here: mathML and annotations)
			if (e.getTagName().equals("math:math") || e.getTagName().equals("annotation")) {

				DOMSource domSource = new DOMSource();
				domSource.setNode(e);
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer;

				try {
					transformer = tf.newTransformer();

					try {
						transformer.transform(domSource, result);
					} catch (TransformerException e1) {
						e1.printStackTrace();
					}

				} catch (TransformerConfigurationException e1) {
					e1.printStackTrace();
				}

				String stringResult = writer.toString();
				
				if (e.getTagName().equals("math:math")) {

					math = stringResult;
				} else if (e.getTagName().equals("annotation")) {

					anno += "new String[] {";
					
					// counter for children with non null local name
					int countNonNull = 0;
					
					// loop over and get all children of the annotation
					for (int index=0; index<e.getChildNodes().getLength(); index++) {
						
						Node child = e.getChildNodes().item(index);
						
						if (child.getLocalName() != null) {
							
							// if not the first cvterm or the last or equals to null, add comma
							if (countNonNull != 0 && index != e.getChildNodes().getLength()-1 && child.getLocalName() != null) {
								
								anno += ",";
							}
							
							anno += "\"" + child.getLocalName() + "\",";

							// get urn in the resource tag
							anno += "\"" + child.getChildNodes().item(0).getAttributes().item(0).getNodeValue() + "\"";
							
							countNonNull++;
						}
					}
					
					// close the sting array for annotations
					anno += "}";
				}
			}
		}
		// the html page will have to display a message if there is no mathML or annotations.
		
		// concatenate mathML and annotation in one string if available

		// if there is some content mathML, add them.
		if (!math.equals("")) {
			extensionString = "\"" + math + "\"" + ",";
		}

		// if there is some annotation, add them.
		if (!anno.equals("")) {
			extensionString = anno;
		}
		
		// if the extension string is not void, remove the last comma.
		if (!extensionString.equals("")) {
			extensionString.replaceAll(",$", "");
		}
		
		return extensionString;
	}
	
	
	/**
	 * Map one by one every {@link Glyph} in their definition in processing constructors.
	 * Sometimes the name of a class is merely returned but numbers can also be a part of it. For more details about processing,
	 * see the documentation of the processing classes. Names for the mapping emerge from the SBGN PD spec table 3.1 and test files of ML1.
	 * @param Glyph g
	 * @return processingString
	 */
	private static HashMap<String, String> buildMappingForProcessing() {
		
		// here we define the mapping
		HashMap<String, String> mapping  = new HashMap<String, String> ();

		// 11 entity pool nodes
		mapping.put("unspecified entity", "UnspecifiedEntity(");
		mapping.put("simple chemical", "SimpleChemical(0,");
		mapping.put("macromolecule", "Rectangle(4,0,0,");
		mapping.put("nucleic acid feature", "Rectangle(2,0,0,");
		mapping.put("perturbation", "PerturbingAgent(");
		mapping.put("source and sink", "Sink(");
		mapping.put("complex", "Rectangle(5,0,0,");
		mapping.put("macromolecule multimer", "Rectangle(4,1,0,");
		mapping.put("complex multimer", "Rectangle(5,1,0,");
		mapping.put("nucleic acid feature multimer", "Rectangle(2,1,0,");
		mapping.put("simple chemical multimer", "SimpleChemical(1,");
		
		// 6 process nodes
		mapping.put("process", "SquareNode(0,");
		mapping.put("omitted process", "SquareNode(1,");
		mapping.put("uncertain process", "SquareNode(2,");
		mapping.put("association", "CircularNode(0,");
		mapping.put("dissociation", "CircularNode(1,");
		mapping.put("phenotype", "Phenotype(");
		
		// 2 reference nodes + the terminal
		mapping.put("tag", "Tag(1,");
		mapping.put("submap", "Rectangle(0,0,0,0,");
		mapping.put("terminal", "Tag(0,");

		// 9 connecting arcs
		mapping.put("consumption", "ArcShape(0,");
		mapping.put("production", "ArcShape(1,");
		mapping.put("modulation", "ArcShape(2,");
		mapping.put("stimulation", "ArcShape(3,");
		mapping.put("catalysis", "ArcShape(4,");
		mapping.put("inhibition", "ArcShape(5,");
		mapping.put("necessary stimulation", "ArcShape(6,");
		mapping.put("logic arc", "ArcShape(7,");
		mapping.put("equivalence arc", "ArcShape(7,");
		
		// 3 logical operators
		mapping.put("and", "LogicalOperator(\"AND\",");
		mapping.put("or", "LogicalOperator(\"OR\",");
		mapping.put("not", "LogicalOperator(\"NOT\",");

		// 1 container node
		mapping.put("compartment", "Compartment(");

		// 2 auxiliary units
		mapping.put("unit of information", "UnitOfInfo(");
		mapping.put("state variable", "StateVariable(");

		return mapping;
	}
	
	
	/**
	 * Tell if a glyph in SBGN can support a clone marker, see section 2.3.3 Glyph: Clone marker of SBGN specifications
	 * @param glyphClass
	 * @return
	 */
	private static Boolean canBeCloned(String glyphClass) {
		
		if ( glyphClass.matches(
				"unspecified entity||" +
				"simple chemical$||" +
				"macromolecule$||" +
				"nucleic acid feature$||" +
				"perturbation||" +
				"complex$||" +
				"macromolecule multimer||" +
				"complex multimer||" +
				"nucleic acid feature multimer||" +
				"simple chemical multimer"
				)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Tell if a glyph in SBGN can support a label, see section from section 2.4 of SBGN specifications
	 * NB: logical operators do not support any label.
	 * @param glyphClass
	 * @return
	 */
	private static Boolean canBeLabeled(String glyphClass) {
		
		if ( glyphClass.matches(
				"unspecified entity||" +
				"simple chemical$||" +
				"macromolecule$||" +
				"nucleic acid feature$||" +
				"perturbation||" +
				"complex$||" +
				"macromolecule multimer||" +
				"complex multimer||" +
				"nucleic acid feature multimer||" +
				"simple chemical multimer||" +
				"tag||" +
				"terminal||" +
				"submap||" +
				"phenotype||" +
				"unit of information||" +
				"state variable||" +
				"compartment"
				)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Tell if a glyph in SBGN can support a label, see section from section 2.4 of SBGN specifications
	 * NB: logical operators do not support any label.
	 * @param glyphClass
	 * @return
	 */
	private static Boolean canCarryUnitOrStateGlyph(String glyphClass) {
		
		if ( glyphClass.matches(
				"unspecified entity||" +
				"simple chemical$||" +
				"macromolecule$||" +
				"nucleic acid feature$||" +
				"perturbation||" +
				"complex$||" +
				"macromolecule multimer||" +
				"complex multimer||" +
				"nucleic acid feature multimer||" +
				"simple chemical multimer||" +
				"compartment"
				)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * According to the given x and y, the function keeps or replaces the value of the window size
	 *  (width and height) by the maximum. Processing window width and height are kept as global variables.
	 * @param x
	 * @param y
	 */
	private static void adaptWindowSize(float x, float y) {
		
		int xInt = (int) x;
		int yInt = (int) y;

		// compare x to the width max
		WINDOW_WIDTH = Math.max(WINDOW_WIDTH, xInt);
		WINDOW_HEIGHT = Math.max(WINDOW_HEIGHT, yInt);
	}
	
	
	/**
	 * Get the distance between the x axis (y axis) and the first glyph or arc met. These distances will be kept as global variables.
	 * These values will be used to center the SBGN graph in the processing window.
	 * @param x
	 * @param y
	 */
	private static void getWindowScaleFrame(float x, float y) {
		
		int xInt = (int) x;
		int yInt = (int) y;
		
		// compare x (y) to the width (height) from the border to the nearest graphical element
		WINDOW_XBORDER = Math.min(WINDOW_XBORDER, xInt);
		WINDOW_YBORDER = Math.min(WINDOW_YBORDER, yInt);
	}
	
	
	/**
	 * Write an xml file read by javascript in order to display a tree of the current SBGN map.
	 * The function take treeData which gathers (SBGN class, a name for the tree, an id for the tree) for all glyphs and arcs.
	 * @throws ParserConfigurationException 
	 */
	private void writeSBGNBrowsingTree() throws ParserConfigurationException {
		
		// sort all classes alphabetically in treeData

		// loop over treeData and extract the List of classes
		List<String> classes = new ArrayList<String>();
		for (int i=0; i<treeData.size(); i=i+3 ) {
			
			classes.add(treeData.get(i));
		}
		
		// sort the list of classes lexicographically
		Collections.sort(classes);
		
		// Build again the treeData list
		List<String> treeData2 = new ArrayList<String>();
		for (int i=0; i<classes.size(); i++) {
			
			String firstString = classes.get(i);
			
			int index = treeData.indexOf(firstString);
			
			// add first, second and third string and remove all of them from treeData
			treeData2.add(firstString);
			treeData.remove(index);
			treeData2.add(treeData.remove(index));
			treeData2.add(treeData.remove(index));
		}
		
		// treeData2 becomes treeData (which is now sorted according to classes)
		treeData = treeData2;
		
		// sort each sublists of names lexicographically too
		treeData2 = new ArrayList<String>();
        String firstClass = treeData.get(0);
        // a sublist to support each list with the same first element
        List<String> listOfNamesWithSameClass = new ArrayList<String>();
        // initialize sublist
        listOfNamesWithSameClass.add(treeData.get(1));

        // loop over the list
        for (int i=3;i<treeData.size()-3; i=i+3) {

        	String secondClass = treeData.get(i);
        	// if the class remains the same
        	if (firstClass.equals(secondClass)) {

        		// fill sublist with names
        		listOfNamesWithSameClass.add(treeData.get(i+1));
        	} 
        	
        	else {

        		// sort out the sublist
        		Collections.sort(listOfNamesWithSameClass);

        		// fill the main list treeData2 with sorted 3-uplets
        		for (int i2=0; i2<listOfNamesWithSameClass.size(); i2++) {

        			// add first element
        			treeData2.add(firstClass);
        			// add second element
        			treeData2.add(listOfNamesWithSameClass.get(i2));
        			// add third element from treeData
        			int indexOfName = treeData.indexOf(listOfNamesWithSameClass.get(i2))+1;
        			
        			treeData2.add(treeData.get(indexOfName));

        			// replace data from treeData to avoid errors with duplicates
        			treeData.set(indexOfName-1, "");
        			treeData.set(indexOfName, "");
        			treeData.set(indexOfName+1, "");
        		}

        		// reinitialize the subList
        		listOfNamesWithSameClass = new ArrayList<String>();
        		listOfNamesWithSameClass.add(treeData.get(i+1));
        	} 
        	firstClass = secondClass;
        }

		// finally treeData2 becomes treeData (which is now sorted)
		treeData = treeData2;
		

		// file name and path TODO make global variables/change the parameters
		String fileName = "sbgnTree.xml";
		String filePath = "/automount/nas17b_vol-vol_homes-homes/jalowcki/Desktop/processing-1.2.3/test_javascript_zoom/treeImgs/";
		
		// get dom parser ready
		// create new dom
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder constructeur = factory.newDocumentBuilder();
		Document document = constructeur.newDocument();

		// root element named tree
		Element tree = document.createElement("tree");
		// add id to the root
		tree.setAttribute("id", "0");
		document.appendChild(tree);
		
		// check treeData
		if (treeData.size()%3 != 0) {
			System.out.println("Error in writeSBGNBrowsingTree");
			System.exit(1);
		}
		
		// define an arrayList with the branches names inside
		ArrayList<String> branchClasses = new ArrayList<String>();
		
		// loop over the data from treeData
		for (int i=0; i<treeData.size(); i++) {
			
			// Element at a first position: SBGN class
			Element parent = document.createElement("item");
			
			// if this SBGN class does not have a branch yet
			if (!branchClasses.contains(treeData.get(i))) {
				
				branchClasses.add(treeData.get(i));
			
				// add text, id attributes
				parent.setAttribute("text", treeData.get(i));		
				parent.setAttribute("id", treeData.get(i));
				
				// add attributes im0, im1, im2
				parent.setAttribute("im0", "book_titel.gif");
				parent.setAttribute("im1", "book_titel.gif");
				parent.setAttribute("im2", "book_titel.gif");
				
				tree.appendChild(parent);
			} else {
				
				// get the parent of all the SBGN classes: tree
				for (int index=0; index<tree.getChildNodes().getLength(); index++) {
					
					// node could be the putative parent
					Node node = tree.getChildNodes().item(index);
					
					for (int index2=0; index2<node.getAttributes().getLength(); index2++) {
						
						// checked attributes to find the right parent
						if (node.getAttributes().getNamedItem("id").getNodeValue().equals(treeData.get(i))) {
							
							// replace the parent
							parent = (Element) node;
						}
					}
				}
			}
			
			// create a child
			Element child = document.createElement("item");
			// Second position: add name of the leaf
			i++;
			child.setAttribute("text", treeData.get(i));
			
			// Third position: add id of the leaf
			i++;
			child.setAttribute("id", treeData.get(i));
			
			// add attributes im0, im1, im2
			// TODO put appropriate images
			child.setAttribute("im0", "book_titel.gif");
			child.setAttribute("im1", "book_titel.gif");
			child.setAttribute("im2", "book_titel.gif");
			
			// add this new child
			parent.appendChild(child);
		}
		
		// and write the file
	    try {
	        // Prepare the DOM document for writing
	        Source source = new DOMSource(document);

	        // Prepare the output file
	        File file = new File(filePath+fileName);
	        Result result = new StreamResult(file);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.transform(source, result);
	    } catch (TransformerConfigurationException e) {
	    } catch (TransformerException e) {
	    }
	}
	
	
	/**
	 * Take the array of strings graphData, build a single string and write this string in the output file created on this occasion.
	 * The path of the output file remain still to be defined properly.
	 */
	private void writeProcessingData() {
		
		// convert array dataGraph into a single String
		String dataString = "";
		for (String s : graphData) {
			
			dataString += s;
		}
		
		// specify the path of the processing directory
		// TODO path is only directed for personal processing for the moment
		String filePath = this.sbgnmlFileName.replaceAll("[_A-Za-z0-9-]+.xml$", "");

		// put this data String in a file
		try {
			
			FileWriter writer = new FileWriter(filePath + processingFileName);
			writer.write(dataString);
			writer.close();
			
		} catch(IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {
		
		// Take a filename into input
		if (args.length < 1 || args.length > 2) {
			System.out.println("usage: java org.processing.SBGNML2Processing <SBGNML filename>");
			return;
		}
		
		// Read the file, create a Sbgn object...
		SBGNML2Processing s2p = new SBGNML2Processing();
		s2p.sbgnmlFileName = args[0];
		
		// get the SBGN-ML file
		File f = new File(s2p.sbgnmlFileName);
		
		// read the file with libSBGN and building the array graphData
		s2p.readSbgnmlFile(f);
		
		// convert the array graphData into String and write this String in an output file
		s2p.writeProcessingData();
		
		// write an xml file for the browsing tree
		try {
			System.out.println("browsing the tree");
			s2p.writeSBGNBrowsingTree();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		//Building the output filename path and the output filename
		String outputFilePath = s2p.sbgnmlFileName.replaceAll("[_A-Za-z0-9-]+.xml$", "");
		
		System.out.println("Writing sucessful. Output file at " + outputFilePath + s2p.processingFileName);
	}

}
