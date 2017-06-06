package org.sbfc.converter.sbml2sbgnml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbgn.SbgnUtil;

public class SBGNML2SBML {
	HashMap<String, Glyph> processNodes = new HashMap<String, Glyph>();
	HashMap<String, Glyph> entityPoolNodes = new HashMap<String, Glyph>();
	HashMap<String, Arc> inwardArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> outwardArcs = new HashMap<String, Arc>();
	HashMap<String, String> glyphMap = new HashMap<String, String>(40);
	HashMap<String, String> arcMap = new HashMap<String, String>(16);
	Map map;
	Model model;
	Layout layout;
	
	public static void main(String[] args) throws FileNotFoundException {
		String sbgnFileNameInput;
		String sbmlFileNameOutput;
		String workingDirectory;
		File inputFile;
		File outputFile;
		Sbgn sbgnObject = null;
		Map map;
		SBGNML2SBML converter;
		SBMLDocument sbmlDocument;
		SBMLWriter sbmlWriter;
		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbml2sbgnml.SBGNML2SBML <SBGNML filename>. "
					+ "filename example: /examples/sbml_layout_examples/GeneralGlyph_Example.xml");
			return;
		}		
		
		workingDirectory = System.getProperty("user.dir");
		sbgnFileNameInput = args[0];
		sbgnFileNameInput = workingDirectory + "\\" + sbgnFileNameInput;			
		sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
		
		inputFile = new File(sbgnFileNameInput);
		outputFile = new File(sbmlFileNameOutput);
		try {
			sbgnObject = SbgnUtil.readFromFile(inputFile);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		map = sbgnObject.getMap();	
		debugSbgnObject(map);
		
		converter = new SBGNML2SBML(map);
		converter.convertToSBML();
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(converter.model);
		Dimensions dimensions = new Dimensions(400, 200, 0, 3, 1);
		converter.layout.setDimensions(dimensions);
		sbmlWriter = new SBMLWriter();
		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SBGNML2SBML(Map map) {
		this.map = map;
		this.model = new Model(3, 1);
		LayoutModelPlugin plugin = (LayoutModelPlugin) model.getPlugin("layout");
		this.layout = plugin.createLayout();
	}
		
	public Boolean isProcessNode(String clazz) {
		if (clazz.equals("process")) {
			return true;
		} else if (clazz.equals("omitted process")) {
			return true;
		} else if (clazz.equals("uncertain process")) {
			return true;
		} else if (clazz.equals("association")) {
			return true;
		} else if (clazz.equals("dissociation")) {
			return true;
		} else if (clazz.equals("phenotype")) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isInwardArc(String clazz) {
		if (clazz.equals("consumption")) {
			return true;
		} else {
			return false;
		} 
	}
	
	public Boolean isOutwardArc(String clazz) {
		if (clazz.equals("production")) {
			return true;
		} else {
			return false;
		} 		
	}
	
	public void convertToSBML() {
		List<Glyph> listOfGlyphs = map.getGlyph();
		List<Arc> listOfArcs = map.getArc();
		String id;
		String clazz; 
		
		for (Glyph glyph: listOfGlyphs) {
			id = glyph.getId();
			clazz = glyph.getClazz();
			System.out.format("glyph clazz=%s \n", clazz);
			
			if (isProcessNode(clazz)) {
				processNodes.put(id, glyph);
				System.out.format("processNodes size=%d \n", processNodes.size());
			} else {
				entityPoolNodes.put(id, glyph);
			}
		}
		
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			clazz = arc.getClazz();
			
			if (isInwardArc(clazz)) {
				inwardArcs.put(id, arc);
			} else if (isOutwardArc(clazz)) {
				outwardArcs.put(id, arc);
			}			
		}		
		
		createSpecies();
		createReactions();
	}
	
	public void createSpecies() {
		ListOf<Species> listOfSpecies = model.getListOfSpecies();
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();	
		
		Species species;
		SpeciesGlyph speciesGlyph;
		Glyph glyph;
		String id;
		String name = "";
		String clazz; 
		Annotation annotation;
		CVTerm cvTerm;
		BoundingBox boundingBox;
		Bbox bbox;
		
		for (String key : entityPoolNodes.keySet()) {
			glyph = entityPoolNodes.get(key);
			if (glyph.getLabel() != null) {
				name = glyph.getLabel().getText();
			}
			clazz = glyph.getClazz();
			id = key;
			
			species = new Species(id, name, 3, 1);
			annotation = species.getAnnotation();
			cvTerm = new CVTerm(Type.BIOLOGICAL_QUALIFIER, Qualifier.BQB_IS_VERSION_OF);
			// should be urn
			cvTerm.addResource(clazz);
			annotation.addCVTerm(cvTerm);
			listOfSpecies.add(species);
			
			speciesGlyph = new SpeciesGlyph();
			speciesGlyph.setId(id+"_Glyph");
			bbox = glyph.getBbox();
			boundingBox = new BoundingBox();
			// horizontal?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			speciesGlyph.setBoundingBox(boundingBox);
			listOfSpeciesGlyphs.add(speciesGlyph);
		}
	}
	
	public void createReactions() {
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		ListOf<ReactionGlyph> listOfReactionGlyphs = layout.getListOfReactionGlyphs();
		
		Reaction reaction;
		ReactionGlyph reactionGlyph;
		Arc arc;
		Glyph glyph;
		String id;
		String reactionId;
		Curve curve;
		CurveSegment curveSegment;
		Point point;
		Bbox bbox;
		
		Object source;
		Object target;
		Glyph sourceGlyph;
		Port targetPort;
		Port sourcePort;
		Glyph targetGlyph;	
		
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		Arc.Start start;
		Arc.End end;
		
		for (String key: processNodes.keySet()) {
			glyph = processNodes.get(key);
			id = glyph.getId();
			reaction = new Reaction();
			
			reaction.setId(id);
			listOfReactions.add(reaction);
			
			reactionGlyph = new ReactionGlyph();
			reactionGlyph.setId(id+"_Glyph");
			reactionGlyph.setReaction(reaction);
						
			curve = new Curve();
			curveSegment = new LineSegment();
			bbox = glyph.getBbox();
			point = new Point(bbox.getX(), bbox.getY());
			curveSegment.setStart(point);
			point = new Point(bbox.getX()+bbox.getW(), bbox.getY()+bbox.getH());
			curveSegment.setEnd(point);
			curve.addCurveSegment(curveSegment);
			reactionGlyph.setCurve(curve);
			listOfReactionGlyphs.add(reactionGlyph);
			
			System.out.format("listOfReactions size=%s \n", listOfReactions.size());
			System.out.format("listOfReactionGlyphs size=%s \n", layout.getListOfReactionGlyphs().size());
			
		}		
		for (String key: inwardArcs.keySet()) {
			arc = inwardArcs.get(key);
			id = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;
			
			speciesReference = new SpeciesReference();
			speciesReference.setId(id);
			species = findSpecies(model.getListOfSpecies(), sourceGlyph.getId());
			speciesReference.setSpecies(species);
			
			reactionId = targetPort.getId().substring(0, targetPort.getId().indexOf("."));
			System.out.format("reactionId=%s \n", reactionId);
			reaction = findReaction(model.getListOfReactions(), reactionId);
			reaction.addReactant(speciesReference);
			
			speciesReferenceGlyph = new SpeciesReferenceGlyph();
			speciesReferenceGlyph.setId(id+"_Glyph");
			speciesReferenceGlyph.setRole(searchForReactionRole(arc.getClazz()));
			speciesReferenceGlyph.setSpeciesGlyph(sourceGlyph.getId()+"_Glyph");
			speciesReferenceGlyph.setSpeciesReference(speciesReference);
			
			start = arc.getStart();
			//next = arc.getNext();
			end = arc.getEnd();
			curve = new Curve();
			curveSegment = new LineSegment();
			point = new Point(start.getX(), start.getY());
			curveSegment.setStart(point);
			point = new Point(end.getX(), end.getY());
			curveSegment.setEnd(point);			
			curve.addCurveSegment(curveSegment);
			
			speciesReferenceGlyph.setCurve(curve);
			reactionGlyph = findReactionGlyph(layout.getListOfReactionGlyphs(), reactionId+"_Glyph");
			reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);
			
		}	
		for (String key: outwardArcs.keySet()) {
			
		}
	}
	
	public Species findSpecies(ListOf<Species> listOfSpecies, String id) {
		for (Species species : listOfSpecies) {
			if (species.getId().equals(id)) {
				return species;
			}
		}
		return null;
	}
	
	public SpeciesGlyph findSpeciesGlyph(ListOf<SpeciesGlyph> listOfSpeciesGlyph, String id) {
		for (SpeciesGlyph speciesGlyph : listOfSpeciesGlyph) {
			if (speciesGlyph.getId().equals(id)) {
				return speciesGlyph;
			}
		}
		return null;
	}	
	
	public Reaction findReaction(ListOf<Reaction> listOfReactions, String id) {
		for (Reaction reaction : listOfReactions) {
			if (reaction.getId().equals(id)) {
				System.out.format("findReaction reaction=%s \n", reaction.getId());
				return reaction;
			}
		}
		return null;		
	}
	
	public ReactionGlyph findReactionGlyph(ListOf<ReactionGlyph> listOfReactionGlyph, String id) {
		for (ReactionGlyph reactionGlyph : listOfReactionGlyph) {
			if (reactionGlyph.getId().equals(id)) {
				return reactionGlyph;
			}
		}
		return null;
	}		
	
	public SpeciesReferenceRole searchForReactionRole(String clazz) {
		SpeciesReferenceRole role = null;

		if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SUBSTRATE;
		} if (clazz.equals("production")) {
			role = SpeciesReferenceRole.PRODUCT;
		} if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SIDESUBSTRATE;
		} if (clazz.equals("production")) {
			role = SpeciesReferenceRole.SIDEPRODUCT;
		} if (clazz.equals("catalysis")) {
			role = SpeciesReferenceRole.ACTIVATOR;
		} if (clazz.equals("inhibition")) {
			role = SpeciesReferenceRole.INHIBITOR;
		} if (clazz.equals("modulation")) {
			role = SpeciesReferenceRole.MODIFIER;	// not sure
		} if (clazz.equals("unknown influence")) {
			role = SpeciesReferenceRole.UNDEFINED;
		}
		
		return role;		
	}
	
	public static void debugSbgnObject(Map map){
		
		List<Arc> listOfArcs = map.getArc();
		List<Glyph> listOfGlyphs = map.getGlyph();
		// ...
		List<Port> listOfPorts;
		List<Glyph> listOfContainingGlyphs;
		
		String id;
		Glyph.State state;
		Glyph.Clone clone;
		Glyph.Callout callout;
		Glyph.Entity entity;
		Label label;
		Bbox bbox;
		String clazz; 
		String orientation;
		Object compartmentRef;
		Float compartmentOrder;
		
		Arc.Start start;
		List<Arc.Next> next;
		Arc.End end;
		Object source;
		Object target;
		
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			listOfContainingGlyphs = arc.getGlyph();
			listOfPorts = arc.getPort();
			start = arc.getStart();
			next = arc.getNext();
			end = arc.getEnd();
			clazz = arc.getClazz();
			source = arc.getSource();
			target = arc.getTarget();
			
			System.out.format("arc id=%s clazz=%s start,end=%s \n"
					+ "next=%s source=%s target=%s \n "
					+ "source,target=%s \n \n",
					id, clazz, displayCoordinates(start, end),
					sizeOf(next, 3), isNull(source), isNull(target),
					displaySourceAndTarget(source, target));
		}
		for (Glyph glyph: listOfGlyphs) {
			id = glyph.getId();
			state = glyph.getState();
			clone = glyph.getClone();
			callout = glyph.getCallout();
			entity = glyph.getEntity();
			label = glyph.getLabel();
			bbox = glyph.getBbox();
			listOfContainingGlyphs = glyph.getGlyph();
			listOfPorts = glyph.getPort();
			clazz = glyph.getClazz();
			orientation = glyph.getOrientation();
			compartmentRef = glyph.getCompartmentRef();
			compartmentOrder = glyph.getCompartmentOrder();
			
			System.out.format("glyph id=%s clazz=%s label=%s bbox=%s \n"
					+ "state=%s clone=%s callout=%s entity=%s \n"
					+ "listOfContainingGlyphs=%s listOfPorts=%s \n"
					+ "orientation=%s compartmentRef=%s compartmentOrder=%s \n \n",
					id, clazz, isNull(label), displayCoordinates(bbox), 
					isNull(state), isNull(clone), isNull(callout), isNull(entity),  
					sizeOf(listOfContainingGlyphs, 0), sizeOf(listOfPorts, 2), 
					orientation, isNull(compartmentRef), isNull(compartmentOrder));
			
		}
	}
	
	public static String displaySourceAndTarget(Object source, Object target) {

		String type = "";
		if (source instanceof Glyph && target instanceof Port ) {
			type = "inward";
		}
		if (source instanceof Port && target instanceof Glyph ) {
			type = "outward";
		}		
		
		return String.format("%s %s %s", source.getClass(), target.getClass(), type);		
	}
	
	public static String displayCoordinates(Bbox bbox) {
		if (bbox == null) {
			return "[ ]";
		}
		String X = Float.toString(bbox.getX());
		String Y = Float.toString(bbox.getY());
		String W = Float.toString(bbox.getW());
		String H = Float.toString(bbox.getH());
			
		return String.format("[x=%s y=%s w=%s h=%s]", X, Y, W, H);
	}

	public static String displayCoordinates(Arc.Start start, Arc.End end) {
		List<org.sbgn.bindings.Point> listOfPoints;
		int numOfPoints;
		
		if (start == null || end == null) {
			return "[ ]";
		}
		String startX = Float.toString(start.getX());
		String startY = Float.toString(start.getY());
		listOfPoints = end.getPoint();
		numOfPoints = listOfPoints.size();		
		String endX = Float.toString(end.getX());
		String endY = Float.toString(end.getY());
		
		return String.format("[(%s,%s) %s (%s,%s)]", startX, startY, Integer.toString(numOfPoints), endX, endY);
	}
	
	public static String isNull(Object o) {
		if  (o == null) {
			return "NULL";
		}
		return "NOT NULL";
	}
	
	public static String sizeOf(Object list, int elementType) {		
		if (list != null) {
			if (elementType == 0) {
				List<Glyph> listOfGlyphs = (List<Glyph>) list;
				return Integer.toString(listOfGlyphs.size());
			} else if (elementType == 2) {
				List<Port> listOfPorts = (List<Port>) list;
				return Integer.toString(listOfPorts.size());
			} else if (elementType == 3) {
				List<Arc.Next> listOfNext = (List<Arc.Next>) list;
				int numOfPoints = 0;
				List<org.sbgn.bindings.Point> listOfPoints;
				for (Arc.Next next : listOfNext) {
					listOfPoints = next.getPoint();
					numOfPoints += listOfPoints.size();
				}
				return String.format("%s/%s", Integer.toString(listOfNext.size()), Integer.toString(numOfPoints));
			}			
		}
			
		return "0";
	}
}

