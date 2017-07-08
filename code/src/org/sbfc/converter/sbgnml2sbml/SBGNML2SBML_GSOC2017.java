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
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.LineSegment;
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
	// Contains all data structures needed to create the output XML document. 
	// Example: LayoutModelPlugin.
	public SBGNML2SBMLOutput sOutput;
	// Contains methods that do not depend on any information in the Model. 
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
		List<Glyph> listOfGlyphs = sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = sWrapperModel.map.getArc();
		
		// Go over every Glyph in the libSBGN Map, classify them, and 
		// store the Glyph in the appropriate container in SWrapperModel 		
		addGlyphsToSWrapperModel(listOfGlyphs);

		// Create Compartments and CompartmentGlyphs for the classified SBGN Glyphs
		createCompartments();
		// Create Species and SpeciesGlyphs for the classified SBGN Glyphs
		createSpecies();	
		// Check if the Model contains any Compartments. Check if any Species is does not have a Comparment.
		// If not, create a default one and assign every Species without a Compartment to this Compartment.
		// todo: move to ModelCompleter
		sUtil.createDefaultCompartment(sOutput.model);
	
		// Go over every Arc in the libSBGN Map, classify them, and 
		// store the Arc in the appropriate container in SWrapperModel 
		addArcsToSWrapperModel(listOfArcs);
		
		// Create Reactions and ReactionGlyphs using the classified Glyphs.
		// Then create SpeciesReference and SpeciesReferenceGlyphs using the classified SBGN Arcs
		createReactions();
		// Create GeneralGlyphs for Arcs that do not belong to any Reactions. i.e. Logics Arcs
		// note that LogicOperators are classified as SpeciesGlyphs. 
		// see or-simple.sbgn
		// We cannot convert a Modifier Arcs to a SpeciesReference if we classify LogicOperators 
		// as ReactionGlyphs. 
		// Modifier Arcs now have a SpeciesReference, and are part of a Reaction.
		createGeneralGlyphs();
		
		// Set the Dimensions for the Layout
		sOutput.createCanvasDimensions();
		
		// Render all elements currently in the Model Layout
		sRender.renderCompartmentGlyphs();
		sRender.renderSpeciesGlyphs();
		sRender.renderReactionGlyphs();
		sRender.renderGeneralGlyphs();
		
		sOutput.completeModel();
	}

	/**
	 * Classify every <code>Glyph</code> in the libSBGN <code>Map</code>, store the <code>Glyph</code> 
	 * in the appropriate container in SWrapperModel.
	 */		
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

	/**
	 * Classify every <code>Arc</code> in the libSBGN <code>Map</code>, store the <code>Arc</code>
	 * in the appropriate container in SWrapperModel.
	 */
	public void addArcsToSWrapperModel(List<Arc> listOfArcs) {
		String id;
		SWrapperArc sWrapperArc;
		
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			sWrapperArc = createWrapperArc(arc);
			
			if (sUtil.isLogicArc(arc)){
				sWrapperModel.addLogicArc(id, sWrapperArc);
			} else if (sWrapperModel.getWrapperSpeciesGlyph(sWrapperArc.sourceId) != null &&
					sWrapperModel.getWrapperSpeciesGlyph(sWrapperArc.targetId) != null){
				sWrapperModel.addLogicArc(id, sWrapperArc);
			} else if (sUtil.isModifierArc(arc.getClazz())) {
				sWrapperModel.addModifierArc(id, sWrapperArc);
			} else if (sUtil.isConsumptionArc(arc.getClazz())) {
				sWrapperModel.addConsumptionArc(id, sWrapperArc);
			} else if (sUtil.isProductionArc(arc.getClazz())) {
				sWrapperModel.addProductionArc(id, sWrapperArc);
			} 		
		}	
	}	
	
	
	/**
	 * Create multiple SBML <code>SpeciesGlyph</code>s and its associated 
	 * <code>Species</code> from the list of SBGN <code>Glyph</code>s in sWrapperModel. 
	 * Add the created <code>SWrapperSpeciesGlyph</code>s to the sWrapperModel.
	 */	
	public void createSpecies() {
		SWrapperSpeciesGlyph speciesGlyphTuple;
		Glyph glyph;
		for (String key : sWrapperModel.entityPoolNodes.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			speciesGlyphTuple = createOneSpecies(glyph);
			sWrapperModel.addSWrapperSpeciesGlyph(key, speciesGlyphTuple);
//			sOutput.addTextGlyph(speciesGlyphTuple.textGlyph);
//			sOutput.addSpeciesGlyph(speciesGlyphTuple.speciesGlyph);
		}
		
		for (String key : sWrapperModel.logicOperators.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			speciesGlyphTuple = createOneSpecies(glyph);
			sWrapperModel.addSWrapperSpeciesGlyph(key, speciesGlyphTuple);
		}
	}
	
	/**
	 * Create a SBML <code>Species</code> and <code>SpeciesGlyph</code>, 
	 * create any <code>TextGlyph</code>s for the Species. Create any <code>GeneralGlyph</code>s for the Species. 
	 * Construct a <code>SWrapperSpeciesGlyph</code>. 
	 */		
	public SWrapperSpeciesGlyph createOneSpecies(Glyph glyph) {
		Species species;
		SpeciesGlyph speciesGlyph;
		String speciesId;
		String name;
		String clazz; 
		Bbox bbox;
		TextGlyph textGlyph;
		List<Glyph> nestedGlyphs;	
		SWrapperSpeciesGlyph speciesGlyphTuple;
		List<GraphicalObject> listOfGeneralGlyphs = null;
		
		name = sUtil.getText(glyph);
		clazz = glyph.getClazz();
		speciesId = glyph.getId();
		
		// create a Species, add it to the output
		species = sUtil.createJsbmlSpecies(speciesId, name, clazz, false, true);
		sOutput.addSpecies(species);
		
		// create a SpeciesGlyph, add it to the output 
		bbox = glyph.getBbox();
		speciesGlyph = sUtil.createJsbmlSpeciesGlyph(speciesId, name, clazz, species, true, bbox);
		sOutput.addSpeciesGlyph(speciesGlyph);
		
		// if the Glyph contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: a Species might have Units of Information
		if (glyph.getGlyph().size() != 0){
			nestedGlyphs = glyph.getGlyph();
			listOfGeneralGlyphs = createNestedGlyphs(nestedGlyphs, speciesGlyph);
		} 
		
		// create TextGlyph for the SpeciesGlyph
		textGlyph = sUtil.createJsbmlTextGlyph(species, speciesGlyph);
		sOutput.addTextGlyph(textGlyph);
		
		// create a new SWrapperSpeciesGlyph class, store a list of GeneralGlyphs if present
		speciesGlyphTuple =  new SWrapperSpeciesGlyph(species, speciesGlyph, glyph, textGlyph);
		speciesGlyphTuple.setListOfNestedGlyphs(listOfGeneralGlyphs);
		
		return speciesGlyphTuple;
	}
	
	/**
	 * Create multiple SBML <code>GeneralGlyph</code>/<code>SpeciesGlyph</code>s from the list of SBGN <code>Glyph</code>s
	 * provided. Add the created <code>SWrapperGeneralGlyph</code>/<code>SWrapperSpeciesGlyph</code>s to the sWrapperModel. 
	 */		
	public List<GraphicalObject> createNestedGlyphs(List<Glyph> glyphs, GraphicalObject parent) {
		List<GraphicalObject> listOfGeneralGlyphs = new ArrayList<GraphicalObject>();
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperSpeciesGlyph sWrapperSpeciesGlyph;
		
		for (Glyph glyph : glyphs) {
			if (sUtil.isEntityPoolNode(glyph.getClazz())){
				sWrapperSpeciesGlyph = createOneSpecies(glyph);
				System.out.println("createNestedGlyphs"+sWrapperSpeciesGlyph.speciesGlyph.getId());
//				sOutput.addTextGlyph(sWrapperSpeciesGlyph.textGlyph);
//				sOutput.addSpeciesGlyph(sWrapperSpeciesGlyph.speciesGlyph);
				
				sWrapperModel.addSWrapperSpeciesGlyph(glyph.getId(), sWrapperSpeciesGlyph);
				
			} else {
				// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
				sWrapperGeneralGlyph = createOneGeneralGlyph(glyph, parent, true);
				
				sWrapperModel.addWrapperGeneralGlyph(glyph.getId(), sWrapperGeneralGlyph);
				listOfGeneralGlyphs.add(sWrapperGeneralGlyph.generalGlyph);
				sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);				
			}
		}		
		return listOfGeneralGlyphs;
	}
	
	/**
	 * Create multiple SBML <code>ReactionGlyph</code>s and associated 
	 * <code>Reaction</code>s from the list of SBGN <code>Glyph</code>s. 
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
	
	/**
	 * Create a SBML <code>Reaction</code> and <code>ReactionGlyph</code>, 
	 * Construct a <code>SWrapperSpeciesGlyph</code>. 
	 */		
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
		
		// Create a Reaction
		reaction = sUtil.createJsbmlReaction(reactionId);
		sOutput.addReaction(reaction);
		
		// Create a ReactionGlyph
		bbox = glyph.getBbox();
		reactionGlyph = sUtil.createJsbmlReactionGlyph(reactionId, name, clazz, reaction, true, bbox);
		sOutput.addReactionGlyph(reactionGlyph);
		// Create a temporary center Curve for the ReactionGlyph
		sUtil.createReactionGlyphCurve(reactionGlyph, glyph);
				
		sWrapperReactionGlyph = new SWrapperReactionGlyph(reaction, reactionGlyph, glyph, sWrapperModel);
		// Create all SpeciesReferenceGlyphs associated with this ReactionGlyph.
		createSpeciesReferenceGlyphs(reaction, reactionGlyph, sWrapperReactionGlyph);	
		setStartAndEndPointForCurve(sWrapperReactionGlyph);
		
		return sWrapperReactionGlyph;
	} 
		
	
	/**
	 * Create a SBML <code>SpeciesReference</code> and <code>SpeciesReferenceGlyph</code>, 
	 * Construct a <code>SWrapperSpeciesReferenceGlyph</code>. 
	 */		
	public SWrapperSpeciesReferenceGlyph createOneSpeciesReferenceGlyph(Reaction reaction, ReactionGlyph reactionGlyph,
			SWrapperArc sWrapperArc, String speciesId, Glyph speciesGlyph, String reactionId, String speciesReferenceId) {
		Curve curve;
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = sUtil.createSpeciesReference(reaction, species, speciesReferenceId);
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = sUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, sWrapperArc.arc, 
			speciesReference, speciesGlyph);
		
		// create the center Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneCurve(sWrapperArc.arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);		
		
		return new SWrapperSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, sWrapperArc);
	}


	/**
	 * Create a SBML <code>SpeciesReference</code> and <code>SpeciesReferenceGlyph</code>, 
	 * Construct a <code>SWrapperSpeciesReferenceGlyph</code>. 
	 */		
	public SWrapperModifierSpeciesReferenceGlyph createOneModifierSpeciesReferenceGlyph(Reaction reaction, ReactionGlyph reactionGlyph,
			SWrapperArc sWrapperArc, String speciesId, Glyph speciesGlyph, String reactionId, String speciesReferenceId) {
		Curve curve;
		Species species;
		ModifierSpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = sUtil.createModifierSpeciesReference(reaction, species, speciesReferenceId);
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = sUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, sWrapperArc.arc, 
			speciesReference, speciesGlyph);
		
		// create the center Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneCurve(sWrapperArc.arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);		
		
		return new SWrapperModifierSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, sWrapperArc);
	}
	
	public SWrapperArc createWrapperArc(Arc arc) {
		Object source;
		Object target;
		Glyph sourceGlyph;
		Glyph targetGlyph;	
		Port sourcePort;
		Port targetPort;
		
		String sourceSpeciesId;
		String targetSpeciesId;
		String sourceReactionId;
		String targetReactionId;
		
		source = arc.getSource();
		target = arc.getTarget();
		
		// There are 4 types of classified Arcs: 
		// the glyphToPortArcs has source from a Glyph and has target to a Port
		// the portToGlyphArcs has source from a Port and has target to a Glyph
		// the glyphToGlyphArcs has source from a Glyph and has target to a Glyph
		if (source instanceof Glyph && target instanceof Glyph){
			sourceGlyph = (Glyph) source;
			targetGlyph = (Glyph) target;	
			sourceSpeciesId = sourceGlyph.getId();
			targetSpeciesId = targetGlyph.getId();
			return new SWrapperArc(arc, "GlyphToGlyph", sourceSpeciesId, targetSpeciesId, source, target);
		}
		
		if (source instanceof Glyph && target instanceof Port){
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;	
			sourceSpeciesId = sourceGlyph.getId();
			targetReactionId = sWrapperModel.findGlyphFromPort(targetPort);	
			return new SWrapperArc(arc, "GlyphToPort", sourceSpeciesId, targetReactionId, source, target);
		}
		
		if (source instanceof Port && target instanceof Glyph){
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			sourceReactionId = sWrapperModel.findGlyphFromPort(sourcePort);		
			targetSpeciesId = targetGlyph.getId();
			return new SWrapperArc(arc, "PortToGlyph", sourceReactionId, targetSpeciesId, source, target);
		}
		
		if (source instanceof Port && target instanceof Port){
			sourcePort = (Port) source;
			targetPort = (Port) target;	
			sourceReactionId = sWrapperModel.findGlyphFromPort(sourcePort);	
			targetReactionId = sWrapperModel.findGlyphFromPort(targetPort);		
			return new SWrapperArc(arc, "PortToPort", sourceReactionId, targetReactionId, source, target);
		}
		return null;
	}
		
	
	/**
	 * Create multiple SBML <code>SpeciesReference</code>s and <code>SpeciesReferenceGlyph</code>s 
	 * from the list of SBGN <code>Arcs</code>s.
	 * Proceed to creation only when the <code>Arc</code> is associated with the provided reaction.
	 * Add the created <code>SpeciesReferenceGlyph</code>s to the reactionGlyph. 
	 */			
	public List<SWrapperSpeciesReferenceGlyph> createSpeciesReferenceGlyphs(Reaction reaction, ReactionGlyph reactionGlyph, 
			SWrapperReactionGlyph reactionGlyphTuple) {
		List<SWrapperSpeciesReferenceGlyph> listOfSWrappersSRG = new ArrayList<SWrapperSpeciesReferenceGlyph>();
		Arc arc;
		String speciesReferenceId;
		String reactionId;
		
		SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph;
		SWrapperModifierSpeciesReferenceGlyph sWrapperModifierSpeciesReferenceGlyph;
		SWrapperArc sWrapperArc;
		
		// the modifierArcs is a special case of portToGlyphArcs
		// The way they are handled is very similar, except for 
		// small parameter variations when creating SpeciesReferenceGlyphs
		for (String key: sWrapperModel.consumptionArcs.keySet()) {
			sWrapperArc = sWrapperModel.consumptionArcs.get(key);
			arc = sWrapperArc.arc;
			speciesReferenceId = key;
			reactionId = sWrapperArc.targetId;
			
			// Proceed only when the Arc is associated with the provided reaction.
			if (reactionId != reaction.getId()){ continue; } 
			else {
				// store the Arc in the Wrapper
				reactionGlyphTuple.addArc(speciesReferenceId, sWrapperArc, sWrapperArc.arcClazz);
			}
			
			// create a SpeciesReference and a SpeciesReferenceGlyph, add the SpeciesReferenceGlyph to the ReactionGlyph
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					sWrapperArc, sWrapperArc.sourceId, getGlyph(sWrapperArc.source), reactionId, speciesReferenceId);
			// add the SpeciesReference to the Reaction
			reaction.addReactant(sWrapperSpeciesReferenceGlyph.speciesReference);	
			// this is a trick to correctly set the Start and End point of the center Curve of the ReactionGlyph
			// note that this works well
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "start", 
					reactionGlyphTuple);
		
			// add the SpeciesReferenceGlyph to the SWrapperReactionGlyph
			// and add the enclosing SWrapperSpeciesReferenceGlyph to the List<SWrapperSpeciesReferenceGlyph>
			// note that the second step is optional
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph);	
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}	
		for (String key: sWrapperModel.productionArcs.keySet()) {
			sWrapperArc = sWrapperModel.productionArcs.get(key);
			arc = sWrapperArc.arc;
			speciesReferenceId = key;
			reactionId = sWrapperArc.sourceId;

			if (reactionId != reaction.getId()){ continue; } 
			else {
				// Add the Production Arc in the Wrapper
				reactionGlyphTuple.addArc(speciesReferenceId, sWrapperArc, sWrapperArc.arcClazz);
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					sWrapperArc, sWrapperArc.targetId, getGlyph(sWrapperArc.target), reactionId, speciesReferenceId);
			reaction.addProduct(sWrapperSpeciesReferenceGlyph.speciesReference);
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "end",
					reactionGlyphTuple);
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}
		// assuming target is a process node, may not be true
		// todo: check the clazz first, then decide
		// it seems that for Modifier Arcs, they point into a ProcessNode, 
		// but does not point into a Port, it just points to the whole Glyph
		// this is why we have a 'Glyph to Glyph' Arc
		// todo: target could be a reaction or a species, handle these cases differently
		for (String key: sWrapperModel.modifierArcs.keySet()) {
			sWrapperArc = sWrapperModel.modifierArcs.get(key);
			arc = sWrapperArc.arc;
			speciesReferenceId = key;
			reactionId = sWrapperArc.targetId;
			
			if (reactionId != reaction.getId()){ continue; } 
			else {
				// Add the Modifier Arc in the Wrapper
				reactionGlyphTuple.addArc(speciesReferenceId, sWrapperArc, sWrapperArc.arcClazz);
			}
			
			sWrapperModifierSpeciesReferenceGlyph = createOneModifierSpeciesReferenceGlyph(reaction, reactionGlyph,
					sWrapperArc, sWrapperArc.sourceId, getGlyph(sWrapperArc.source), reactionId, speciesReferenceId);
			reaction.addModifier(sWrapperModifierSpeciesReferenceGlyph.speciesReference);
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperModifierSpeciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperModifierSpeciesReferenceGlyph);
		}		
		
		// Note: or-simple.sbgn
		// Modifier Arcs coming out of a LogicOperator going into a ProcessNode
		// these Arcs, once created a SpeciesReference, does not have a Species to reference to.
		// i.e. these arcs points to a ReactionGlyph, not a SpeciesGlyph
		// solution:
		// if the Arc is a Modifier, and it comes out of a LogicOperator, it is part of the ReactionGlyph
		// we know the Arc is a Logic Arc, it will be part of a GeneralGlyph without associating
		// with any core Model elements. i.e. the Model is missing some arcs
		
		return listOfSWrappersSRG;
	}
				
//	/**
//	 * Obsolete. 
//	 */		
//	public boolean isModifierArcOutOfLogicOperator(Arc arc){
//		String clazz = arc.getClazz();
//		
//		if (clazz.equals("catalysis") || 
//				clazz.equals("modulation") ||
//				clazz.equals("stimulation") ||
//				clazz.equals("inhibition") ){		// todo: and many others
//			Object source = arc.getSource();
//			if (source instanceof Glyph){
//				if (sUtil.isLogicOperator(((Glyph) source).getClazz())){
//					return true;
//				}
//			} else if (source instanceof Port){
//				String reactionId = sWrapperModel.findGlyphFromPort((Port) source);
//				if (sWrapperModel.logicOperators.get(reactionId) != null){
//					return true;
//				}
//			}
//		}
//		
//		return false;
//	}
	
	/**
	 * TODO: rename method name
	 * Update the start or end <code>Point</code> of <code>ReactionGlyph</code> using values 
	 * in a <code>SpeciesReferenceGlyph</code>. 
	 */			
	public void updateReactionGlyph(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph speciesReferenceGlyph, 
			String reactionGlyphPointType, SWrapperReactionGlyph sWrapperReactionGlyph){
		Point curvePoint = null;
		Point reactionGlyphPoint = null;
		if (reactionGlyphPointType.equals("start")) {
			// for now,  we assume there is only 1 CurveSegment in this Curve, so getCurveSegment(0)
			curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(0).getEnd();
			// update the Start Point of the ReactionGlyph
			reactionGlyphPoint = reactionGlyph.getCurve().getCurveSegment(0).getStart();
		} else if (reactionGlyphPointType.equals("end")) {
			curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(0).getStart();
			// update the End Point of the ReactionGlyph
			reactionGlyphPoint = reactionGlyph.getCurve().getCurveSegment(0).getEnd();			
		}
	
		sWrapperReactionGlyph.addPoint(curvePoint);
	}
	
	void setStartAndEndPointForCurve(SWrapperReactionGlyph sWrapperReactionGlyph){
		int _nrows = sWrapperReactionGlyph.listOfEndPoints.size();
		//System.out.println("setStartAndEndPointForCurve _nrows="+_nrows);
		
	     KMeans KM = new KMeans( sWrapperReactionGlyph.listOfEndPoints, null );
	     KM.clustering(2, 10, null); // 2 clusters, maximum 10 iterations
	     //KM.printResults();
	     double[][] centroids = KM._centroids;
	     
	     // assume only 1 CurveSegment for the Curve
	     Point start = sWrapperReactionGlyph.reactionGlyph.getCurve().getCurveSegment(0).getStart();
	     Point end = sWrapperReactionGlyph.reactionGlyph.getCurve().getCurveSegment(0).getEnd();
	     
	     // arbitrary assignment of values
	     start.setX(centroids[0][0]);
	     start.setY(centroids[0][1]);
	     end.setX(centroids[1][0]);
	     end.setY(centroids[1][1]);
	}
	
	void setStartAndEndPointForCurve(List<Point> listOfEndPoints, GeneralGlyph generalGlyph){
		int _nrows = listOfEndPoints.size();
		//System.out.println("setStartAndEndPointForCurve _nrows="+_nrows);
		
	     KMeans KM = new KMeans( listOfEndPoints, null );
	     KM.clustering(2, 10, null); // 2 clusters, maximum 10 iterations
	     //KM.printResults();
	     double[][] centroids = KM._centroids;
	     
	     // assume only 1 CurveSegment for the Curve
	     Point start = new Point();
	     generalGlyph.getCurve().getCurveSegment(0).setStart(start);
	     Point end = new Point(); 
	     generalGlyph.getCurve().getCurveSegment(0).setEnd(end);
	     
	     // arbitrary assignment of values
	     start.setX(centroids[0][0]);
	     start.setY(centroids[0][1]);
	     end.setX(centroids[1][0]);
	     end.setY(centroids[1][1]);
	}	
	
	
	/**
	 * Create multiple SBML <code>CompartmentGlyph</code> and its associated
	 *  <code>Compartment</code> from list of SBGN <code>Glyph</code>. 
	 */		
	public void createCompartments() {

		for (String key: sWrapperModel.compartments.keySet()) {
			SWrapperCompartmentGlyph compartmentGlyphTuple = createOneCompartment(key);
			sWrapperModel.addSWrapperCompartmentGlyph(key, compartmentGlyphTuple);
			//System.out.println("createCompartments " + sWrapperModel.listOfWrapperCompartmentGlyphs.size());
		}
	}
	
	/**
	 * Create a SBML <code>Compartment</code> and <code>CompartmentGlyph</code>, 
	 * Construct a <code>SWrapperCompartmentGlyph</code>. 
	 */		
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
		
		// Set the Compartment order
		sUtil.setCompartmentOrder(compartmentGlyph, glyph);
		
		return new SWrapperCompartmentGlyph(compartment, compartmentGlyph, glyph);
	}	

	/**
	 * Create a <code>GeneralGlyph</code>.
	 * Construct a <code>SWrapperCompartmentGlyph</code>, set the parent that contains this <code>GeneralGlyph</code>
	 */			
	public SWrapperGeneralGlyph createOneGeneralGlyph(Glyph glyph, GraphicalObject parent, boolean setBoundingBox) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		TextGlyph textGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		text = sUtil.getText(glyph);
		clazz = glyph.getClazz();
		bbox = glyph.getBbox();
		generalGlyph = sUtil.createJsbmlGeneralGlyph(glyph.getId(), setBoundingBox, bbox);
		
		textGlyph = sUtil.createJsbmlTextGlyph(generalGlyph, text);		
		sOutput.addTextGlyph(textGlyph);

		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, glyph, parent, textGlyph, sWrapperModel);
		return sWrapperGeneralGlyph;		
	}
		
	/**
	 * Create a <code>GeneralGlyph</code> without a <code>BoundingBox</code>.
	 * Construct a <code>SWrapperCompartmentGlyph</code>.
	 */		
	public SWrapperGeneralGlyph createOneGeneralGlyph(Arc arc) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		clazz = arc.getClazz();
		// No BoundingBox
		generalGlyph = sUtil.createJsbmlGeneralGlyph(arc.getId(), false, null);

		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, arc, sWrapperModel);
		return sWrapperGeneralGlyph;		
	}	
	
	/**
	 * Create a <code>ReferenceGlyph</code> that associates with an <code>GraphicalObject</code> object.
	 */		
	public SWrapperReferenceGlyph createOneReferenceGlyph(SWrapperArc sWrapperArc, GraphicalObject object) {
		Curve curve;
		ReferenceGlyph referenceGlyph;
		
		// create a SpeciesReferenceGlyph
		referenceGlyph = sUtil.createOneReferenceGlyph(sWrapperArc.arc.getId(), sWrapperArc.arc, 
			null, object);
		
		// create the center Curve for the ReferenceGlyph
		curve = sUtil.createOneCurve(sWrapperArc.arc);
		referenceGlyph.setCurve(curve);

		return new SWrapperReferenceGlyph(referenceGlyph, sWrapperArc);
	}
	
	/**
	 * Create multiple SBML <code>GeneralGlyph</code>s from list of SBGN <code>Glyph</code>s. 
	 */		
	public void createGeneralGlyphs() {
		Arc arc;
		String referenceId;
		Curve curve;
		
		Object source;
		Object target;
		String objectId;

		ReferenceGlyph referenceGlyph;
		SpeciesGlyph speciesGlyph;
		SWrapperSpeciesGlyph sWrapperSpeciesGlyph;
		
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		SWrapperArc sWrapperArc;
		
		// create a GeneralGlyph for each Logic Arc
		for (String key: sWrapperModel.logicArcs.keySet()) {
			sWrapperArc = sWrapperModel.logicArcs.get(key);
			arc = sWrapperArc.arc;
			objectId = sWrapperArc.sourceId;
			
			// Create a GeneralGlyph without a BoundingBox, add it to sWrapperModel
			sWrapperGeneralGlyph = createOneGeneralGlyph(arc);
			sWrapperModel.addWrapperGeneralGlyph(arc.getId(), sWrapperGeneralGlyph);
			
			// Create a ReferenceGlyph
			speciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(objectId).speciesGlyph;
			sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperArc, speciesGlyph);
			// Add the ReferenceGlyph to the generalGlyph
			sWrapperGeneralGlyph.generalGlyph.addReferenceGlyph(sWrapperReferenceGlyph.referenceGlyph);
			// Add the ReferenceGlyph to the wrapper. This step is optional
			sWrapperGeneralGlyph.addSpeciesReferenceGlyph(arc.getId(), sWrapperReferenceGlyph, arc);
			
			// Add the GeneralGlyph created to the output
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
			// todo sWrapperGeneralGlyph add to sWrapperModel
		}	
		
		// todo: explanations to be added later
		for (String key: sWrapperModel.logicOperators.keySet()) {
			sWrapperSpeciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(key);

			ArrayList<Point> connectedPoints = new ArrayList<Point>();
			List<Arc> allArcs = sWrapperModel.map.getArc();
			for (Arc candidate: allArcs){

				source = candidate.getSource();
				target = candidate.getTarget();
				
				checkLogicOperatorId(connectedPoints, source, key, candidate, "source");
				checkLogicOperatorId(connectedPoints, target, key, candidate, "target");
			}
				
			//System.out.println("sWrapperModel.logicOperators" + connectedPoints.size());
			sWrapperSpeciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(key);
			sOutput.addTextGlyph(sWrapperSpeciesGlyph.textGlyph);
			// todo: SpeciesGlyph already added, need to remove it
			sOutput.addSpeciesGlyph(sWrapperSpeciesGlyph.speciesGlyph);
			GeneralGlyph generalGlyph = sUtil.createJsbmlGeneralGlyph(key, false, null);
			
			curve = new Curve();
			LineSegment curveSegment = new LineSegment();
			curveSegment.createEnd();
			curveSegment.createStart();
			curve.addCurveSegment(curveSegment);
			generalGlyph.setCurve(curve);
			
			boolean added = generalGlyph.addSubGlyph(sWrapperSpeciesGlyph.speciesGlyph);
			// todo: added = false
			System.out.println("added?"+added);
			setStartAndEndPointForCurve(connectedPoints, generalGlyph);
			sOutput.addGeneralGlyph(generalGlyph);
			
//			sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, sWrapperSpeciesGlyph.sbgnGlyph, 
//					sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.textGlyph,
//					sWrapperModel);
			// todo sWrapperGeneralGlyph add to sWrapperModel
		}
	}
	
	public Glyph getGlyph(Object source) {
		Port connectingPort;
		Glyph glyph = null;
		
		if (source instanceof Glyph){
			glyph = (Glyph) source;
		} else if (source instanceof Port){
			connectingPort = (Port) source;
			glyph = sWrapperModel.getGlyph(sWrapperModel.findGlyphFromPort(connectingPort));
		}	
		
		return glyph;
	}
	
	public void checkLogicOperatorId(ArrayList<Point> connectedPoints, Object source, String key, Arc candidate, String direction) {
		
		Glyph connectingGlyph = null;
		String arcId = candidate.getId();
		
		connectingGlyph = getGlyph(source);
		 
		if (connectingGlyph != null && connectingGlyph.getId() == key){
			
			if (sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId) != null){
				//System.out.println("sWrapperModel.logicOperators");
				if (direction.equals("source")){connectedPoints.add(sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId).speciesReferenceGlyph.getCurve().getCurveSegment(0).getStart());}
				else if (direction.equals("target")){connectedPoints.add(sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId).speciesReferenceGlyph.getCurve().getCurveSegment(0).getEnd());}
			} else if (sWrapperModel.getSWrapperReferenceGlyph(arcId) != null){
				//System.out.println("sWrapperModel.logicOperators");
				if (direction.equals("source")){connectedPoints.add(sWrapperModel.getSWrapperReferenceGlyph(arcId).referenceGlyph.getCurve().getCurveSegment(0).getStart());}
				else if (direction.equals("target")){connectedPoints.add(sWrapperModel.getSWrapperReferenceGlyph(arcId).referenceGlyph.getCurve().getCurveSegment(0).getEnd());}				
			}
		}		
	}
			
	public void storeTemplateRenderInformation() {
		sOutput.storeTemplateLocalRenderInformation(sOutput.loadTemplateFromFile());
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
		
		// Read a .sbgn file
		workingDirectory = System.getProperty("user.dir");
		sbgnFileNameInput = args[0];
		sbgnFileNameInput = workingDirectory + sbgnFileNameInput;			
		sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
				
		sbgnObject = SBGNML2SBMLUtil.readSbgnFile(sbgnFileNameInput);

		map = sbgnObject.getMap();	
		// optional
		//SBGNML2SBMLUtil.debugSbgnObject(map);
		
		// Create a new converter
		converter = new SBGNML2SBML_GSOC2017(map);
		// Load a template file containing predefined RenderInformation
		converter.storeTemplateRenderInformation();
		// Convert the file
		converter.convertToSBML();
				
		// Write converted SBML file
		SBGNML2SBMLUtil.writeSbmlFile(sbmlFileNameOutput, converter.sOutput.model);
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
