package org.sbfc.converter.sbml2sbgnml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBGNModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.SBGNBase;
import org.sbgn.bindings.Sbgn;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SBML2SBGNML_GSOC2017 extends GeneralConverter { 
	
	private static Logger logger = Logger.getLogger(SBML2SBGNML_GSOC2017.class);
	public int debugMode = 0;
	private Model sbmlModel;
	private static SBGNUtils sbu = new SBGNUtils("sbgnml");
			
	/**
	 * Create an <code>Sbgn</code>, which corresponds to an SBML <code>Model</code>. 
	 * Each element in the <code>Model</code> is mapped to an SBGN <code>Glyph</code>.
	 * 
	 * @param <code>SBMLDocument</code> sbmlDocument
	 * @return <code>Sbgn</code> sbgnObject
	 */			
	public Sbgn convertToSBGNML(SBMLDocument sbmlDocument) throws SBMLException {
		Sbgn sbgnObject = null;
		Map map = null;
		
		LayoutModelPlugin sbmlLayoutModel = null;
		ListOf<Layout> listOfLayouts = null;
		HashMap<String, Sbgn> listOfSbgnObjects = new HashMap<String, Sbgn>();	
		
		Dimensions layoutDimensions = null;
		ListOf<GraphicalObject> listOfAdditionalGraphicalObjects;
		ListOf<CompartmentGlyph> listOfCompartmentGlyphs;
		ListOf<ReactionGlyph> listOfReactionGlyphs;
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs;
		ListOf<TextGlyph> listOfTextGlyphs;		

		try { 
			sbmlModel = sbmlDocument.getModel();
		} catch(Exception e) {
			throw new SBMLException("SBML2SBGN: Input file is not a regular SBML file.");
		}
		
		if (sbmlModel.isSetPlugin("layout")){
			sbmlLayoutModel = (LayoutModelPlugin) sbmlModel.getExtension("layout");
		}

		if (sbmlModel.isSetPlugin("layout")){
			listOfLayouts = sbmlLayoutModel.getListOfLayouts();
		}
		
		for (Layout layout : listOfLayouts){
			sbgnObject = new Sbgn();
			map = new Map();
			sbgnObject.setMap(map);		
			
			listOfSbgnObjects.put(layout.getId(), sbgnObject);
			
			if (layout.isSetDimensions()){
				layoutDimensions = layout.getDimensions();
			}
			
			// note: the order of reading the lists matters
			if (layout.isSetListOfCompartmentGlyphs()){
				listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();
				createGlyphsFomCompartmentGlyphs(sbgnObject, listOfCompartmentGlyphs);
			}			
			if (layout.isSetListOfSpeciesGlyphs()){
				listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();
				createGlyphsFromSpeciesGlyphs(sbgnObject, listOfSpeciesGlyphs);
			}
			if (layout.isSetListOfAdditionalGraphicalObjects()){
				listOfAdditionalGraphicalObjects = layout.getListOfAdditionalGraphicalObjects();
				createGlyphsFromGeneralGlyphs(sbgnObject, listOfAdditionalGraphicalObjects);
			}			
			if (layout.isSetListOfTextGlyphs()){
				listOfTextGlyphs = layout.getListOfTextGlyphs();
				createLabelsFromTextGlyphs(sbgnObject, listOfTextGlyphs);
			}			
			if (layout.isSetListOfReactionGlyphs()){
				listOfReactionGlyphs = layout.getListOfReactionGlyphs();
				createGlyphsFromReactionGlyphs(sbgnObject, listOfReactionGlyphs);
			}			
		}
		
		// todo: return one of the sbgnObjects?
		return sbgnObject;		
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, each corresponding to an SBML <code>CompartmentGlyph</code>. 
	 * TODO: many things still need to be handled, see comments below
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<CompartmentGlyph></code> listOfCompartmentGlyphs
	 */			
	public void createGlyphsFomCompartmentGlyphs(Sbgn sbgnObject, ListOf<CompartmentGlyph> listOfCompartmentGlyphs) {
		Glyph sbgnCompartmentGlyph;
		Label label;
		
		for (CompartmentGlyph compartmentGlyph : listOfCompartmentGlyphs){
			sbgnCompartmentGlyph = new Glyph();
			sbgnCompartmentGlyph.setId(compartmentGlyph.getId());
			sbgnCompartmentGlyph.setClazz("compartment");
			
			sbgnObject.getMap().getGlyph().add(sbgnCompartmentGlyph);		
			
			label = new Label();
			label.setText(compartmentGlyph.getCompartment());
			//sbgnCompartmentGlyph.setLabel(label);
			
			createBBox(compartmentGlyph, sbgnCompartmentGlyph);
			
			// todo: create Auxiliary items?
		}
		// todo: need to keep compartment name
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, each corresponding to an SBML <code>SpeciesGlyph</code>. 
	 * TODO: many things still need to be handled, see comments below
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<SpeciesGlyph></code> listOfSpeciesGlyphs
	 */		
	public void createGlyphsFromSpeciesGlyphs(Sbgn sbgnObject, ListOf<SpeciesGlyph> listOfSpeciesGlyphs) {
		Glyph sbgnSpeciesGlyph;
		Label label;
		
		for (SpeciesGlyph speciesGlyph : listOfSpeciesGlyphs){
			sbgnSpeciesGlyph = new Glyph();
			sbgnSpeciesGlyph.setId(speciesGlyph.getId());
			// todo: or class could be simple chemical etc.
			sbgnSpeciesGlyph.setClazz("macromolecule");
			
			sbgnObject.getMap().getGlyph().add(sbgnSpeciesGlyph);		
			
			label = new Label();
			label.setText(speciesGlyph.getSpecies());
			sbgnSpeciesGlyph.setLabel(label);
			
			createBBox(speciesGlyph, sbgnSpeciesGlyph);
			
			// todo: create Auxiliary items?
		}
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, each corresponding to an SBML <code>ReactionGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<ReactionGlyph></code> listOfReactionGlyphs
	 */			
	public void createGlyphsFromReactionGlyphs(Sbgn sbgnObject, ListOf<ReactionGlyph> listOfReactionGlyphs) {

		for (ReactionGlyph reactionGlyph : listOfReactionGlyphs){
			createGlyphFromReactionGlyph(sbgnObject, reactionGlyph);
		}		
	}
	
	/**
	 * Create SBGN <code>Glyph</code> and <code>Arc</code>, corresponding to an SBML <code>ReactionGlyph</code>. 
	 * TODO: many things still need to be handled, see comments below
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ReactionGlyph</code> reactionGlyph
	 */		
	public void createGlyphFromReactionGlyph(Sbgn sbgnObject, ReactionGlyph reactionGlyph) {
		Glyph processNode = null;
		Label label;	
		Arc arc;
		Curve sbmlCurve;
		ListOf<CurveSegment> listOfCurveSegments;
		ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs;
		
		Reaction reaction;		
		
		if (reactionGlyph.isSetReaction()) {
			
			// create a process node from dimensions of the curve
			if (reactionGlyph.isSetCurve()) {
				sbmlCurve = reactionGlyph.getCurve();
				listOfCurveSegments = sbmlCurve.getListOfCurveSegments();
				
				processNode = createOneProcessNode(reactionGlyph, listOfCurveSegments);
				sbgnObject.getMap().getGlyph().add(processNode);	
			}
			
			listOfSpeciesReferenceGlyphs = reactionGlyph.getListOfSpeciesReferenceGlyphs();
			
			if (listOfSpeciesReferenceGlyphs.size() > 0) {
				
				for (SpeciesReferenceGlyph speciesReferenceGlyph : listOfSpeciesReferenceGlyphs){
					printHelper("createGlyphFromReactionGlyph", 
							String.format("speciesGlyph = %s, speciesReference = %s \n", 
							speciesReferenceGlyph.getSpeciesGlyph(), speciesReferenceGlyph.getSpeciesReference()));
					
					sbmlCurve = speciesReferenceGlyph.getCurve();
					listOfCurveSegments = sbmlCurve.getListOfCurveSegments();
					for (CurveSegment curveSegment : listOfCurveSegments) {
						arc = createOneArc(curveSegment);
						
						// set Clazz of the Arc
						// todo: need to determine from getOutputFromClass between production/consumption etc
						String clazz = searchForReactionRole(speciesReferenceGlyph.getSpeciesReferenceRole());
						arc.setClazz(clazz);
						
						// store the created Arc into SBGN
						// todo: need source/target glyphs without violating syntax? i.e process nodes always connect arcs?
						// need bezier (can't do?)
						// need port (is it useful?)
						sbgnObject.getMap().getArc().add(arc);	
					}						
				}
			}	
			
			// store any additional information into SBGN
			reaction = (Reaction) reactionGlyph.getReactionInstance();
			if (reaction.getKineticLaw() != null && reactionGlyph.isSetCurve()) {

				String math = reaction.getKineticLaw().getMathMLString();
				addExtensionElement(processNode, math);
			}
		}
		// todo: create Auxiliary items?		
	}
	
	/**
	 * Create multiple SBGN <code>Label</code>, each corresponding to an SBML <code>TextGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<TextGlyph></code> listOfTextGlyphs
	 */			
	public void createLabelsFromTextGlyphs(Sbgn sbgnObject, ListOf<TextGlyph> listOfTextGlyphs) {
		Glyph sbgnGlyph;
		Label label;
		String id = null;
		String text;
		List<Glyph> listOfGlyphs;
		int indexOfSpeciesGlyph;
		
		for (TextGlyph textGlyph : listOfTextGlyphs){
			
			if (textGlyph.isSetText()) {
				text = textGlyph.getText();
				
			} else if (textGlyph.isSetOriginOfText()) {
				text = textGlyph.getOriginOfText();
			} else {
				text = "";
			}
			
			label = new Label();
			label.setText(text);
			
			if (textGlyph.isSetGraphicalObject()) {
				id = textGlyph.getGraphicalObjectInstance().getId();
				listOfGlyphs = sbgnObject.getMap().getGlyph();
				
				// find the Glyph that should contain this text
				indexOfSpeciesGlyph = searchForIndex(listOfGlyphs, id);
				sbgnGlyph = listOfGlyphs.get(indexOfSpeciesGlyph);
				sbgnGlyph.setLabel(label);
				
			} else {
				sbgnGlyph = new Glyph();
				sbgnGlyph.setId(textGlyph.getId());
				// todo: or class could be simple chemical etc.
				sbgnGlyph.setClazz("unspecified entity");
				
				sbgnObject.getMap().getGlyph().add(sbgnGlyph);		
				sbgnGlyph.setLabel(label);
				
				createVoidBBox(sbgnGlyph);				
			}
		}
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, corresponding to SBML <code>GeneralGlyph</code>. 
	 * Each <code>GraphicalObject</code> is casted to <code>GeneralGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<GraphicalObject></code> listOfAdditionalGraphicalObjects
	 */		
	public void createGlyphsFromGeneralGlyphs(Sbgn sbgnObject, ListOf<GraphicalObject> listOfAdditionalGraphicalObjects) {
		GeneralGlyph generalGlyph;
		BoundingBox bbox;
		
		// treat each GeneralGlyph like a ReactionGlyph
		for (GraphicalObject graphicalObject : listOfAdditionalGraphicalObjects) {
			bbox = graphicalObject.getBoundingBox();
			generalGlyph = (GeneralGlyph) graphicalObject;

			createGlyphFromGeneralGlyph(sbgnObject, generalGlyph);
		}
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code> from an SBML <code>GeneralGlyph</code>. 
	 * TODO: handle isSetCurve and isSetListOfSubGlyphs. Iterate the listOfSubGlyphs.
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<GraphicalObject></code> listOfAdditionalGraphicalObjects
	 */		
	public void createGlyphFromGeneralGlyph(Sbgn sbgnObject, GeneralGlyph generalGlyph){
		Curve sbmlCurve;
		ListOf<ReferenceGlyph> listOfReferenceGlyphs;
		ListOf<GraphicalObject> listOfSubGlyphs;	
		ListOf<CurveSegment> listOfCurveSegments;
		
		String referenceGlyphId;
		String glyph;
		String reference;
		String role;
		Arc arc;		
		
		if (generalGlyph.isSetCurve()){
			
		}
		if (generalGlyph.isSetListOfReferenceGlyphs()){
			listOfReferenceGlyphs = generalGlyph.getListOfReferenceGlyphs();
			
			for (ReferenceGlyph referenceGlyph : listOfReferenceGlyphs) {
				referenceGlyphId = referenceGlyph.getId();
				glyph = referenceGlyph.getGlyph();
				reference = referenceGlyph.getReference();
				role = referenceGlyph.getRole();
				
				printHelper("createGlyphsFromGeneralGlyphs", 
						String.format("id=%s, glyph=%s, reference=%s, role=%s \n", 
						referenceGlyphId, glyph, reference, role));
				
				sbmlCurve = referenceGlyph.getCurve();
				listOfCurveSegments = sbmlCurve.getListOfCurveSegments();
				// todo: this is wrong, need to create one arc for each referenceGlyph, not for each curveSegment
				for (CurveSegment curveSegment : listOfCurveSegments) {
					arc = createOneArc(curveSegment);
					// todo: need to determine from getOutputFromClass between production/consumption etc
					String clazz = searchForReactionRole(role);
					arc.setClazz(clazz);
					
					// todo: need source/target glyphs without violating syntax? i.e process nodes always connect arcs?
					// need bezier (can't do?)
					// need port (is it useful?)
					sbgnObject.getMap().getArc().add(arc);						
				}
			}
		}
		if (generalGlyph.isSetListOfSubGlyphs()){

		}		
	}
	
	/**
	 * Create a Process Node <code>Glyph</code> using information from an SBML <code>ReactionGlyph</code>.
	 * The dimensions of the Process Node is determined from <code>ListOf<CurveSegment</code> of the <code>ReactionGlyph</code>.
	 * TODO: need an alternative approach for a better visual representation of the Glyph.
	 * 
	 * @param <code>Glyph</code> reactionGlyph
	 * @param <code>ListOf<CurveSegment><Glyph></code> listOfCurveSegments
	 * @return the created Process Node <code>Glyph</code>
	 */			
	public Glyph createOneProcessNode(ReactionGlyph reactionGlyph, ListOf<CurveSegment> listOfCurveSegments) {
		Glyph processNode;
		Arc arc;
		Arc.Start start;
		Arc.End end;
		
		double minCurveXCoord = 0;
		double maxCurveXCoord = 0;
		double minCurveYCoord = 0;
		double maxCurveYCoord = 0;
		
		double minXCoord;
		double maxXCoord;
		double minYCoord;
		double maxYCoord;		
		
		double startX;
		double startY;
		double endX;
		double endY;
		
		int index = 0;
		
		for (CurveSegment curveSegment : listOfCurveSegments) {
			arc = createOneArc(curveSegment);
			
			start = arc.getStart();
			end = arc.getEnd();
			
			startX = start.getX();
			startY = start.getY();
			endX = end.getX();
			endY = end.getY();
			
			minXCoord = Math.min(startX, endX);
			maxXCoord = Math.max(startX, endX);
			minYCoord = Math.min(startY, endY);
			maxYCoord = Math.max(startY, endY);		
			
			if (index == 0){
				minCurveXCoord = minXCoord;
				maxCurveXCoord = maxXCoord;
				minCurveYCoord = minYCoord;
				maxCurveYCoord = maxYCoord;
			} else {
				minCurveXCoord = Math.min(minXCoord, minCurveXCoord);
				maxCurveXCoord = Math.max(maxXCoord, maxCurveXCoord);
				minCurveYCoord = Math.min(minYCoord, minCurveYCoord);
				maxCurveYCoord = Math.max(maxYCoord, maxCurveYCoord);
			}
			index ++;
		}

		printHelper("createOneProcessNode", String.format("dimensions = (%s, %s), (%s, %s) \n", 
				Double.toString(minCurveXCoord),
				Double.toString(minCurveYCoord),
				Double.toString(maxCurveXCoord),
				Double.toString(maxCurveYCoord)));
		
		processNode = new Glyph();
		processNode.setId(reactionGlyph.getId());
		processNode.setClazz("process");
		createVoidBBox(processNode);
		setBBoxDimensions(processNode, (float) minCurveXCoord, (float) maxCurveXCoord, (float) minCurveYCoord, (float) maxCurveYCoord);
		return processNode;
	}
	
	/**
	 * Create the <code>Bbox</code> and set its dimensions for an SBGN <code>Glyph</code>.
	 * This <code>Bbox</code> is to have the same dimensions as an SBML <code>GraphicalObject</code>.
	 * 
	 * @param <code>Glyph</code> sbmlGlyph
	 * @param <code>Glyph</code> sbgnGlyph
	 */		
	public void createBBox(GraphicalObject sbmlGlyph, Glyph sbgnGlyph) {
		Bbox bbox = new Bbox();
		
		BoundingBox boundingBox = sbmlGlyph.getBoundingBox();
		Dimensions dimensions = boundingBox.getDimensions();
		Point position = boundingBox.getPosition();

		// note: can't set depth
		double depth = dimensions.getDepth();
		double height = dimensions.getHeight();
		double width = dimensions.getWidth();	
		
		// one of the values will be lost
		double x = position.getX();
		double y = position.getY();
		double z = position.getZ();
		
		bbox.setX((float) x);
		bbox.setY((float) y);
		bbox.setH((float) height);
		bbox.setW((float) width);
		
		sbgnGlyph.setBbox(bbox);
	}
	
	public void createVoidBBox(Glyph g) {

		Bbox bbox = new Bbox();
		bbox.setX(0);
		bbox.setY(0);
		bbox.setH(0);
		bbox.setW(0);
		g.setBbox(bbox);
	}	
	
	/**
	 * Set the dimensions of the <code>Bbox</code> for an SBGN <code>Glyph</code>.
	 * 
	 * @param <code>Glyph</code> glyph
	 * @param <code>float</code> maxCurveXCoord
	 * @param <code>float</code> maxCurveXCoord
	 * @param <code>float</code> minCurveYCoord
	 * @param <code>float</code> maxCurveYCoord
	 */		
	public void setBBoxDimensions(Glyph glyph, 
			float minCurveXCoord, float maxCurveXCoord, float minCurveYCoord, float maxCurveYCoord) {
		// assume bbox has already been set
		Bbox bbox = glyph.getBbox();
				
		bbox.setX(minCurveXCoord);
		bbox.setY(minCurveYCoord);
		// in case H and W is 0, we need +1
		if (maxCurveYCoord - minCurveYCoord == 0.0 || maxCurveXCoord - minCurveXCoord == 0.0){
			bbox.setH(maxCurveYCoord - minCurveYCoord + 1);
			bbox.setW(maxCurveXCoord - minCurveXCoord + 1);			
		} else {
			bbox.setH(maxCurveYCoord - minCurveYCoord);
			bbox.setW(maxCurveXCoord - minCurveXCoord);
		}

	}
	
	/**
	 * Create an SBGN <code>Arc</code> that corresponds to an SBML <code>CurveSegment</code>.
	 * TODO: create one Arc from multiple CurveSegment.
	 * 
	 * @param <code>CurveSegment</code> sbmlGlyph
	 * @return the created <code>Arc</code>
	 */		
	public Arc createOneArc(CurveSegment sbmlGlyph) {
		Arc arc = new Arc();
		
		Arc.Start start;
		Arc.End end;
		
		start = new Arc.Start();
		end = new Arc.End();
			
		// note z will be lost
		start.setX((float) sbmlGlyph.getStart().getX());
		start.setY((float) sbmlGlyph.getStart().getY());
		end.setX((float) sbmlGlyph.getEnd().getX());
		end.setY((float) sbmlGlyph.getEnd().getY());
		
		arc.setStart(start);
		arc.setEnd(end);
		
		printHelper("createOneArc", String.format("dimensions = (%s, %s), (%s, %s) \n", 
				Double.toString(start.getX()),
				Double.toString(start.getY()),
				Double.toString(end.getX()),
				Double.toString(end.getY())));		
		
		return arc;
	}
	
	
	/**
	 * Search for a <code>Glyph</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>List<Glyph></code> listOfGlyphs
	 * @param <code>String</code> id
	 * @return the index of the <code>Glyph</code> in listOfGlyphs
	 */	
	public int searchForIndex(List<Glyph> listOfGlyphs, String id) {
		Glyph glyph;
		String glyphId;
		
		for (int i = 0; i < listOfGlyphs.size(); i++) {
			glyph = listOfGlyphs.get(i);
			glyphId = glyph.getId();
			
			if (id.equals(glyphId)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public String searchForReactionRole(SpeciesReferenceRole speciesReferenceRole) {
		String sbgnClazz = null;
		int sbo;
		
		sbo = speciesReferenceRole.toSBOterm();
		// substrate
		if (sbo == 10) {
			sbgnClazz = "consumption";
		}
		// product
		if (sbo == 11) {
			sbgnClazz = "production";
		} 
		// sidesubstrate
		if (sbo == 603) {
			sbgnClazz = "consumption";
		} 
		// sideproduct
		if (sbo == 604) {
			sbgnClazz = "production";
		} 
		// activator
		if (sbo == 459) {
			sbgnClazz = "catalysis";
		} 
		// inhibitor
		if (sbo == 20) {
			sbgnClazz = "inhibition";
		}
		
		return sbgnClazz;
	}
	
	public String searchForReactionRole(String speciesReferenceRole) {
		String sbgnClazz = null;

		if (speciesReferenceRole.equals("substrate")) {
			sbgnClazz = "consumption";
		} if (speciesReferenceRole.equals("product")) {
			sbgnClazz = "production";
		} if (speciesReferenceRole.equals("sidesubstrate")) {
			sbgnClazz = "consumption";
		} if (speciesReferenceRole.equals("sideproduct")) {
			sbgnClazz = "production";
		} if (speciesReferenceRole.equals("activator")) {
			sbgnClazz = "catalysis";
		} if (speciesReferenceRole.equals("inhibitor")) {
			sbgnClazz = "inhibition";
		} if (speciesReferenceRole.equals("modifier")) {
			sbgnClazz = "modulation";	// not sure
		} if (speciesReferenceRole.equals("undefined")) {
			sbgnClazz = "unknown influence";
		}
		
		return sbgnClazz;		
	}
	
	public SBMLDocument getSBMLDocument(String sbmlFileName) {

		SBMLDocument document = null;
		SBMLReader reader = new SBMLReader();
		try {
			document = reader.readSBML(sbmlFileName);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		return document;
	}	
	
	public void printHelper(String source, String message){
		if (debugMode == 1){
			System.out.println("[" + source + "] " + message);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException {
		
		BasicConfigurator.configure();
		String sbmlFileNameInput;
		String sbgnFileNameOutput;
		SBMLDocument sbmlDocument;
		SBML2SBGNML_GSOC2017 sbml2sbgnml = new SBML2SBGNML_GSOC2017();
		Sbgn sbgnObject;
		File file;
		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbml2sbgnml.SBML2SBGNML_GSOC2017 <SBML filename>. "
					+ "An example of relative path: /examples/sbml_layout_examples/GeneralGlyph_Example.xml");
		}

		String workingDirectory = System.getProperty("user.dir");

		sbmlFileNameInput = args[0];
		sbmlFileNameInput = workingDirectory + sbmlFileNameInput;	
		
		sbmlDocument = sbml2sbgnml.getSBMLDocument(sbmlFileNameInput);
		
		if (sbmlDocument == null) {
			throw new FileNotFoundException("The SBMLDocument is null");
		}
		
		sbgnFileNameOutput = sbmlFileNameInput.replaceAll(".xml", "_SBGN-ML.sbgn");
		
		// visualize JTree
		try {		
			sbml2sbgnml.visualizeJTree(sbmlDocument);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		sbgnObject = sbml2sbgnml.convertToSBGNML(sbmlDocument);	
		
		file = new File(sbgnFileNameOutput);
		try {
			SbgnUtil.writeToFile(sbgnObject, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		sbml2sbgnml.printHelper("sbml2sbgnml.SBML2SBGNML_GSOC2017.main", "output file at: " + sbgnFileNameOutput);

	}	
	
	public class JSBMLvisualizer extends JFrame {
		public JSBMLvisualizer(SBase tree) {
			super("SBML Structure Visualization");
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			getContentPane().add(new JScrollPane(new JTree(tree)));
			pack();
			setAlwaysOnTop(true);
			setLocationRelativeTo(null);
			setVisible(true);			
		}
	}	
	
	public void visualizeJTree(SBMLDocument sbmlDocument) throws 
	ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		// hierarchy displayed by the Java JTree object
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new JSBMLvisualizer(sbmlDocument);		
		return;
	}	
	
	/**
	 * Add an {@link Extension} tag for an {@link SBGNBase} object with a {@link Element} inside. If the {@link Extension} is alredy present 
	 * for the object, a new one is created, the {@link Element} is simply added otherwise.
	 * The String will have to be in a xml structure compliant.
	 * 
	 * @param {@link SBGNbase} base
	 * @param {@link String} elementString
	 */
	private static void addExtensionElement(SBGNBase base, String elementString) {
		
		// ... and add it as extension for the SBGN-ML glyph
		Extension ex = new Extension();

		// if the Extension exists alredy
		if ( base.getExtension() != null ) {
			ex = base.getExtension();
		}
		
		// source: http://www.java2s.com/Code/JavaAPI/org.w3c.dom/DocumentgetDocumentElement.htm
		// prepare a builder factory, details have to be set
	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    builderFactory.setNamespaceAware(false);       // Set namespace aware
	    builderFactory.setValidating(false);           // and validating parser features
	    builderFactory.setIgnoringElementContentWhitespace(false); 
		
	    DocumentBuilder builder = null;

		try {
			builder = builderFactory.newDocumentBuilder();  // Create the parser
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}
		Document xmlDoc = null;
		
		try {
			xmlDoc = builder.parse(new InputSource(new StringReader(elementString)));

		} catch(SAXException exception) {
			exception.printStackTrace();

		} catch(IOException exception) {
			exception.printStackTrace();
		}
		
		// finally we have our dom element
		Element e = xmlDoc.getDocumentElement();
		
		// fill the list<Element> of Extension with our dom element
		ex.getAny().add(e);

		// set the Extension for the SBGNBase
		base.setExtension(ex);
		
	}
	
	private static void addAnnotationInExtension() {}
	
	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {

		try {
			inputModel = model;
			SBMLDocument sbmlDoc = ((SBMLModel) model).getSBMLDocument();
			Sbgn sbgnObj = convertToSBGNML(sbmlDoc);
			SBGNModel outputModel = new SBGNModel(sbgnObj);
			return outputModel;
		} catch (SBMLException e) {
			e.printStackTrace();
			throw new ConversionException(e.getMessage());
		}
	}

	@Override
	public String getResultExtension() {
		return ".sbgn";
	}
	
	@Override
	public String getName() {
		return "SBML2SBGNML";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}
}
