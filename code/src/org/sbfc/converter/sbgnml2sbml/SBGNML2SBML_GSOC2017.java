package org.sbfc.converter.sbgnml2sbml;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;

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
		sWrapperModel = new SWrapperModel(sOutput.getModel(), map);
		sRender = new SBGNML2SBMLRender(sWrapperModel, sOutput);
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
		sUtil.createDefaultCompartment(sOutput.model);
	
		addArcsToSWrapperModel(listOfArcs);
		
		createReactions();
		createGeneralGlyphs();
		
		sOutput.createCanvasDimensions();
		
		sRender.renderCompartmentGlyphs();
		sRender.renderSpeciesGlyphs();
		sRender.renderReactionGlyphs();
		sRender.renderGeneralGlyphs();
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
				System.out.println("isLogicArc "+arc.getClazz());
			} else if (sUtil.isUndirectedArc(arc)) {
				sWrapperModel.addGlyphToGlyphArc(id, arc);
				System.out.println("isUndirectedArc "+arc.getClazz());
			} else if (sUtil.isInwardArc(arc)) {
				sWrapperModel.addGlyphToPortArc(id, arc);
				System.out.println("isInwardArc "+arc.getClazz());
			} else if (sUtil.isOutwardArc(arc)) {
				sWrapperModel.addPortToGlyphArc(id, arc);
				System.out.println("isOutwardArc "+arc.getClazz());
			} 		
		}	
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
		
		glyph = sWrapperModel.getGlyph(key);
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
			sWrapperGeneralGlyph = createOneGeneralGlyph(glyph, parent, true);
			
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
		
//		for (String key: sWrapperModel.logicOperators.keySet()) {
//			glyph = sWrapperModel.logicOperators.get(key);
//			sWrapperReactionGlyph =  createOneReactionGlyph(glyph);
//			sWrapperModel.addSWrapperReactionGlyph(key, sWrapperReactionGlyph);			
//		}
		
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
		
		for (String key: sWrapperModel.glyphToPortArcs.keySet()) {
			arc = sWrapperModel.glyphToPortArcs.get(key);
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
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "glyphToPort");
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
		for (String key: sWrapperModel.portToGlyphArcs.keySet()) {
			arc = sWrapperModel.portToGlyphArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			speciesId = targetGlyph.getId();
					
			reactionId = sWrapperModel.findGlyphFromPort(sourcePort);
			if (sUtil.isModifierArc(arc.getClazz()) == true) {
				reactionId = targetGlyph.getId();
				System.out.println(reactionId);
				if (reactionId != reaction.getId()){
					continue;
				}
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "portToGlyph");
			} else if (reactionId != reaction.getId()){
				continue;
			} else {
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "portToGlyph");
			}
			
			System.out.println(key);
			System.out.println(speciesId);
			System.out.println(reactionId);
			System.out.println();
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addProduct(sWrapperSpeciesReferenceGlyph.speciesReference);
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "end");
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}
		// todo: change SpeciesReference to ModifierSpeciesReference
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
			if (reactionId != reaction.getId()){
				continue;
			} else {
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "glyphToGlyph");
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addReactant(sWrapperSpeciesReferenceGlyph.speciesReference);
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}
		// Modifier Arcs coming out of a LogicOperator going into a ProcessNode
		// these Arcs, once created a SpeciesReferenceGlyph, does not have a SpeciesGlyph to reference to.
		// i.e. these arcs points to a ReactionGlyph, not a SpeciesGlyph
		// if the Arc is a Modifier, and it comes out of a LogicOperator, it is part of the ReactionGlyph
//		if (isModifierArcsOutOfLogicOperator(arc)){
//			sWrapperSpeciesReferenceGlyph = createOneReferenceGlyph(reactionGlyph, arc);
//			reactionGlyphTuple.addReferenceGlyph(speciesReferenceId, referenceGlyph);
//			continue;
//		}
		// Here's an alternative: isModifierArcsIntoLogicOperator(arc)
		// in this case, we know the Arc is a Logic Arc, it will be part of a GeneralGlyph without associating
		// with any core Model elements. i.e. the Model is missing some arcs
		
		return listOfSWrappersSRG;
	}
				
	// obsolete
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
			sWrapperModel.addSWrapperCompartmentGlyph(key, compartmentGlyphTuple);
			System.out.println("createCompartments " + sWrapperModel.listOfWrapperCompartmentGlyphs.size());
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

	
	public SWrapperGeneralGlyph createOneGeneralGlyph(Glyph glyph, GraphicalObject parent, boolean setBoundingBox) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		text = sUtil.getText(glyph);
		clazz = glyph.getClazz();
		bbox = glyph.getBbox();
		generalGlyph = sUtil.createJsbmlGeneralGlyph(glyph.getId(), setBoundingBox, bbox);
		
		sUtil.createJsbmlTextGlyph(generalGlyph, text);		

		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, glyph, parent);
		return sWrapperGeneralGlyph;		
	}
	
	public SWrapperGeneralGlyph createOneGeneralGlyph(Arc arc) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		clazz = arc.getClazz();
		generalGlyph = sUtil.createJsbmlGeneralGlyph(arc.getId(), false, null);

		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, arc);
		return sWrapperGeneralGlyph;		
	}	
	
	public SWrapperReferenceGlyph createOneReferenceGlyph(Arc arc, GraphicalObject object) {
		Curve curve;
		ReferenceGlyph referenceGlyph;
		
		// create a SpeciesReferenceGlyph
		referenceGlyph = sUtil.createOneReferenceGlyph(arc.getId(), arc, 
			null, object);
		
		// create the center Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneSpeciesReferenceCurve(arc);
		referenceGlyph.setCurve(curve);

		return new SWrapperReferenceGlyph(referenceGlyph, arc);
	}
	
	public void createGeneralGlyphs() {
		Arc arc;
		String referenceId;
		Curve curve;
		
		Object source;
		Object target;
		Glyph sourceGlyph;
		Port targetPort;
		Port sourcePort;
		Glyph targetGlyph;	
		String objectId;

		ReferenceGlyph referenceGlyph;
		SpeciesGlyph speciesGlyph;
		
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		
		// create a GeneralGlyph for each Logic Arc
		for (String key: sWrapperModel.logicArcs.keySet()) {
			arc = sWrapperModel.logicArcs.get(key);
			referenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;	
			objectId = sourceGlyph.getId();
					
			sWrapperGeneralGlyph = createOneGeneralGlyph(arc);
			sWrapperModel.addWrapperGeneralGlyph(arc.getId(), sWrapperGeneralGlyph);
			
			speciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(objectId).speciesGlyph;
			sWrapperReferenceGlyph = createOneReferenceGlyph(arc, speciesGlyph);
			sWrapperGeneralGlyph.generalGlyph.addReferenceGlyph(sWrapperReferenceGlyph.referenceGlyph);
			sWrapperGeneralGlyph.addSpeciesReferenceGlyph(arc.getId(), sWrapperReferenceGlyph.referenceGlyph, arc);
			
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
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
				
		sbgnObject = SBGNML2SBMLUtil.readSbgnFile(sbgnFileNameInput);

		map = sbgnObject.getMap();	
		SBGNML2SBMLUtil.debugSbgnObject(map);
		
		converter = new SBGNML2SBML_GSOC2017(map);
		converter.convertToSBML();
			
		SBGNML2SBMLUtil.writeSbgnFile(sbmlFileNameOutput, converter.sOutput.model);
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
