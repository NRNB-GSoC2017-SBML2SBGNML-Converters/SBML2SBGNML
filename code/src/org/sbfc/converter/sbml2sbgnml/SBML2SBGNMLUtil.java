package org.sbfc.converter.sbml2sbgnml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.SBGNBase;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SBML2SBGNMLUtil {

	int debugMode = 0;
	private static SBGNUtils sbu = new SBGNUtils("sbgnml");
	
	/**
	 * Create a new <code>Glyph</code> of class <code>clazz</code>.
	 * The dimensions of the <code>Glyph</code> is copied from the input
	 * SBML <code>GraphicalObject</code>. 
	 * 
	 */		
	public Glyph createGlyph(String id, String clazz, boolean setBbox, GraphicalObject sbmlGlyph,
			boolean setText, String text) {
		Glyph glyph;
		
		
		glyph = new Glyph();
		glyph.setId(id);
		glyph.setClazz(clazz);		
		
		if (setBbox){
			createBBox(sbmlGlyph, glyph);
		}
		
		if (setText){
			setLabel(glyph, text);
		}
		
		return glyph;
	}
	
	public Label setLabel(Glyph glyph, String text) {
		Label label;
		
		label = new Label();
		label.setText(text);	
		glyph.setLabel(label);	
		
		return label;
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
	public Glyph createOneProcessNode(ReactionGlyph reactionGlyph, Curve sbmlCurve) {
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
		ListOf<CurveSegment> listOfCurveSegments;
		
		listOfCurveSegments = sbmlCurve.getListOfCurveSegments();
		
		// this is a temporary trick to find the start and end points, to be replaced later.
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
		
		processNode = createGlyph(reactionGlyph.getId(), "process", false, null, false, null);
		createVoidBBox(processNode);
		setBBoxDimensions(processNode, 
				(float) minCurveXCoord, 
				(float) maxCurveXCoord, 
				(float) minCurveYCoord, 
				(float) maxCurveYCoord);
		return processNode;
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
	
	public Arc createOneArc(Curve curve){
		Arc arc = new Arc();
		
		Arc.Start start;
		Arc.End end;
		List<Arc.Next> next;
		
		start = new Arc.Start();
		end = new Arc.End();
		next = new ArrayList<Arc.Next>();
		
		// todo
		for (CurveSegment curveSegment: curve.getListOfCurveSegments()){
			
		}
		
		// temporary trick, just get the first CurveSegment of the Curve
		CurveSegment curveSegment;
		curveSegment = curve.getCurveSegment(0);
		
		start.setX((float) curveSegment.getStart().getX());
		start.setY((float) curveSegment.getStart().getY());
		end.setX((float) curveSegment.getEnd().getX());
		end.setY((float) curveSegment.getEnd().getY());
		
		arc.setStart(start);
		arc.setEnd(end);
		
		return arc;
	}
	
	public Arcgroup createOneArcgroup(List<Glyph> listOfGlyphs, List<Arc> listOfArcs, String clazz) {
		Arcgroup arcgroup = new Arcgroup();
		
		arcgroup.setClazz(clazz);
		arcgroup.getArc().addAll(listOfArcs);
		arcgroup.getGlyph().addAll(listOfGlyphs);
		
		return arcgroup;
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
	
	public static SBMLDocument getSBMLDocument(String sbmlFileName) {

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
	public static void addExtensionElement(SBGNBase base, String elementString) {
		
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
		
}
