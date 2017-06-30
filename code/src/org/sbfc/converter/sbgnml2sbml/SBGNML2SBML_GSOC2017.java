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
	}

	/**
	 * Create the elements of an SBML <code>Model</code>, 
	 * which corresponds to contents of <code>Map</code> of <code>Sbgn</code>. 
	 * Each <code>Glyph</code> or <code>Arc</code> of the <code>Map</code> 
	 * is mapped to elements of the <code>Model</code>.
	 */	
	public void convertToSBML() {
		// Go over every Glyph or Arc in the libSBGN Map, classify them, and 
		// store the Glyph or Arc in the appropriate container in SWrapperModel 
		List<Glyph> listOfGlyphs = sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = sWrapperModel.map.getArc();
		
		addGlyphsToSWrapperModel(listOfGlyphs);

		createCompartments();
		createSpecies();		
	
		addArcsToSWrapperModel(listOfArcs);
		
		createReactions();
		
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
			sWrapperModel.addSWrapperSpeciesGlyph(key, speciesGlyphTuple);
		}
		
		for (String key : sWrapperModel.logicOperators.keySet()) {
			speciesGlyphTuple = createOneSpecies(key);
			sWrapperModel.addSWrapperSpeciesGlyph(key, speciesGlyphTuple);
		}
	}
	
	public SWrapperSpeciesGlyph createOneSpecies(String key) {
		Species species;
		SpeciesGlyph speciesGlyph;
		Glyph glyph;
		String speciesId;
		String name;
		String clazz; 
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
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		for (Glyph glyph : glyphs) {
			// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
			sWrapperGeneralGlyph = createOneGeneralGlyph(glyph, parent);
			
			sWrapperModel.addWrapperGeneralGlyph(glyph.getId(), sWrapperGeneralGlyph);
			listOfGeneralGlyphs.add(sWrapperGeneralGlyph.generalGlyph);
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
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
			sWrapperModel.addSWrapperReactionGlyph(key, sWrapperReactionGlyph);
		}
		
		for (String key: sWrapperModel.logicOperators.keySet()) {
			glyph = sWrapperModel.logicOperators.get(key);
			sWrapperReactionGlyph =  createOneReactionGlyph(glyph);
			sWrapperModel.addSWrapperReactionGlyph(key, sWrapperReactionGlyph);			
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
		//createSpeciesReferenceGlyphs(reaction, reactionGlyph, sWrapperReactionGlyph);	
		
		return sWrapperReactionGlyph;
	} 
	
	public SWrapperSpeciesReferenceGlyph createOneSpeciesReferenceGlyph(Reaction reaction, ReactionGlyph reactionGlyph,
			Arc arc, String speciesId, Glyph speciesGlyph, String reactionId, String speciesReferenceId) {
		Curve curve;
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = sUtil.createSpeciesReference(reaction, species, speciesReferenceId);
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = sUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, arc, 
			speciesReference, speciesGlyph);
		
		// create the center Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneSpeciesReferenceCurve(arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);		
		
		return new SWrapperSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, arc);
	}

	public void addGlyphsToSWrapperModel(List<Glyph> listOfGlyphs) {
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
	}
	
	public void addArcsToSWrapperModel(List<Arc> listOfArcs) {
		String id;
		
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			
			if (sUtil.isLogicArc(arc)){
				sWrapperModel.addLogicArc(id, arc);
			} else if (sUtil.isUndirectedArc(arc)) {
				sWrapperModel.addGlyphToGlyphArc(id, arc);
			} else if (sUtil.isInwardArc(arc)) {
				sWrapperModel.addGlyphToPortArc(id, arc);
			} else if (sUtil.isOutwardArc(arc)) {
				sWrapperModel.addPortToGlyphArc(id, arc);
			} 		
		}	
		
		Arc arc;
		String speciesReferenceId;
		String reactionId;
		
		Object source;
		Object target;
		Glyph sourceGlyph;
		Port targetPort;
		Port sourcePort;
		Glyph targetGlyph;	
		String speciesId;
		
		SWrapperReactionGlyph sWrapperReactionGlyph;
		
		for (String key: sWrapperModel.glyphToPortArcs.keySet()) {
			arc = sWrapperModel.glyphToPortArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;
			speciesId = sourceGlyph.getId();
			
			reactionId = sWrapperModel.findGlyphFromPort(targetPort);
			sWrapperReactionGlyph = sWrapperModel.getWrapperReactionGlyph(reactionId);
			// store the Arc in the Wrapper
			sWrapperReactionGlyph.addArc(speciesReferenceId, arc, "inward");
			
		}	
		for (String key: sWrapperModel.portToGlyphArcs.keySet()) {
			arc = sWrapperModel.portToGlyphArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			speciesId = targetGlyph.getId();
			
			reactionId = sWrapperModel.findGlyphFromPort(sourcePort);
			sWrapperReactionGlyph = sWrapperModel.getWrapperReactionGlyph(reactionId);
			sWrapperReactionGlyph.addArc(speciesReferenceId, arc, "outward");
		}
		for (String key: sWrapperModel.glyphToGlyphArcs.keySet()) {
			arc = sWrapperModel.glyphToGlyphArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			// assuming target is a process node, may not be true
			// todo: check the clazz first, then decide
			// it seems that for Modifier Arcs, they point into a ProcessNode, 
			// but does not point into a Port, it just points to the whole Glyph
			// this is why we have a 'Glyph to Glyph' Arc
			targetGlyph = (Glyph) target;	
			speciesId = sourceGlyph.getId();
			
			reactionId = targetGlyph.getId();
			sWrapperReactionGlyph = sWrapperModel.getWrapperReactionGlyph(reactionId);
			sWrapperReactionGlyph.addArc(speciesReferenceId, arc, "undirected");			
		}		
	}
	
	public List<SWrapperSpeciesReferenceGlyph> createSpeciesReferenceGlyphs(Reaction reaction, ReactionGlyph reactionGlyph, 
			SWrapperReactionGlyph reactionGlyphTuple) {
		List<SWrapperSpeciesReferenceGlyph> listOfSWrappersSRG = new ArrayList<SWrapperSpeciesReferenceGlyph>();
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
				// store the Arc in the Wrapper
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "inward");
			}
			
			// create a SpeciesReference and a SpeciesReferenceGlyph, add the SpeciesReferenceGlyph to the ReactionGlyph
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					arc, speciesId, sourceGlyph, reactionId, speciesReferenceId);
			// add the SpeciesReference to the Reaction
			reaction.addReactant(sWrapperSpeciesReferenceGlyph.speciesReference);	
			// this is a trick to correctly set the Start and End point of the center Curve of the ReactionGlyph
			// note that this doesn't work well
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "start");
		
			// add the SpeciesReferenceGlyph to the SWrapperReactionGlyph
			// and add the enclosing SWrapperSpeciesReferenceGlyph to the List<SWrapperSpeciesReferenceGlyph>
			// note that the second step is optional
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);	
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
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
				// skip Modifier Arcs coming out of a LogicOperator going into a ProcessNode
				// these Arcs, once created a SpeciesReferenceGlyph, does not have a SpeciesGlyph to reference to.
				// i.e. these arcs points to a ReactionGlyph, not a SpeciesGlyph
				// check if the Arc is a Modifier, and it comes out of a LogicOperator
				if (isModifierArcsOutOfLogicOperator(arc)){
					sWrapperSpeciesReferenceGlyph = createOneReferenceGlyph(reactionGlyph, arc);
					reactionGlyphTuple.addReferenceGlyph(speciesReferenceId, referenceGlyph);
					continue;
				}
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "outward");
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addProduct(sWrapperSpeciesReferenceGlyph.speciesReference);
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "end");
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}
		
		// todo: change ReferenceGlyph to ModifierReferenceGlyph
		for (String key: sWrapperModel.undirectedArcs.keySet()) {
			arc = sWrapperModel.undirectedArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			// assuming target is a process node, may not be true
			// todo: check the clazz first, then decide
			// it seems that for Modifier Arcs, they point into a ProcessNode, 
			// but does not point into a Port, it just points to the whole Glyph
			// this is why we have a 'Glyph to Glyph' Arc
			targetGlyph = (Glyph) target;	
			speciesId = sourceGlyph.getId();
			
			reactionId = targetGlyph.getId();
			if (reactionId != reaction.getId()){
				continue;
			} else {
				// this check might be redundant, we assume only Modifiers Arcs connects to Glyphs only.
				if (isModifierArcsOutOfLogicOperator(arc)){continue;}
				
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "undirected");
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addReactant(sWrapperSpeciesReferenceGlyph.speciesReference);
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}
		return listOfSWrappersSRG;
	}
				

	public boolean isModifierArcsOutOfLogicOperator(Arc arc){
		String clazz = arc.getClazz();
		
		if (clazz.equals("catalysis") || 
				clazz.equals("modulation") ||
				clazz.equals("stimulation") ||
				clazz.equals("inhibition") ){		// todo: and many others
			Object source = arc.getSource();
			if (source instanceof Glyph){
				if (sUtil.isLogicOperator(((Glyph) source).getClazz())){
					return true;
				}
			} else if (source instanceof Port){
				String reactionId = sWrapperModel.findGlyphFromPort((Port) source);
				if (sWrapperModel.logicOperators.get(reactionId) != null){
					return true;
				}
			}
		}
		
		return false;
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

		for (String key: sWrapperModel.compartments.keySet()) {
			SWrapperCompartmentGlyph compartmentGlyphTuple = createOneCompartment(key);
			sWrapperModel.listOfWrapperCompartmentGlyphs.add(compartmentGlyphTuple);
		}
	}
		
	public SWrapperCompartmentGlyph createOneCompartment(String key) {
		Glyph glyph;
		String compartmentId;
		String name;
		Compartment compartment;
		CompartmentGlyph compartmentGlyph;
		
		glyph = sWrapperModel.compartments.get(key);
		compartmentId = glyph.getId();		
		name = sUtil.getText(glyph);
		
		compartment = sUtil.createJsbmlCompartment(compartmentId, name);
		sOutput.addCompartment(compartment);
		
		compartmentGlyph = sUtil.createJsbmlCompartmentGlyph(glyph, compartmentId, compartment, true);
		sOutput.addCompartmentGlyph(compartmentGlyph);				
		
		sUtil.setCompartmentOrder(compartmentGlyph, glyph);
		
		return new SWrapperCompartmentGlyph(compartment, compartmentGlyph, glyph);
	}	

	
	public SWrapperGeneralGlyph createOneGeneralGlyph(Glyph glyph, GraphicalObject parent) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		text = sUtil.getText(glyph);
		clazz = glyph.getClazz();
		bbox = glyph.getBbox();
		generalGlyph = sUtil.createJsbmlGeneralGlyph(glyph, true, bbox);
		
		sUtil.createJsbmlTextGlyph(generalGlyph, text);		

		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, glyph, parent);
		return sWrapperGeneralGlyph;		
	}
	
	public SWrapperSpeciesReferenceGlyph createOneReferenceGlyph(ReactionGlyph reactionGlyph, Arc arc) {
		Curve curve;
		ReferenceGlyph referenceGlyph;
		GraphicalObject object;
		
		object = 
		
		// create a SpeciesReferenceGlyph
		referenceGlyph = sUtil.createOneReferenceGlyph(arc.getId(), arc, 
			null, object);
		
		// create the center Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneSpeciesReferenceCurve(arc);
		referenceGlyph.setCurve(curve);
		
		return new SWrapperSpeciesReferenceGlyph(referenceGlyph, arc);
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
