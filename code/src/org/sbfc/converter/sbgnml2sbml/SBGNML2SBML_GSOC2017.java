package org.sbfc.converter.sbgnml2sbml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbgn.SbgnUtil;

public class SBGNML2SBML_GSOC2017  extends GeneralConverter{
	HashMap<String, Glyph> processNodes = new HashMap<String, Glyph>();
	HashMap<String, Glyph> entityPoolNodes = new HashMap<String, Glyph>();
	HashMap<String, Glyph> compartments = new HashMap<String, Glyph>();
	HashMap<String, Glyph> logicOperators = new HashMap<String, Glyph>();
	
	HashMap<String, String> portToGlyphMap = new HashMap<String, String>();
				
	// see code below for definition of inwardArcs, outwardArcs, and undirectedArcs
	HashMap<String, Arc> inwardArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> outwardArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> undirectedArcs = new HashMap<String, Arc>();
	
	Map map;
	Model model;
	Layout layout;
	
	// keep track of the maximum value for each dimension. Finally, set these 3 values as the dimensions of the layout
	Double dimensionX;
	Double dimensionY;
	Double dimensionZ;
	
	// keep track of how many Arcs are in Sbgn
	int numberOfSpeciesReferences;
	// keep track of how many Glyphs are in Sbgn
	int numberOfEntities;
	public int debugMode;
	
	SWrapperModel sWrapperModel;
	List<SWrapperReactionGlyph> listOfReactionGlyphTuples;
	List<SWrapperCompartmentGlyph> listOfCompartmentGlyphTuples;
	List<SWrapperSpeciesGlyph> listOfSpeciesGlyphTuples;
		
	public SBGNML2SBML_GSOC2017(Map map) {
		sWrapperModel = new SWrapperModel();
		listOfReactionGlyphTuples = sWrapperModel.getListOfWrapperReactionGlyphs();
		listOfCompartmentGlyphTuples = sWrapperModel.getListOfWrapperCompartmentGlyphs();
		listOfSpeciesGlyphTuples = sWrapperModel.getListOfWrapperSpeciesGlyphs();		
		
		this.map = map;
		this.model = sWrapperModel.getModel();
		LayoutModelPlugin plugin = (LayoutModelPlugin) model.getPlugin("layout");
		this.layout = plugin.createLayout();
		dimensionX = 0.0;
		dimensionY = 0.0;
		dimensionZ = 0.0;
		numberOfSpeciesReferences = 0;
		numberOfEntities = 0;
		
		debugMode = 0;
		
		List<Glyph> listOfGlyphs = map.getGlyph();
		List<Arc> listOfArcs = map.getArc();
		String id;
		String clazz; 		
		
		for (Glyph glyph: listOfGlyphs) {
			id = glyph.getId();
			if (id == null) {
				id = "Entities" + Integer.toString(this.numberOfEntities);
				glyph.setId(id);
			}				
			numberOfEntities++;
			clazz = glyph.getClazz();
			//System.out.format("glyph clazz=%s \n", clazz);
			
			updatePortToGlyphMap(glyph);
			
			if (isProcessNode(clazz)) {
				processNodes.put(id, glyph);
			} else if (isCompartment(clazz)) {
				compartments.put(id, glyph);
			} else if (isEntityPoolNode(clazz)) {
				entityPoolNodes.put(id, glyph);
			} else if (isLogicOperator(clazz)) {
				logicOperators.put(id, glyph);
			}
		}
		
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			if (id == null) {
				id = "SpeciesReferences" + Integer.toString(this.numberOfSpeciesReferences);
				arc.setId(id);
			}				
			this.numberOfSpeciesReferences++;			
			
			clazz = arc.getClazz();
			
			if (isUndirectedArc(arc)) {
				undirectedArcs.put(id, arc);
			} else if (isInwardArc(arc)) {
				inwardArcs.put(id, arc);
			} else if (isOutwardArc(arc)) {
				outwardArcs.put(id, arc);
			} 		
		}
		
	}

	/**
	 * Create an SBML <code>Model</code>, which corresponds to contents of <code>Map</code> of the <code>Sbgn</code>. 
	 * Each <code>Glyph</code> or <code>Arc</code> of the <code>Map</code> is mapped to elements of the <code>Model</code>.
	 */	
	public void convertToSBML() {
		createCompartments();
		createSpecies();
		//createReactions();
		createGeneralGlyphs();
		
		Dimensions dimensions = new Dimensions(this.dimensionX, this.dimensionY, this.dimensionZ, 3, 1);
		this.layout.setDimensions(dimensions);		
	}
	
	/**
	 * Create multiple SBML <code>SpeciesGlyph</code> and its associated <code>Species</code> from list of SBGN <code>Glyph</code>. 
	 * TODO: add more details to Javadoc
	 * TODO: use recursion for complex
	 */	
	public void createSpecies() {
		
		for (String key : entityPoolNodes.keySet()) {
			SWrapperSpeciesGlyph speciesGlyphTuple = createOneSpecies(key);
			listOfSpeciesGlyphTuples.add(speciesGlyphTuple);
		}
	}
	
	public SWrapperSpeciesGlyph createOneSpecies(String key) {
		ListOf<Species> listOfSpecies = model.getListOfSpecies();
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();	
		
		Species species;
		SpeciesGlyph speciesGlyph;
		Glyph glyph;
		String speciesId;
		String name = "";
		String clazz; 
		BoundingBox boundingBox;
		Bbox bbox;
		TextGlyph textGlyph;
		List<Glyph> nestedGlyphs;		
		
		glyph = entityPoolNodes.get(key);
		if (glyph.getLabel() != null) {
			name = glyph.getLabel().getText();
		}
		clazz = glyph.getClazz();
		speciesId = key;
		
		species = new Species(speciesId, name, 3, 1);
		addAnnotation(species, clazz);
		addSBO(species, clazz);
		listOfSpecies.add(species);
		
		speciesGlyph = new SpeciesGlyph();
		speciesGlyph.setId(speciesId+"_Glyph");
		speciesGlyph.setSpecies(species);
		bbox = glyph.getBbox();
		boundingBox = new BoundingBox();
		// todo: horizontal or vertical orientation?
		boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
		boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
		speciesGlyph.setBoundingBox(boundingBox);
		listOfSpeciesGlyphs.add(speciesGlyph);
		
		// todo: not just complex
		printHelper("clazz==",clazz);
		if (clazz.equals("complex")) {
			nestedGlyphs = glyph.getGlyph();
			for (Glyph nestedGlyph : nestedGlyphs) {
				if (nestedGlyph.getLabel() != null) {
					name = nestedGlyph.getLabel().getText();
				}
				clazz = nestedGlyph.getClazz();
				speciesId = nestedGlyph.getId();
				
				species = new Species(speciesId, name, 3, 1);
				addAnnotation(species, clazz);
				addSBO(species, clazz);
				listOfSpecies.add(species);
				
				printHelper("speciesId==",speciesId);
				speciesGlyph = new SpeciesGlyph();
				speciesGlyph.setId(speciesId+"_Glyph");
				speciesGlyph.setSpecies(species);
				bbox = nestedGlyph.getBbox();
				boundingBox = new BoundingBox();
				// todo: horizontal or vertical orientation?
				boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
				boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
				speciesGlyph.setBoundingBox(boundingBox);		
				listOfSpeciesGlyphs.add(speciesGlyph);
				
				createOneTextGlyph(species, speciesGlyph);
			}
		} else {
			createOneTextGlyph(species, speciesGlyph);
		}	
		
		SWrapperSpeciesGlyph speciesGlyphTuple =  new SWrapperSpeciesGlyph(species, speciesGlyph, glyph);
		return speciesGlyphTuple;
	}
	
	public SWrapperReactionGlyph createOneReaction(Reaction reaction, ReactionGlyph reactionGlyph, SWrapperReactionGlyph reactionGlyphTuple) {
		Arc arc;
		String speciesReferenceId;
		String reactionId;
		Curve curve;
		
		Object source;
		Object target;
		Glyph sourceGlyph;
		Port targetPort;
		Port sourcePort;
		Glyph targetGlyph;	
		
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		for (String key: inwardArcs.keySet()) {
			arc = inwardArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;
			
			//temp
			reactionId = findGlyphFromPort(targetPort);
			if (reactionId != reaction.getId()){continue;} else {reactionGlyphTuple.addArc(speciesReferenceId, arc, "inward");}
			
			speciesReference = new SpeciesReference();
			species = findSpecies(model.getListOfSpecies(), sourceGlyph.getId());
			if (species == null) {
				debugMode = 1;
				printHelper("createReactions", "inward "+key);
				debugMode = 0;
				continue;
			}
			speciesReference.setId(speciesReferenceId);
			speciesReference.setSpecies(species);
			
			//reactionId = targetPort.getId().substring(0, targetPort.getId().indexOf("."));
			reactionId = findGlyphFromPort(targetPort);
			reaction = findReaction(model.getListOfReactions(), reactionId);
	
			//try {reaction.addReactant(speciesReference);} catch (Exception e) {printHelper("createReactions", arc.toString()); continue;}
			reaction.addReactant(speciesReference);
			
			speciesReferenceGlyph = createOneSpeciesReferenceGlyph(speciesReferenceId, arc, speciesReference, sourceGlyph);
			
			curve = createOneSpeciesReferenceCurve(arc);
			speciesReferenceGlyph.setCurve(curve);
			reactionGlyph = findReactionGlyph(layout.getListOfReactionGlyphs(), reactionId+"_Glyph");
			//try {reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);} catch (Exception e) {printHelper("createReactions", arc.toString()); continue;}
			reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);
			
			updateReactionGlyph(reactionGlyph, speciesReferenceGlyph, "start");
		}	
		for (String key: outwardArcs.keySet()) {
			arc = outwardArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			
			//temp
			reactionId = findGlyphFromPort(sourcePort);
			if (reactionId != reaction.getId()){continue;} else {reactionGlyphTuple.addArc(speciesReferenceId, arc, "outward");}
			
			speciesReference = new SpeciesReference();
			species = findSpecies(model.getListOfSpecies(), targetGlyph.getId());
			if (species == null) {
				debugMode = 1;
				printHelper("createReactions", "outward "+key);
				debugMode = 0;
				continue;
			}
			speciesReference.setId(speciesReferenceId);
			speciesReference.setSpecies(species);
			
			//reactionId = sourcePort.getId().substring(0, sourcePort.getId().indexOf("."));
			reactionId = findGlyphFromPort(sourcePort);
			reaction = findReaction(model.getListOfReactions(), reactionId);

			reaction.addProduct(speciesReference);
			
			speciesReferenceGlyph = createOneSpeciesReferenceGlyph(speciesReferenceId, arc, speciesReference, targetGlyph);
			
			curve = createOneSpeciesReferenceCurve(arc);
			speciesReferenceGlyph.setCurve(curve);
			reactionGlyph = findReactionGlyph(layout.getListOfReactionGlyphs(), reactionId+"_Glyph");
			reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);
			
			updateReactionGlyph(reactionGlyph, speciesReferenceGlyph, "end");
		}
		for (String key: undirectedArcs.keySet()) {
			arc = undirectedArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			// assuming target is a process node, may not be true
			targetGlyph = (Glyph) target;	
			
			//todo: check the clazz first, then decide
			
			//temp
			reactionId = targetGlyph.getId();
			if (reactionId != reaction.getId()){continue;} else {reactionGlyphTuple.addArc(speciesReferenceId, arc, "undirected");}
			
			speciesReference = new SpeciesReference();
			species = findSpecies(model.getListOfSpecies(), sourceGlyph.getId());

			speciesReference.setId(speciesReferenceId);
			speciesReference.setSpecies(species);
			
			reactionId = targetGlyph.getId();
			reaction = findReaction(model.getListOfReactions(), reactionId);

			reaction.addReactant(speciesReference);
			
			speciesReferenceGlyph = createOneSpeciesReferenceGlyph(speciesReferenceId, arc, speciesReference, sourceGlyph);
			
			curve = createOneSpeciesReferenceCurve(arc);
			speciesReferenceGlyph.setCurve(curve);
			reactionGlyph = findReactionGlyph(layout.getListOfReactionGlyphs(), reactionId+"_Glyph");
			reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);
		}
		return reactionGlyphTuple;
	}
				
	/**
	 * Create multiple SBML <code>ReactionGlyph</code> and its associated <code>Reaction</code> from list of SBGN <code>Glyph</code> and <code>Arc</code>. 
	 */			
	public void createReactions() {
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		ListOf<ReactionGlyph> listOfReactionGlyphs = layout.getListOfReactionGlyphs();
		
		Reaction reaction;
		ReactionGlyph reactionGlyph;
		Glyph glyph;
		String reactionId;
		Curve curve;
		CurveSegment curveSegment;
		Point point;
		Bbox bbox;
		
		SWrapperReactionGlyph reactionGlyphTuple;
		
		for (String key: processNodes.keySet()) {
			glyph = processNodes.get(key);
			reactionId = glyph.getId();
			reaction = new Reaction();
			
			reaction.setId(reactionId);
			listOfReactions.add(reaction);
			
			reactionGlyph = new ReactionGlyph();
			reactionGlyph.setId(reactionId+"_Glyph");
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
			
			reactionGlyphTuple = new SWrapperReactionGlyph(reaction, reactionGlyph, glyph);
			
			listOfReactionGlyphTuples.add(reactionGlyphTuple);
			createOneReaction(reaction, reactionGlyph, reactionGlyphTuple);
		}
		
	}
	
	/**
	 * Update the start or end <code>Point</code> of <code>ReactionGlyph</code> using values in a <code>SpeciesReferenceGlyph</code>. 
	 */			
	public void updateReactionGlyph(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph speciesReferenceGlyph, String reactionGlyphPointType){
		Point curvePoint = null;
		Point reactionGlyphPoint = null;
		if (reactionGlyphPointType.equals("start")) {
			// for now,  we assume there is only 1 CurveSegment in this Curve, so getCurveSegment(0)
			curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(0).getEnd();
			reactionGlyphPoint = reactionGlyph.getCurve().getCurveSegment(0).getStart();
		} else if (reactionGlyphPointType.equals("end")) {
			curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(0).getStart();
			reactionGlyphPoint = reactionGlyph.getCurve().getCurveSegment(0).getEnd();			
		}
	
		reactionGlyphPoint.setX(curvePoint.getX());
		reactionGlyphPoint.setY(curvePoint.getY());
		reactionGlyphPoint.setZ(curvePoint.getZ());
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
	
	/**
	 * Create multiple SBML <code>CompartmentGlyph</code> and its associated <code>Compartment</code> from list of SBGN <code>Glyph</code>. 
	 */		
	public void createCompartments() {

		for (String key: compartments.keySet()) {
			SWrapperCompartmentGlyph compartmentGlyphTuple = createOneCompartment(key);
			listOfCompartmentGlyphTuples.add(compartmentGlyphTuple);
			//printHelper("===createCompartments",key);
		}
	}
		
	public SWrapperCompartmentGlyph createOneCompartment(String key) {
		ListOf<Compartment> listOfCompartments = model.getListOfCompartments();
		ListOf<CompartmentGlyph> listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();			
		
		Glyph glyph;
		String compartmentId;
		String name;
		Compartment compartment;
		CompartmentGlyph compartmentGlyph;
		Bbox bbox;
		BoundingBox boundingBox;
		float z;		
		
		glyph = compartments.get(key);
		//System.out.format("Entities=%s \n", glyph.toString());
		compartmentId = glyph.getId();		
		name = glyph.getLabel().getText();
		
		compartment = new Compartment(compartmentId, name, 3, 1);
		listOfCompartments.add(compartment);
		
		try {
			z = glyph.getCompartmentOrder();
			debugMode = 1;
			printHelper("depth==", Float.toString(z));
			debugMode = 0;
			if (isNull(z).equals("NULL")) {
				z = 0;
			}				
		} catch (NullPointerException e) {
			z = 0;
		}
					
		compartmentGlyph = new CompartmentGlyph();
		compartmentGlyph.setId(compartmentId+"_Glyph");
		compartmentGlyph.setCompartment(compartment);
		bbox = glyph.getBbox();
		boundingBox = new BoundingBox();
		// todo: horizontal?
		boundingBox.createDimensions(bbox.getW(), bbox.getH(), z);
		boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
		compartmentGlyph.setBoundingBox(boundingBox);
		listOfCompartmentGlyphs.add(compartmentGlyph);	
		
		return new SWrapperCompartmentGlyph(compartment, compartmentGlyph);
	}	
	
	
	/**
	 * Create a <code>SpeciesReferenceGlyph</code> using values from an SBGN <code>Arc</code>. 
	 * Associate the <code>SpeciesReferenceGlyph</code> with a <code>SpeciesGlyph</code> and a <code>SpeciesReference</code>.
	 * 
	 * @param <code>String</code> id
	 * @param <code>Arc</code> arc
	 * @param <code>SpeciesReference</code> speciesReference
	 * @param <code>Glyph</code> speciesGlyph
	 * @return <code>SpeciesReferenceGlyph</code> speciesReferenceGlyph
	 */		
	public SpeciesReferenceGlyph createOneSpeciesReferenceGlyph(String id, Arc arc, SpeciesReference speciesReference, Glyph speciesGlyph) {
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		speciesReferenceGlyph = new SpeciesReferenceGlyph();
		speciesReferenceGlyph.setId(id+"_Glyph");
		speciesReferenceGlyph.setRole(findReactionRole(arc.getClazz()));
		speciesReferenceGlyph.setSpeciesGlyph(speciesGlyph.getId()+"_Glyph");
		speciesReferenceGlyph.setSpeciesReference(speciesReference);	
					
		return speciesReferenceGlyph;
	}
	
	public ReferenceGlyph createOneReferenceGlyph(String id, Arc arc, SpeciesReference reference, Glyph glyph) {
		ReferenceGlyph referenceGlyph;
		
		referenceGlyph = new ReferenceGlyph();
		referenceGlyph.setId(id+"_Glyph");
		referenceGlyph.setGlyph(glyph.getId()+"_Glyph");

		return referenceGlyph;
	}	
	
	
	/**
	 * Create a TextGlyph using values from a <code>SpeciesGlyph</code> and its associated <code>Species</code>.
	 * 
	 * @param <code>Species</code> species
	 * @param <code>SpeciesGlyph</code> speciesGlyph
	 */		
	public TextGlyph createOneTextGlyph(Species species, SpeciesGlyph speciesGlyph) {
		TextGlyph textGlyph;
		String id;
		BoundingBox boundingBoxText;
		BoundingBox boundingBoxSpecies;
		
		textGlyph = new TextGlyph(3, 1);
		id = species.getId() + "_TextGlyph";
		textGlyph.setId(id);
		textGlyph.setOriginOfText(species);
		textGlyph.setGraphicalObject(speciesGlyph);
		boundingBoxText = new BoundingBox();
		boundingBoxSpecies = speciesGlyph.getBoundingBox();
		boundingBoxText.setDimensions(boundingBoxSpecies.getDimensions());
		boundingBoxText.setPosition(boundingBoxSpecies.getPosition());
		textGlyph.setBoundingBox(boundingBoxText);
		
		ListOf<TextGlyph> listOfTextGlyphs = layout.getListOfTextGlyphs();
		listOfTextGlyphs.add(textGlyph);
		
		return textGlyph;
	}
	
	
	public SWrapperReactionGlyph createOneGeneralGlyph(Reaction reaction, GeneralGlyph generalGlyph, SWrapperReactionGlyph reactionGlyphTuple) {
		Arc arc;
		String speciesReferenceId;
		String reactionId;
		Curve curve;
		
		Object source;
		Object target;
		Glyph sourceGlyph;
		Port targetPort;
		Port sourcePort;
		Glyph targetGlyph;	
		
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		ReferenceGlyph referenceGlyph;
		
		for (String key: inwardArcs.keySet()) {
			arc = inwardArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;
			
			//temp
			reactionId = findGlyphFromPort(targetPort);
			if (reactionId != reaction.getId()){continue;} else {reactionGlyphTuple.addArc(speciesReferenceId, arc, "inward");}
			
//			speciesReference = new SpeciesReference();
//			species = findSpecies(model.getListOfSpecies(), sourceGlyph.getId());
//			speciesReference.setId(speciesReferenceId);
//			speciesReference.setSpecies(species);
//			
//			reactionId = findGlyphFromPort(targetPort);
//			reaction = findReaction(model.getListOfReactions(), reactionId);
//	
//			reaction.addReactant(speciesReference);
			
			referenceGlyph = createOneReferenceGlyph(speciesReferenceId, arc, null, sourceGlyph);
			
			curve = createOneSpeciesReferenceCurve(arc);
			referenceGlyph.setCurve(curve);
			generalGlyph.getListOfReferenceGlyphs().add(referenceGlyph);
			
			reactionGlyphTuple.addReferenceGlyph(speciesReferenceId, referenceGlyph);
			
//			updateReactionGlyph(reactionGlyph, speciesReferenceGlyph, "start");
		}	
		for (String key: outwardArcs.keySet()) {
			arc = outwardArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			
			//temp
			reactionId = findGlyphFromPort(sourcePort);
			if (reactionId != reaction.getId()){continue;} else {reactionGlyphTuple.addArc(speciesReferenceId, arc, "outward");}
			
//			speciesReference = new SpeciesReference();
//			species = findSpecies(model.getListOfSpecies(), targetGlyph.getId());
//			speciesReference.setId(speciesReferenceId);
//			speciesReference.setSpecies(species);
//			
//			reactionId = findGlyphFromPort(sourcePort);
//			reaction = findReaction(model.getListOfReactions(), reactionId);
//
//			reaction.addProduct(speciesReference);
//			
			referenceGlyph = createOneReferenceGlyph(speciesReferenceId, arc, null, targetGlyph);
			
			curve = createOneSpeciesReferenceCurve(arc);
			referenceGlyph.setCurve(curve);
			generalGlyph.getListOfReferenceGlyphs().add(referenceGlyph);

			reactionGlyphTuple.addReferenceGlyph(speciesReferenceId, referenceGlyph);
			
//			updateReactionGlyph(reactionGlyph, speciesReferenceGlyph, "end");
		}
		for (String key: undirectedArcs.keySet()) {
			arc = undirectedArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			// assuming target is a process node, may not be true
			targetGlyph = (Glyph) target;	
			
			//todo: check the clazz first, then decide
			
			//temp
			reactionId = targetGlyph.getId();
			if (reactionId != reaction.getId()){continue;} else {reactionGlyphTuple.addArc(speciesReferenceId, arc, "undirected");}
			
//			speciesReference = new SpeciesReference();
//			species = findSpecies(model.getListOfSpecies(), sourceGlyph.getId());
//			speciesReference.setId(speciesReferenceId);
//			speciesReference.setSpecies(species);
//			
//			reactionId = targetGlyph.getId();
//			reaction = findReaction(model.getListOfReactions(), reactionId);
//
//			reaction.addReactant(speciesReference);
//			
//			speciesReferenceGlyph = createOneSpeciesReferenceGlyph(speciesReferenceId, arc, speciesReference, sourceGlyph);
//			
//			curve = createOneSpeciesReferenceCurve(arc);
//			speciesReferenceGlyph.setCurve(curve);
//			reactionGlyph = findReactionGlyph(layout.getListOfReactionGlyphs(), reactionId+"_Glyph");
//			reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);
		}
		return reactionGlyphTuple;		
	}
	
	public void createGeneralGlyphs() {
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		ListOf<GraphicalObject> listOfGeneralGlyphs = layout.getListOfAdditionalGraphicalObjects();
		
		Reaction reaction;
		GeneralGlyph generalGlyph;
		Glyph glyph;
		String reactionId;
		Curve curve;
		CurveSegment curveSegment;
		Point point;
		Bbox bbox;
		
		SWrapperReactionGlyph reactionGlyphTuple;
		
		Set<String> allReactions = new HashSet<String>();
		allReactions.addAll(logicOperators.keySet());
		allReactions.addAll(processNodes.keySet());
		
		debugMode = 1;
		printHelper("createGeneralGlyphs", allReactions.size());
		debugMode = 0;
		
		for (String key: allReactions) {
			glyph = logicOperators.get(key);
			
			//temp
			if (glyph==null){glyph = processNodes.get(key);}
				
			reactionId = glyph.getId();
			reaction = new Reaction();
			
			reaction.setId(reactionId);
			listOfReactions.add(reaction);
			
			generalGlyph = new GeneralGlyph();
			generalGlyph.setId(reactionId+"_Glyph");
			generalGlyph.setReference(reactionId);
			
			createBoundingBox(generalGlyph, glyph);
						
			listOfGeneralGlyphs.add(generalGlyph);
			
			reactionGlyphTuple = new SWrapperReactionGlyph(reaction, generalGlyph, glyph);
			
			listOfReactionGlyphTuples.add(reactionGlyphTuple);
			createOneGeneralGlyph(reaction, generalGlyph, reactionGlyphTuple);
		}		
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
	 * Set the dimensionX, dimensionY, dimensionZ from the <code>Point</code> values.
	 * 
	 * @param <code>Point</code> point
	 */			
	public void updateDimensions(Point point) {
		if (point.getX() > this.dimensionX) {
			this.dimensionX = point.getX();
		}
		if (point.getY() > this.dimensionY) {
			this.dimensionY = point.getY();
		}
		if (point.getZ() > this.dimensionZ) {
			this.dimensionZ = point.getZ();
		}		
	}
	
	/**
	 * Set the dimensionX, dimensionY, dimensionZ from the <code>BoundingBox</code> values.
	 * 
	 * @param <code>BoundingBox</code> boundingBox
	 */		
	public void updateDimensions(BoundingBox boundingBox) {
		Dimensions dimensions;
		Point point;
		
		dimensions = boundingBox.getDimensions();
		point = boundingBox.getPosition();
		
		if (point.getX() + dimensions.getWidth() > this.dimensionX) {
			this.dimensionX = point.getX() + dimensions.getWidth();
		}
		if (point.getY() + dimensions.getHeight() > this.dimensionY) {
			this.dimensionY = point.getY() + dimensions.getHeight();
		}
		if (point.getZ() + dimensions.getDepth() > this.dimensionZ) {
			this.dimensionZ = point.getZ() + dimensions.getDepth();
		}
	}
	
	public void updatePortToGlyphMap(Glyph glyph){
		List<Port> listOfPorts;
		listOfPorts = glyph.getPort();
		for (Port port: listOfPorts) {
			portToGlyphMap.put(port.getId(), glyph.getId());
		}
	}
	
	public String findGlyphFromPort(Port port) {
		return portToGlyphMap.get(port.getId());
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
	
	/**
	 * Find a <code>Species</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<ReactionGlyph></code> listOfSpecies
	 * @param <code>String</code> id
	 * @return the Species from the listOfSpecies
	 */			
	public Species findSpecies(ListOf<Species> listOfSpecies, String id) {
		for (Species species : listOfSpecies) {
			if (species.getId().equals(id)) {
				return species;
			}
		}
		return null;
	}
	
	/**
	 * Find a <code>SpeciesGlyph</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<SpeciesGlyph></code> listOfSpeciesGlyph
	 * @param <code>String</code> id
	 * @return the SpeciesGlyph from the listOfSpeciesGlyph
	 */			
	public SpeciesGlyph findSpeciesGlyph(ListOf<SpeciesGlyph> listOfSpeciesGlyph, String id) {
		for (SpeciesGlyph speciesGlyph : listOfSpeciesGlyph) {
			if (speciesGlyph.getId().equals(id)) {
				return speciesGlyph;
			}
		}
		return null;
	}	
	
	/**
	 * Find a <code>Reaction</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<Reaction></code> listOfReactions
	 * @param <code>String</code> id
	 * @return the Reaction from the listOfReactions
	 */			
	public Reaction findReaction(ListOf<Reaction> listOfReactions, String id) {
		for (Reaction reaction : listOfReactions) {
			if (reaction.getId().equals(id)) {
				//System.out.format("findReaction reaction=%s \n", reaction.getId());
				return reaction;
			}
		}
		return null;		
	}
	
	/**
	 * Find a <code>ReactionGlyph</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<ReactionGlyph></code> listOfReactionGlyph
	 * @param <code>String</code> id
	 * @return the ReactionGlyph from the listOfReactionGlyph
	 */		
	public ReactionGlyph findReactionGlyph(ListOf<ReactionGlyph> listOfReactionGlyph, String id) {
		for (ReactionGlyph reactionGlyph : listOfReactionGlyph) {
			if (reactionGlyph.getId().equals(id)) {
				return reactionGlyph;
			}
		}
		return null;
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
	
	public static void main(String[] args) throws FileNotFoundException {
		String sbgnFileNameInput;
		String sbmlFileNameOutput;
		String workingDirectory;
		
		Sbgn sbgnObject = null;
		Map map;
		SBGNML2SBML_GSOC2017 converter;		
		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbgnml2sbml.SBGNML2SBML_GSOC2017 <SBGNML filename>. "
					+ "filename example: /examples/sbgnml_examples/multimer.sbgn");
			return;
		}		
		
		workingDirectory = System.getProperty("user.dir");
		sbgnFileNameInput = args[0];
		sbgnFileNameInput = workingDirectory + sbgnFileNameInput;			
		sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
				
		sbgnObject = SBGNML2SBML_GSOC2017.readSbgnFile(sbgnFileNameInput);

		map = sbgnObject.getMap();	
		debugSbgnObject(map);
		
		converter = new SBGNML2SBML_GSOC2017(map);
		converter.convertToSBML();
			
		SBGNML2SBML_GSOC2017.writeSbgnFile(sbmlFileNameOutput, converter.model);
	}

	@Override
	public GeneralModel convert(GeneralModel sbgn) throws ConversionException, ReadModelException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHtmlDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultExtension() {
		// TODO Auto-generated method stub
		return null;
	}
		
}
