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

/**
 * The SBGNML2SBML_GSOC2017 class is the primary converter. 
 * It coordinates the creation of the JSBML Model and its extensions. 
 * Model elements are added after interpreting the input libSBGN Map.
 */	
public class SBGNML2SBML_GSOC2017  extends GeneralConverter{
	// A Model wrapper that stores every Model element. 
	// Example: Species, Reaction, Compartment, etc.
	// SBGNML2SBML_GSOC2017 does not store any Model information.
	SWrapperModel sWrapperModel;
	// Contains all data structured needed to create the output XML document. 
	// Example: LayoutModelPlugin.
	SBGNML2SBMLOutput sOutput;
	// Contains methods that do depend on any information in the Model. 
	// Example: finding a value from a given list.
	SBGNML2SBMLUtil sUtil;
	// Contains methods to create the RenderInformation.
	SBGNML2SBMLRender sRender;
		
	public SBGNML2SBML_GSOC2017(Map map) {
		sOutput = new SBGNML2SBMLOutput(3, 1);
		sUtil = new SBGNML2SBMLUtil(3, 1);
		sRender = new SBGNML2SBMLRender();
		sWrapperModel = new SWrapperModel(sOutput.getModel(), map);

		// Go over every Glyph or Arc in the libSBGN Map, classify them, and 
		// store the Glyph or Arc in the appropriate container in SWrapperModel 
		List<Glyph> listOfGlyphs = map.getGlyph();
		List<Arc> listOfArcs = map.getArc();
		String id;
		String clazz; 		
		
		for (Glyph glyph: listOfGlyphs) {
			id = glyph.getId();
			clazz = glyph.getClazz();

			if (sUtil.isProcessNode(clazz)) {
				sWrapperModel.addSbgnProcessNode(id, glyph);
			} else if (sUtil.isCompartment(clazz)) {
				sWrapperModel.addSbgnCompartment(id, glyph);
			} else if (sUtil.isEntityPoolNode(clazz)) {
				sWrapperModel.addSbgnEntityPoolNode(id, glyph);
			} else if (sUtil.isLogicOperator(clazz)) {
				sWrapperModel.addSbgnLogicOperator(id, glyph);
			}
		}

		for (Arc arc: listOfArcs) {
			id = arc.getId();
			clazz = arc.getClazz();
			
			if (sUtil.isUndirectedArc(arc)) {
				sWrapperModel.addSbgnUndirectedArc(id, arc);
			} else if (sUtil.isInwardArc(arc)) {
				sWrapperModel.addSbgnInwardArc(id, arc);
			} else if (sUtil.isOutwardArc(arc)) {
				sWrapperModel.addSbgnOutwardArc(id, arc);
			} 		
		}
	}

	/**
	 * Create the elements of an SBML <code>Model</code>, 
	 * which corresponds to contents of <code>Map</code> of <code>Sbgn</code>. 
	 * Each <code>Glyph</code> or <code>Arc</code> of the <code>Map</code> 
	 * is mapped to elements of the <code>Model</code>.
	 */	
	public void convertToSBML() {
		createCompartments();
		createSpecies();
		createReactions();
		createGeneralGlyphs();
		
		sOutput.createCanvasDimensions();
	}
	
	/**
	 * Create multiple SBML <code>SpeciesGlyph</code> and its associated 
	 * <code>Species</code> from list of SBGN <code>Glyph</code>. 
	 * TODO: add more details to Javadoc
	 * TODO: use recursion for complex
	 */	
	public void createSpecies() {
		SWrapperSpeciesGlyph speciesGlyphTuple;
		for (String key : sWrapperModel.entityPoolNodes.keySet()) {
			speciesGlyphTuple = createOneSpecies(key);
			sWrapperModel.listOfWrapperSpeciesGlyphs.add(speciesGlyphTuple);
		}
	}
	
	public SWrapperSpeciesGlyph createOneSpecies(String key) {
		Species species;
		SpeciesGlyph speciesGlyph;
		Glyph glyph;
		String speciesId;
		String name;
		String clazz; 
		BoundingBox boundingBox;
		Bbox bbox;
		TextGlyph textGlyph;
		List<Glyph> nestedGlyphs;	
		SWrapperSpeciesGlyph speciesGlyphTuple;
		List<GeneralGlyph> listOfGeneralGlyphs = null;
		
		glyph = sWrapperModel.entityPoolNodes.get(key);
		name = sUtil.getText(glyph);
		clazz = glyph.getClazz();
		speciesId = key;
		
		// create a Species, add it to the output
		species = sUtil.createJsbmlSpecies(speciesId, name, clazz, false, true);
		sOutput.addSpecies(species);
		
		// create a SpeciesGlyph, add it to the output 
		bbox = glyph.getBbox();
		speciesGlyph = sUtil.createJsbmlSpeciesGlyph(speciesId, name, clazz, species, true, bbox);
		sOutput.addSpeciesGlyph(speciesGlyph);
		
		// if the Glyph contains nested Glyphs, create GeneralGlyphs for these, add them to output
		if (glyph.getGlyph().size() != 0){
			nestedGlyphs = glyph.getGlyph();
			listOfGeneralGlyphs = createGeneralGlyphs(nestedGlyphs, speciesGlyph);
		} 
		
		// create TextGlyph for the SpeciesGlyph
		textGlyph = sUtil.createJsbmlTextGlyph(species, speciesGlyph);
		sOutput.addTextGlyph(textGlyph);
		
		// create a new SWrapperSpeciesGlyph class, store a list of GeneralGlyphs if present
		speciesGlyphTuple =  new SWrapperSpeciesGlyph(species, speciesGlyph, glyph);
		speciesGlyphTuple.setListOfGeneralGlyphs(listOfGeneralGlyphs);
		
		return speciesGlyphTuple;
	}
	
	public List<GeneralGlyph> createGeneralGlyphs(List<Glyph> glyphs, GraphicalObject parent) {
		List<GeneralGlyph> listOfGeneralGlyphs = new ArrayList<GeneralGlyph>();
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		for (Glyph glyph : glyphs) {
			text = sUtil.getText(glyph);
			clazz = glyph.getClazz();
			bbox = glyph.getBbox();
			generalGlyph = sUtil.createJsbmlGeneralGlyph(glyph, true, bbox);
			
			sUtil.createJsbmlTextGlyph(generalGlyph, text);		
			listOfGeneralGlyphs.add(generalGlyph);
			
			// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
			sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, glyph, parent);
			sWrapperModel.listOfWrapperGeneralGlyphs.add(sWrapperGeneralGlyph);
			
		}		
		return listOfGeneralGlyphs;
	}
	
	/**
	 * Create multiple SBML <code>ReactionGlyph</code> and its associated 
	 * <code>Reaction</code> from list of SBGN <code>Glyph</code> and <code>Arc</code>. 
	 */			
	public void createReactions() {
		SWrapperReactionGlyph sWrapperReactionGlyph;
		Glyph glyph;
		
		for (String key: sWrapperModel.processNodes.keySet()) {
			glyph = sWrapperModel.processNodes.get(key);
			sWrapperReactionGlyph =  createOneReactionGlyph(glyph);
			sWrapperModel.listOfWrapperReactionGlyphs.add(sWrapperReactionGlyph);
		}
		
		for (String key: sWrapperModel.logicOperators.keySet()) {
			glyph = sWrapperModel.logicOperators.get(key);
			sWrapperReactionGlyph =  createOneReactionGlyph(glyph);
			sWrapperModel.listOfWrapperReactionGlyphs.add(sWrapperReactionGlyph);			
		}
		
	}	
	
	public SWrapperReactionGlyph createOneReactionGlyph(Glyph glyph) {
		String reactionId;
		String name;
		String clazz;
		Reaction reaction;
		ReactionGlyph reactionGlyph;
		Bbox bbox;
		SWrapperReactionGlyph sWrapperReactionGlyph;
		
		reactionId = glyph.getId();
		name = sUtil.getText(glyph);
		clazz = glyph.getClazz();
		
		reaction = sUtil.createJsbmlReaction(reactionId);
		sOutput.addReaction(reaction);
		
		bbox = glyph.getBbox();
		reactionGlyph = sUtil.createJsbmlReactionGlyph(reactionId, name, clazz, reaction, true, bbox);
		sUtil.createReactionGlyphCurve(reactionGlyph, glyph);
		sOutput.addReactionGlyph(reactionGlyph);
		
		sWrapperReactionGlyph = new SWrapperReactionGlyph(reaction, reactionGlyph, glyph);
		createSpeciesReferenceGlyphs(reaction, reactionGlyph, sWrapperReactionGlyph);	
		
		return sWrapperReactionGlyph;
	} 
	
	public SWrapperSpeciesReferenceGlyph createOneSpeciesReferenceGlyphs(Reaction reaction, ReactionGlyph reactionGlyph,
			Arc arc, String speciesId, Glyph speciesGlyph, String reactionId, String speciesReferenceId) {
		Curve curve;
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		species = sOutput.findSpecies(speciesId);
		speciesReference = sUtil.createSpeciesReference(reaction, species, speciesReferenceId);
		
		speciesReferenceGlyph = sUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, arc, 
			speciesReference, speciesGlyph);
		
		curve = sUtil.createOneSpeciesReferenceCurve(arc);
		speciesReferenceGlyph.setCurve(curve);
		
		reactionGlyph = sOutput.findReactionGlyph(reactionId+"_Glyph");
		reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);		
		
		return new SWrapperSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, arc);
	}

	
	public List<SpeciesReferenceGlyph> createSpeciesReferenceGlyphs(Reaction reaction, ReactionGlyph reactionGlyph, 
			SWrapperReactionGlyph reactionGlyphTuple) {
		List<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyph = new ArrayList<SpeciesReferenceGlyph>();
		
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
		String speciesId;
		
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph;
		
		for (String key: sWrapperModel.inwardArcs.keySet()) {
			arc = sWrapperModel.inwardArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;
			speciesId = sourceGlyph.getId();
			
			reactionId = sWrapperModel.findGlyphFromPort(targetPort);
			if (reactionId != reaction.getId()){
				continue;
			} else {
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "inward");
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyphs(reaction, reactionGlyph,
					arc, speciesId, sourceGlyph, reactionId, speciesReferenceId);
			reaction.addReactant(sWrapperSpeciesReferenceGlyph.speciesReference);		
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "start");
		}	
		for (String key: sWrapperModel.outwardArcs.keySet()) {
			arc = sWrapperModel.outwardArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			speciesId = targetGlyph.getId();
			
			reactionId = sWrapperModel.findGlyphFromPort(sourcePort);
			if (reactionId != reaction.getId()){
				continue;
			} else {
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "outward");
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyphs(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addProduct(sWrapperSpeciesReferenceGlyph.speciesReference);
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "end");
		}
		for (String key: sWrapperModel.undirectedArcs.keySet()) {
			arc = sWrapperModel.undirectedArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			// assuming target is a process node, may not be true
			// todo: check the clazz first, then decide
			targetGlyph = (Glyph) target;	
			speciesId = sourceGlyph.getId();
			
			reactionId = targetGlyph.getId();
			if (reactionId != reaction.getId()){
				continue;
			} else {
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "undirected");
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyphs(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addReactant(sWrapperSpeciesReferenceGlyph.speciesReference);
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
		}
		return listOfSpeciesReferenceGlyph;
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
	
	

	
	public ReferenceGlyph createOneReferenceGlyph(String id, Arc arc, SpeciesReference reference, Glyph glyph) {
		ReferenceGlyph referenceGlyph;
		
		referenceGlyph = new ReferenceGlyph();
		referenceGlyph.setId(id+"_Glyph");
		referenceGlyph.setGlyph(glyph.getId()+"_Glyph");

		return referenceGlyph;
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
