package org.sbfc.converter.sbml2sbgnml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.sbfc.converter.sbgnml2sbml.SBGNWrapperPoint;
import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.SBGNBase;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.CurveSegment.Type;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.Transition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * SBML2SBGNMLUtil contains methods that do not depend on any information in the Sbgn model. 
 * Example: finding a value from a given list.
 * @author haoran
 * @author jalowcki
 *
 */
public class SBML2SBGNMLUtil {
	// for writing Sbgn object to an .sbgn file
	public static SBGNUtils sbu = new SBGNUtils("sbgnml");
	
	// for adding an XML Element to the Sbgn object's Extension
	static final String SBFCANNO_PREFIX = "sbfcanno";
	static final String SBFC_ANNO_NAMESPACE = "http://www.sbfc.org/sbfcanno";
	static Document document = null;
	static DocumentBuilder builder = null;
	
	// for debugging
	static int debugMode = 0;

	SBML2SBGNMLUtil() {
		// the DocumentBuilder is used to add an XML Element to the Sbgn object's Extension
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder constructeur;
		try {
			constructeur = factory.newDocumentBuilder();
			document = constructeur.newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    builderFactory.setNamespaceAware(false);       // Set namespace aware
	    builderFactory.setValidating(false);           // and validating parser features
	    builderFactory.setIgnoringElementContentWhitespace(false);   
		try {
			builder = builderFactory.newDocumentBuilder();  // Create the parser
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * Create a new <code>Glyph</code> with its <code>clazz</code>.
	 * The dimensions of the <code>Glyph</code> is copied from the input
	 * SBML <code>GraphicalObject</code>. 
	 * 
	 */		
	public static Glyph createGlyph(String id, String clazz, boolean setBbox, GraphicalObject sbmlGlyph,
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
	
	/**
	 * Create a new Label for the glyph and set the Label's text to the provided text
	 * @param glyph
	 * @param text
	 * @return
	 */
	public static Label setLabel(Glyph glyph, String text) {
		Label label;
		
		label = new Label();
		label.setText(text);	
		glyph.setLabel(label);	
		
		return label;
	}
	
	/**
	 * Create a new Label for the glyph, set the Label's Bbox to the values provided in boundingBox,
	 * and set the Label's text to the provided text
	 * @param glyph
	 * @param text
	 * @param boundingBox
	 * @return
	 */
	public static Label setLabel(Glyph glyph, String text, BoundingBox boundingBox ) {
		Label label;
		
		label = new Label();
		label.setText(text);	
		glyph.setLabel(label);	
		
		if (boundingBox != null){
			Bbox bbox = new Bbox();
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
			
			label.setBbox(bbox);			
		}
		
		return label;
	}
	
	/**
	 * Create a Process Node <code>Glyph</code> using information from an SBML <code>ReactionGlyph</code>.
	 * The dimensions of the Process Node is determined from <code>ListOf<CurveSegment</code> of the <code>ReactionGlyph</code>.
	 * TODO: need an alternative approach for a better visual representation of the Glyph (using render Style).
	 * 
	 * @return the created Process Node <code>Glyph</code>
	 */			
	public static Arcgroup createOneProcessNode(String reactionId, Curve sbmlCurve, String clazz) {
		Glyph processNode;
		Arc arc;
		Arc.Start start;
		Arc.End end;
		ListOf<CurveSegment> listOfCurveSegments = null;
		Arcgroup ag = new Arcgroup();
		
		// create the center glyph in a reaction (note that this glyph is all we need to return in this function)
		processNode = createGlyph(reactionId, clazz, false, null, false, null);
		createVoidBBox(processNode);
		
		if (sbmlCurve != null){
			listOfCurveSegments = sbmlCurve.getListOfCurveSegments();
		}
		// Create a style for the processNode so that it looks like the style in SBML layout
		// i.e. we want to create the special kind of style where the "process node" is the endHead of the centre Curve
		// but most likely we won't need all this.
		// SKIP reading the following block of code if desired.
		if (sbmlCurve != null && listOfCurveSegments.size() > 0){			
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

			double yLastStart = listOfCurveSegments.get(listOfCurveSegments.size()-1).getStart().getY();
			double yLastEnd = listOfCurveSegments.get(listOfCurveSegments.size()-1).getEnd().getY();
			double xLastStart = listOfCurveSegments.get(listOfCurveSegments.size()-1).getStart().getX();
			double xLastEnd = listOfCurveSegments.get(listOfCurveSegments.size()-1).getEnd().getX();		
			
			if (maxCurveYCoord - minCurveYCoord > maxCurveXCoord - minCurveXCoord){
				// get 1/4 length of Y axis
				double yLength = (maxCurveYCoord - minCurveYCoord)/4;
							
				if (yLastEnd > yLastStart){
					setBBoxDimensions(processNode, 
							(float) (xLastEnd - yLength/2), 
							(float) (xLastEnd + yLength/2), 
							(float) (yLastEnd - yLength), 
							(float) yLastEnd);				
				} else {
					setBBoxDimensions(processNode, 
							(float) (xLastEnd - yLength/2), 
							(float) (xLastEnd + yLength/2), 
							(float) (yLastStart - yLength), 
							(float) yLastStart);				
				}
				
			} else {
				// get 1/4 length of X axis
				double xLength = (maxCurveXCoord - minCurveXCoord)/4;
				
				if (xLastEnd > xLastStart){
					setBBoxDimensions(processNode, 
							(float) (xLastEnd - xLength), 
							(float) xLastEnd, 
							(float) (yLastEnd - xLength/2), 
							(float) (yLastEnd + xLength/2));				
				} else {
					setBBoxDimensions(processNode, 
							(float) (xLastStart - xLength), 
							(float) xLastStart, 
							(float) (yLastEnd - xLength/2), 
							(float) (yLastEnd + xLength/2));			
				}			
			}
			
			// this is a trick to create something in SBGN that resembles an SBML ReactionGlyph
			Arc a = new Arc();
			a.setId(reactionId + "_Curve");
			a.setClazz("unknown influence");
			start = new Arc.Start();
			end = new Arc.End();
			start.setX((float) listOfCurveSegments.get(0).getStart().getX());
			start.setY((float) listOfCurveSegments.get(0).getStart().getY());
			end.setX((float) listOfCurveSegments.get(listOfCurveSegments.size()-1).getEnd().getX());
			end.setY((float) listOfCurveSegments.get(listOfCurveSegments.size()-1).getEnd().getY());
			a.setEnd(end);
			a.setStart(start);
			
			ag.getArc().add(a);
		}
		
		
		ag.getGlyph().add(processNode);		
		
		return ag;

	}

	/**
	 * Create an SBGN <code>Arc</code> that corresponds to an SBML <code>CurveSegment</code>.
	 * 
	 * @param <code>CurveSegment</code> sbmlGlyph
	 * @return the created <code>Arc</code>
	 */		
	public static Arc createOneArc(CurveSegment sbmlGlyph) {
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
	 * Create an Sbgn Arc corresponding to an SBML layout Curve
	 * @param curve
	 * @return arc
	 */
	public static Arc createOneArc(Curve curve){
		Arc arc = new Arc();
		
		Arc.Start start;
		Arc.End end;
		List<Arc.Next> nextList;
		
		start = new Arc.Start();
		end = new Arc.End();
		nextList = new ArrayList<Arc.Next>();
		
		// create a HashMap as well as a ArrayList. 
		// The HashMap is used to check for duplicate Points (although not needed), the ArrayList used to store Points
		HashMap<String, Point> pointStringMap = new HashMap<String, Point>();
		List<Point> pointList = new ArrayList<Point>();
		// the key stored in the HashMap
		String pointString;
		
		// store all the Points in the pointList, so that we can use them to create arc's Start, Next, and End later
		for (CurveSegment curveSegment: curve.getListOfCurveSegments()){
			// the start Point
			pointString = Double.toString(curveSegment.getStart().getX()) + " " + 
							Double.toString(curveSegment.getStart().getY());
			if (!pointStringMap.containsKey(pointString)){
				pointStringMap.put(pointString, curveSegment.getStart());
				pointList.add(curveSegment.getStart());
			} else {
				pointList.add(curveSegment.getStart());
			}
			
			// the end Point
			pointString = Double.toString(curveSegment.getEnd().getX()) + " " + 
					Double.toString(curveSegment.getEnd().getY());
			if (curveSegment.getType() == Type.CUBIC_BEZIER) {
				if (!pointStringMap.containsKey(pointString)){
					SBMLWrapperPoint bezier = new SBMLWrapperPoint(curveSegment.getEnd(), ((CubicBezier) curveSegment).getBasePoint1(), ((CubicBezier) curveSegment).getBasePoint2() );
					pointList.add(bezier);
				}
			} else {
				if (!pointStringMap.containsKey(pointString)){
					pointStringMap.put(pointString, curveSegment.getEnd());
					pointList.add(curveSegment.getEnd());
				} else {
					pointList.add(curveSegment.getEnd());
				}					
			}
		}
		
		
		// Set the Start of the arc
		start.setX((float) pointList.get(0).getX());
		start.setY((float) pointList.get(0).getY());
		arc.setStart(start);
		
		// Set the Next of the arc
		// go over the list of Points we created, except for the first and last Point
		for (int i = 1; i < pointList.size() - 1; i++){
			Point p = pointList.get(i);			
			Arc.Next  e = new Arc.Next();
			
			if (p instanceof SBMLWrapperPoint){
				Point pNew = ((SBMLWrapperPoint) p).targetPoint;
				e.setX((float) pNew.getX());
				e.setY((float) pNew.getY());
				
				e.getPoint().add(((SBMLWrapperPoint) p).basePoint1);
				e.getPoint().add(((SBMLWrapperPoint) p).basePoint2);
			} else {
				e.setX((float) p.getX());
				e.setY((float) p.getY());
			}
			
			// add the Next to the nextList
			nextList.add(e);
		}
		arc.getNext().addAll(nextList);
		
		// Set the End of the arc
		Point endSbmlPoint = pointList.get(pointList.size() - 1);
		if (endSbmlPoint instanceof SBMLWrapperPoint){
			Point endSbmlPointNew = ((SBMLWrapperPoint) endSbmlPoint).targetPoint;
			end.setX((float) endSbmlPointNew.getX());
			end.setY((float) endSbmlPointNew.getY());	
			
			end.getPoint().add(((SBMLWrapperPoint) endSbmlPoint).basePoint1);
			end.getPoint().add(((SBMLWrapperPoint) endSbmlPoint).basePoint2);
		} else {
			end.setX((float) endSbmlPoint.getX());
			end.setY((float) endSbmlPoint.getY());			
		}
		arc.setEnd(end);
		
		
		return arc;
	}
	
	/**
	 * Create an Arcgroup with the given listOfGlyphs and listOfArcs
	 * @param listOfGlyphs
	 * @param listOfArcs
	 * @param clazz
	 * @return
	 */
	public static Arcgroup createOneArcgroup(List<Glyph> listOfGlyphs, List<Arc> listOfArcs, String clazz) {
		Arcgroup arcgroup = new Arcgroup();
		
		arcgroup.setClazz(clazz);
		arcgroup.getArc().addAll(listOfArcs);
		arcgroup.getGlyph().addAll(listOfGlyphs);
		
		return arcgroup;
	}
	
	/**
	 * Create the <code>Bbox</code> and set its dimensions.
	 * This <code>Bbox</code> will have the same dimensions as an SBML <code>GraphicalObject</code>.
	 * 
	 * @param <code>Glyph</code> sbmlGlyph
	 * @param <code>Glyph</code> sbgnGlyph
	 */		
	public static void createBBox(GraphicalObject sbmlGlyph, Glyph sbgnGlyph) {
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
		
	/**
	 * Create and set a Bbox for a glyph, the Bbox doesn't have valid values in its dimensions
	 * @param g
	 */
	public static void createVoidBBox(Glyph g) {

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
	public static void setBBoxDimensions(Glyph glyph, 
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
	 * Create and set Clone for the sbgnGlyph.
	 * @param sbgnGlyph
	 * @return 
	 */
	static void setClone(Glyph sbgnGlyph){
		org.sbgn.bindings.Glyph.Clone clone = null;

		clone = new Glyph.Clone();
		sbgnGlyph.setClone(clone);
		// TODO: add cloneText
		
	}
	
	/**
	 * A recursive function to find the glyph with the id, from the listOfGlyphs
	 * @param listOfGlyphs
	 * @param id
	 * @return glyph
	 */ 
	public static Glyph searchForGlyph(List<Glyph> listOfGlyphs, String id) {
		Glyph glyph;
		String glyphId;
		
		for (int i = 0; i < listOfGlyphs.size(); i++) {
			glyph = listOfGlyphs.get(i);
			glyphId = glyph.getId();
			
			if (id.equals(glyphId)) {
				return glyph;
			}
			
			if (glyph.getGlyph().size() > 0){
				glyph = searchForGlyph(glyph.getGlyph(), id);
				if (glyph == null){continue;}
				
				glyphId = glyph.getId();
				if (id.equals(glyphId)) {
					//System.out.println("searchForIndex glyphId" + glyphId);
					return glyph;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Search for a <code>Glyph</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>List<Glyph></code> listOfGlyphs
	 * @param <code>String</code> id
	 * @return the index of the <code>Glyph</code> in listOfGlyphs
	 */	
	public static int searchForIndex(List<Glyph> listOfGlyphs, String id) {
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
	
	/**
	 * Given the SBML speciesReferenceRole, find the corresponding Sbgn clazz.
	 * @param speciesReferenceRole
	 * @return
	 */
	public static String searchForReactionRole(SpeciesReferenceRole speciesReferenceRole) {
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
			sbgnClazz = "production";
		} 
		// sideproduct
		if (sbo == 604) {
			sbgnClazz = "consumption";
		} 
		// activator
		if (sbo == 459) {
			sbgnClazz = "catalysis";
		} 
		// inhibitor
		if (sbo == 20) {
			sbgnClazz = "inhibition";
		} 
		// TODO: what about AF modulation arcs?
				
		return sbgnClazz;
	}
	
	/**
	 * Given the SBML speciesReferenceRole string, find the corresponding Sbgn clazz.
	 * @param speciesReferenceRole
	 * @return
	 */
	public static String searchForReactionRole(String speciesReferenceRole) {
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
			sbgnClazz = "stimulation";
		} if (speciesReferenceRole.equals("inhibitor")) {
			sbgnClazz = "inhibition";
		} if (speciesReferenceRole.equals("modifier")) {
			sbgnClazz = "modulation"; // there are many types of modulations
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
	
	/**
	 * For debugging
	 * @param source
	 * @param message
	 */
	public static void printHelper(String source, String message){
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
	 * Add an {@link Extension} tag for an {@link SBGNBase} object with a {@link Element} inside. If the {@link Extension} is not already present 
	 * for the object, a new one is created, the {@link Element} is simply added otherwise.
	 * The String will have to be XML structure compliant.
	 * @param base
	 * @param ud
	 */
	static void addSbaseInExtension(SBGNBase base, UnitDefinition ud) {
		Element extension = document.createElement(SBFCANNO_PREFIX + ":unitDefinition");
		extension.setAttribute("xmlns:" + SBFCANNO_PREFIX, SBFC_ANNO_NAMESPACE);
		extension.setAttribute(SBFCANNO_PREFIX + ":name", ud.getName());

		ListOf<Unit> listOfUnits = ud.getListOfUnits();
		for (Unit u : listOfUnits){
			Element attribute = document.createElement(SBFCANNO_PREFIX + ":kind" + u.getKind().toString());
			attribute.setAttribute(SBFCANNO_PREFIX + ":kind", u.getKind().toString());
			attribute.setAttribute(SBFCANNO_PREFIX + ":exponent", Double.toString(u.getExponent()));
			attribute.setAttribute(SBFCANNO_PREFIX + ":scale", Integer.toString(u.getScale()));
			attribute.setAttribute(SBFCANNO_PREFIX + ":multiplier", Double.toString(u.getMultiplier()));
			extension.appendChild(attribute);
		}
		
		// ... and add it as extension for the SBGN-ML glyph
		Extension ex = new Extension();

		// if the Extension exists already
		if ( base.getExtension() != null ) {
			ex = base.getExtension();
		}
		//System.out.println("got hereee");
		// fill the list<Element> of Extension with our dom element
		ex.getAny().add(extension);

		// set the Extension for the SBGNBase
		base.setExtension(ex);
		
	}
	
	/**
	 * Add an {@link Extension} tag for an {@link SBGNBase} object with a {@link Element} inside. If the {@link Extension} is not already present 
	 * for the object, a new one is created, the {@link Element} is simply added otherwise.
	 * The String will have to be XML structure compliant.
	 * @param base
	 * @param species
	 */
	static void addSpeciesInformationInExtension(SBGNBase base, Species species) {
		Element extension = document.createElement(SBFCANNO_PREFIX + ":species");
		extension.setAttribute("xmlns:" + SBFCANNO_PREFIX, SBFC_ANNO_NAMESPACE);
		extension.setAttribute(SBFCANNO_PREFIX + ":id", species.getId());
		
		if (species.getName() != null){
			extension.setAttribute(SBFCANNO_PREFIX + ":name", species.getName());
		}
		extension.setAttribute(SBFCANNO_PREFIX + ":compartment", species.getCompartment());
		if (species.getInitialAmount() != Double.NaN){
			extension.setAttribute(SBFCANNO_PREFIX + ":initialAmount",  Double.toString(species.getInitialAmount()));
		}
		if (species.getInitialConcentration() != Double.NaN){
			extension.setAttribute(SBFCANNO_PREFIX + ":initialConcentration",  Double.toString(species.getInitialConcentration()));
		}
		if (species.getSubstanceUnits() != null){
			extension.setAttribute(SBFCANNO_PREFIX + ":substanceUnits",  species.getSubstanceUnits());
		}
		extension.setAttribute(SBFCANNO_PREFIX + ":hasOnlySubstanceUnits",  Boolean.toString(species.getHasOnlySubstanceUnits()));
		extension.setAttribute(SBFCANNO_PREFIX + ":boundaryCondition",  Boolean.toString(species.getBoundaryCondition()));
		extension.setAttribute(SBFCANNO_PREFIX + ":constant",  Boolean.toString(species.getConstant()));
		if (species.getConversionFactor() != null){
			extension.setAttribute(SBFCANNO_PREFIX + ":conversionFactor",  species.getConversionFactor());
		}
		
		
		// ... and add it as extension for the SBGN-ML glyph
		Extension ex = new Extension();

		// if the Extension exists already
		if ( base.getExtension() != null ) {
			ex = base.getExtension();
		}
		//System.out.println("got hereee");
		// fill the list<Element> of Extension with our dom element
		ex.getAny().add(extension);

		// set the Extension for the SBGNBase
		base.setExtension(ex);		
	}
	
	static void addSboInExtension(SBGNBase base, String SBO){
		
	}
	
	/**
	 * Add an {@link Extension} tag for an {@link SBGNBase} object with a {@link Element} inside. If the {@link Extension} is not already present 
	 * for the object, a new one is created, the {@link Element} is simply added otherwise.
	 * The String will have to be XML structure compliant.
	 * @param base
	 * @param tr
	 * @param ft
	 */
	public static void addMathMLInExtension(Sbgn base, Transition tr, FunctionTerm ft) {
		String elementString = ft.getMathMLString();

		// we want to get rid of the "<?xml version='1.0' encoding='UTF-8'?>" before MathML
		String segments[] = elementString.split("math");
		try{ 
			elementString = "<math" + segments[1] + "math>";
		} catch(Exception e){
			return;
		}
		
		// ... and add it as extension for the SBGN-ML glyph
		Extension ex = new Extension();

		// if the Extension exists already
		if ( base.getExtension() != null ) {
			ex = base.getExtension();
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
		
		e.setAttribute(SBFCANNO_PREFIX + ":transition", tr.getId());
		e.setAttribute(SBFCANNO_PREFIX + ":functionTerm", Integer.toString(ft.getResultLevel()));
		e.setAttribute("xmlns:" + SBFCANNO_PREFIX, SBFC_ANNO_NAMESPACE);

		// fill the list<Element> of Extension with our dom element
		ex.getAny().add(e);

		// set the Extension for the SBGNBase
		base.setExtension(ex);		
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
	
	/**
	 * Create a mere XML schema for an annotation.
	 * 
	 * @param base
	 * @param anno
	 * @throws ParserConfigurationException 
	 */
	static void addAnnotationInExtension(SBGNBase base, Annotation anno) throws ParserConfigurationException {
		
		// new dom creation
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder constructeur = factory.newDocumentBuilder();
		Document document = constructeur.newDocument();

		// root annotation
		Element annotation = document.createElement(SBFCANNO_PREFIX + ":annotation");
		annotation.setAttribute("xmlns:" + SBFCANNO_PREFIX, SBFC_ANNO_NAMESPACE);
		
		// creating a child for each qualifier
		for (CVTerm cvt : anno.getListOfCVTerms()) {

			// get the name of the qualifier
			String cvtermName = cvt.isBiologicalQualifier() ? cvt.getBiologicalQualifierType().getElementNameEquivalent() :
				cvt.getModelQualifierType().getElementNameEquivalent();
			
			// add the CVTerm as a child
			Element cvterm = document.createElement(SBFCANNO_PREFIX + ":" + cvtermName);
			
			// append this element to the root
			annotation.appendChild(cvterm);
			
			// for each cvterm, add a resource tag corresponding to in which will be uri and url
			for (String urn : cvt.getResources()) {
				
				// create and add an element called resource
				Element resource = document.createElement(SBFCANNO_PREFIX + ":resource");
				resource.setAttribute(SBFCANNO_PREFIX + ":urn", urn);
				// TODO get the url of the corresponding urn if possible
				// resource.setAttribute(SBFCANNO_PREFIX + ":url", );
				cvterm.appendChild(resource);
				
			}
		}
		
		try {annotation.getFirstChild().toString(); }
		catch (NullPointerException e) { return; }
		
		// ... and add it as extension for the SBGN-ML glyph
		Extension ex = new Extension();

		// if the Extension exists alredy
		if ( base.getExtension() != null ) {
			ex = base.getExtension();
		}
		
		// fill the list<Element> of Extension with our dom element
		ex.getAny().add(annotation);

		// set the Extension for the SBGNBase
		base.setExtension(ex);
		
	}

	
}
