package org.sbfc.converter.sbgnml2sbml;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SBGNML2SBMLUtil {
	int debugMode;
	int level;
	int version;
	
	SBGNML2SBMLUtil(int level, int version) {
		this.level = level;
		this.version = version;
	}
	
	public Species createJsbmlSpecies(String speciesId, String name, String clazz, 
			boolean addAnnotation, boolean addSBO) {
		Species species;
		species = new Species(speciesId, name, level, version);
		
		if (addAnnotation){
			addAnnotation(species, clazz);
		}
		if (addSBO){
			addSBO(species, clazz);	
		}
		
		return species;
	}
	
	public SpeciesGlyph createJsbmlSpeciesGlyph(String speciesId, String name, String clazz, 
			Species species, boolean createBoundingBox, Bbox bbox) {
		SpeciesGlyph speciesGlyph;
		BoundingBox boundingBox;
		
		speciesGlyph = new SpeciesGlyph();
		speciesGlyph.setId("SpeciesGlyph_" + speciesId);
		speciesGlyph.setSpecies(species);
		
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// todo: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			speciesGlyph.setBoundingBox(boundingBox);				
		}
		return speciesGlyph;
	}
	
	public GeneralGlyph createJsbmlGeneralGlyph(Glyph glyph, boolean createBoundingBox, Bbox bbox) {
		GeneralGlyph generalGlyph;
		BoundingBox boundingBox;
		String id;
		
		id = glyph.getId();
		generalGlyph = new GeneralGlyph();
		generalGlyph.setId("GeneralGlyph_" + id);
		
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// todo: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			generalGlyph.setBoundingBox(boundingBox);					
		}

		return generalGlyph;
	}
	
	/**
	 * Create a TextGlyph using values from a <code>SpeciesGlyph</code> and its associated <code>Species</code>.
	 * 
	 * @param <code>Species</code> species
	 * @param <code>SpeciesGlyph</code> speciesGlyph
	 */		
	public TextGlyph createJsbmlTextGlyph(Species species, SpeciesGlyph speciesGlyph) {
		TextGlyph textGlyph;
		String id;
		BoundingBox boundingBoxText;
		BoundingBox boundingBoxSpecies;
		
		textGlyph = new TextGlyph(level, version);
		id = "TextGlyph_" + species.getId();
		textGlyph.setId(id);
		textGlyph.setOriginOfText(species);
		textGlyph.setGraphicalObject(speciesGlyph);
		
		boundingBoxText = new BoundingBox();
		boundingBoxSpecies = speciesGlyph.getBoundingBox();
		boundingBoxText.setDimensions(boundingBoxSpecies.getDimensions());
		boundingBoxText.setPosition(boundingBoxSpecies.getPosition());
		textGlyph.setBoundingBox(boundingBoxText);
				
		return textGlyph;
	}	
	
	
	public TextGlyph createJsbmlTextGlyph(GeneralGlyph generalGlyph, String text) {
		TextGlyph textGlyph;
		String id;
		BoundingBox boundingBoxText;
		BoundingBox boundingBoxGeneralGlyph;
		
		textGlyph = new TextGlyph(level, version);
		id = "TextGlyph_" + generalGlyph.getId();
		textGlyph.setId(id);
		textGlyph.setGraphicalObject(generalGlyph);
		textGlyph.setText(text);
		
		boundingBoxText = new BoundingBox();
		boundingBoxGeneralGlyph = generalGlyph.getBoundingBox();
		boundingBoxText.setDimensions(boundingBoxGeneralGlyph.getDimensions());
		boundingBoxText.setPosition(boundingBoxGeneralGlyph.getPosition());
		textGlyph.setBoundingBox(boundingBoxText);
				
		return textGlyph;
	}		
	
	
	public String getText(Glyph glyph){
		if (glyph.getLabel() != null) {
			return glyph.getLabel().getText();
		}
		return "";
	}
	
	public Reaction createJsbmlReaction(String reactionId) {
		Reaction reaction;
		
		reaction = new Reaction();
		reaction.setId(reactionId);
		
		return reaction;
	}
	
	public ReactionGlyph createJsbmlReactionGlyph(String reactionId, String name, String clazz, 
			Reaction reaction, boolean createBoundingBox, Bbox bbox) {
		ReactionGlyph reactionGlyph;
		BoundingBox boundingBox;
		
		reactionGlyph = new ReactionGlyph();
		reactionGlyph.setId("ReactionGlyph_" + reactionId);
		reactionGlyph.setReaction(reaction);
		
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// todo: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			reactionGlyph.setBoundingBox(boundingBox);				
		}
		return reactionGlyph;
	}	
	
	public Dimensions createDimensions(BoundingBox boundingBox, Bbox bbox){
		Dimensions dimension;
		
		boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
		
		return boundingBox.getDimensions();
	}
	public Point createPoint(BoundingBox boundingBox, Bbox bbox){
		Point point;
		
		boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);	
		
		return boundingBox.getPosition();
	}
	public void createBoundingBox(GraphicalObject graphicalObject, Glyph glyph){
		BoundingBox boundingBox = new BoundingBox();
		Bbox bbox = glyph.getBbox();
		
		graphicalObject.setBoundingBox(boundingBox);
		createDimensions(boundingBox, bbox);
		createPoint(boundingBox, bbox);
	}	
	
	/**
	 * Create a temporary <code>Curve</code> for the given <code>ReactionGlyph</code>. 
	 * This Curve has incorrect Start and End Points. 
	 * The values of the <code>Curve</code> will be modified later;
	 */	
	public void createReactionGlyphCurve(ReactionGlyph reactionGlyph, Glyph glyph) {
		Curve curve;
		CurveSegment curveSegment;
		Point point;
		Bbox bbox;
		
		curve = new Curve();
		curveSegment = new LineSegment();
		bbox = glyph.getBbox();
		point = new Point(bbox.getX(), bbox.getY());
		curveSegment.setStart(point);
		point = new Point(bbox.getX()+bbox.getW(), bbox.getY()+bbox.getH());
		curveSegment.setEnd(point);
		curve.addCurveSegment(curveSegment);
		reactionGlyph.setCurve(curve);		
	}
		
	public SpeciesReference createSpeciesReference(Reaction reaction, Species species, 
			String speciesReferenceId) {
		SpeciesReference speciesReference;
		
		speciesReference = new SpeciesReference();
		speciesReference.setId(speciesReferenceId);
		speciesReference.setSpecies(species);
	
		return speciesReference;
	}	
	
	/**
	 * Create a <code>SpeciesReferenceGlyph</code> using values from an SBGN <code>Arc</code>. 
	 * Associate the <code>SpeciesReferenceGlyph</code> with a <code>SpeciesGlyph</code> and 
	 * a <code>SpeciesReference</code>.
	 * 
	 * @param <code>String</code> id
	 * @param <code>Arc</code> arc
	 * @param <code>SpeciesReference</code> speciesReference
	 * @param <code>Glyph</code> speciesGlyph
	 * @return <code>SpeciesReferenceGlyph</code> speciesReferenceGlyph
	 */		
	public SpeciesReferenceGlyph createOneSpeciesReferenceGlyph(String id, Arc arc, 
			SpeciesReference speciesReference, Glyph speciesGlyph) {
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		speciesReferenceGlyph = new SpeciesReferenceGlyph();
		speciesReferenceGlyph.setId("SpeciesReferenceGlyph_"+id);
		speciesReferenceGlyph.setRole(findReactionRole(arc.getClazz()));
		speciesReferenceGlyph.setSpeciesGlyph("SpeciesGlyph_"+speciesGlyph.getId());
		speciesReferenceGlyph.setSpeciesReference(speciesReference);	
					
		return speciesReferenceGlyph;
	}
	
	/**
	 * Create an SBML <code>Curve</code> from values in an SBGN <code>Arc</code>. 
	 * TODO: need to handle more complex cases where an Arc consists of multiple parts
	 */		
	public Curve createOneSpeciesReferenceCurve(Arc arc) {
		Curve curve;
		CurveSegment curveSegment;
		Point point;
		
		Arc.Start start;
		Arc.End end;		
		
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
				
		return curve;
	}	
	
	public Compartment createJsbmlCompartment(String compartmentId, String name) {
		Compartment compartment = new Compartment(compartmentId, name, level, version);
		return compartment;
	}
	
	public CompartmentGlyph createJsbmlCompartmentGlyph(Glyph glyph, String compartmentId, Compartment compartment,
			boolean createBoundingBox) {
		CompartmentGlyph compartmentGlyph;
		Bbox bbox;
		BoundingBox boundingBox;
		
		compartmentGlyph = new CompartmentGlyph();
		compartmentGlyph.setId("CompartmentGlyph_" + compartmentId);
		compartmentGlyph.setCompartment(compartment);
		
		if (createBoundingBox){
			bbox = glyph.getBbox();
			boundingBox = new BoundingBox();
			// todo: horizontal?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			compartmentGlyph.setBoundingBox(boundingBox);			
		}
		return compartmentGlyph;
	}
	
	public void setCompartmentOrder(CompartmentGlyph compartmentGlyph, Glyph glyph) {
		float order = glyph.getCompartmentOrder();
		if ((Object) order != null){
			compartmentGlyph.setOrder(order);
		}
	}
	
	
	public ReferenceGlyph createOneReferenceGlyph(String id, Arc arc, ModifierSpeciesReference reference, 
			GraphicalObject object) {
		ReferenceGlyph referenceGlyph;
		
		referenceGlyph = new ReferenceGlyph();
		referenceGlyph.setId("ReferenceGlyph_" + id);
		referenceGlyph.setGlyph(object.getId());
		
		// does not work because we can't create a ModifierSpeciesReference. 
		// A ModifierSpeciesReference needs to associate with a Species, but we don't have a Species
		//referenceGlyph.setReference(reference.getId());

		return referenceGlyph;
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
	
	public Boolean isCompartment(String clazz) {
		if (clazz.equals("compartment")) {
			return true;
		}
		return false;
	}
	
	public Boolean isLogicOperator(String clazz) {
		if (clazz.equals("and")) {
			return true;
		} else if (clazz.equals("or")) {
			return true;
		} else if (clazz.equals("not")) {
			return true;
		}
		return false;
	}
	
	public Boolean isEntityPoolNode(String clazz){
		if (clazz.equals("unspecified entity")) {
			return true;
		} else if (clazz.equals("simple chemical")) {
			return true;
		} else if (clazz.equals("macromolecule")) {
			return true;
		} else if (clazz.equals("nucleic acid feature")) {
			return true;
		} else if (clazz.equals("complex multimer")) {
			return true;
		} else if (clazz.equals("complex")) {
			return true;
		} else if (clazz.equals("macromolecule multimer")) {
			return true;
		} else if (clazz.equals("nucleic acid feature multimer")) {
			return true;
		} else if (clazz.equals("source and sink")) {
			return true;
		} else if (clazz.equals("perturbing agent")) {
			return true;
		} else {
			return false;
		}		
	}
	
	public Boolean isUndirectedArc(Arc arc) {
		if (arc.getTarget() instanceof Glyph && arc.getSource() instanceof Glyph) {
			return true;
		}
		return false;
	}
	
	public Boolean isInwardArc(Arc arc) {
		if (arc.getTarget() instanceof Port && arc.getSource() instanceof Glyph) {
			return true;
		}
		return false;		
	}
	
	public Boolean isOutwardArc(Arc arc) {
		if (arc.getTarget() instanceof Glyph && arc.getSource() instanceof Port) {
			return true;
		}
		return false;		
	}
	
	// todo
	public Boolean isPortToPortArc(Arc arc) {
		if (arc.getTarget() instanceof Port && arc.getSource() instanceof Port) {
			return true;
		}		
		return false;
	}
	
	public Boolean isLogicArc(Arc arc) {
		String clazz = arc.getClazz();
		if (clazz.equals("logic arc")) {
			return true;
		}
		return false;
	}
	
	public Boolean isInwardArc(String clazz) {
		if (clazz.equals("consumption")) {
			return true;
		} if (clazz.equals("modulation")) {
			return true;
		} if (clazz.equals("stimulation")) {
			return true;
		} if (clazz.equals("catalysis")) {
			return true;
		} if (clazz.equals("inhibition")) {
			return true;
		} if (clazz.equals("necessary stimulation")) {
			return true;
		} // ...
		else {
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
	
		
	public SpeciesReferenceRole findReactionRole(String clazz) {
		SpeciesReferenceRole role = null;

		if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SUBSTRATE;
		} else if (clazz.equals("production")) {
			role = SpeciesReferenceRole.PRODUCT;
		} else if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SIDESUBSTRATE;
		} else if (clazz.equals("production")) {
			role = SpeciesReferenceRole.SIDEPRODUCT;
		} else if (clazz.equals("catalysis")) {
			role = SpeciesReferenceRole.ACTIVATOR;
		} else if (clazz.equals("inhibition")) {
			role = SpeciesReferenceRole.INHIBITOR;
		} else if (clazz.equals("modulation")) {
			role = SpeciesReferenceRole.MODIFIER;	// not sure
		} else if (clazz.equals("unknown influence")) {
			role = SpeciesReferenceRole.UNDEFINED;
		}
		
		return role;		
	}
	
	public void addAnnotation(Species species, String clazz) {
		Annotation annotation;
		CVTerm cvTerm;
		
		annotation = species.getAnnotation();
		cvTerm = new CVTerm(Type.BIOLOGICAL_QUALIFIER, Qualifier.BQB_IS_VERSION_OF);
		// should be urn
		// add hasPart
		cvTerm.addResource(clazz);
		annotation.addCVTerm(cvTerm);
	}
	
	public void addSBO(Species species, String clazz) {
		int sboTerm = -1;
		
		if (clazz.equals("simple chemical")) {
			sboTerm = 247;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("macromolecule")) {
			sboTerm = 245;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("nucleic acid feature")) {
			sboTerm = 354;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("complex multimer")) {
			sboTerm = 420;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("complex")) {
			sboTerm = 253;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("macromolecule multimer")) {
			sboTerm = 420;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("nucleic acid feature multimer")) {
			sboTerm = 420;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("source and sink")) {
			sboTerm = 291;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("perturbing agent")) {
			sboTerm = 405;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("unspecified entity")) {
			sboTerm = 285;
			species.setSBOTerm(sboTerm);			
		}// ...
		
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
		//Glyph.Callout callout;
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
		
		System.out.println("sbgnml2sbml.SBGNML2SBML_GSOC2017.debugSbgnObject: \n");
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
			//callout = glyph.getCallout();
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
					isNull(state), isNull(clone), "isNull(callout)", isNull(entity),  
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
		if (source instanceof Glyph && target instanceof Glyph ) {
			type = "undirected";
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
	
	public void printHelper(String source, String message){
		if (debugMode == 1){
			System.out.println("[" + source + "] " + message);
		}
	}	
	
	public void printHelper(String source, Integer message){
		if (debugMode == 1){
			System.out.println("[" + source + "] " + Integer.toString(message));
		}
	}	
	
	public static Sbgn readSbgnFile(String sbgnFileNameInput) {
		Sbgn sbgnObject = null;
		File inputFile;
		
		inputFile = new File(sbgnFileNameInput);
		try {
			sbgnObject = SbgnUtil.readFromFile(inputFile);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return sbgnObject;
	}
	
	public static void writeSbgnFile(String sbmlFileNameOutput, Model model) {
		File outputFile;
		outputFile = new File(sbmlFileNameOutput);
		
		SBMLWriter sbmlWriter;
		SBMLDocument sbmlDocument;
		
		sbmlWriter = new SBMLWriter();
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(model);
		
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
}
