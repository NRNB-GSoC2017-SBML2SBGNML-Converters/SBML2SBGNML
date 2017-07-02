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
	// Contains all data structures needed to create the output XML document. 
	// Example: LayoutModelPlugin.
	SBGNML2SBMLOutput sOutput;
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
		
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			
			if (sUtil.isLogicArc(arc)){
				sWrapperModel.addLogicArc(id, arc);
			} else if (sUtil.isUndirectedArc(arc)) {
				sWrapperModel.addGlyphToGlyphArc(id, arc);
			} else if (sUtil.isInwardArc(arc)) {
				sWrapperModel.addGlyphToPortArc(id, arc);
			} else if (sUtil.isOutwardArc(arc)) {
				// Check if the Arc is a Modifier, handle Modifier Arcs differently
				if (sUtil.isModifierArc(arc.getClazz())){
					sWrapperModel.addModifierArcs(id, arc);
				}
				sWrapperModel.addPortToGlyphArc(id, arc);
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
		for (String key : sWrapperModel.entityPoolNodes.keySet()) {
			speciesGlyphTuple = createOneSpecies(key);
			sWrapperModel.addSWrapperSpeciesGlyph(key, speciesGlyphTuple);
		}
		
		for (String key : sWrapperModel.logicOperators.keySet()) {
			speciesGlyphTuple = createOneSpecies(key);
			sWrapperModel.addSWrapperSpeciesGlyph(key, speciesGlyphTuple);
		}
	}
	
	/**
	 * Create a SBML <code>Species</code> and <code>SpeciesGlyph</code>, 
	 * create any <code>TextGlyph</code>s for the Species. Create any <code>GeneralGlyph</code>s for the Species. 
	 * Construct a <code>SWrapperSpeciesGlyph</code>. 
	 */		
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
		// example: a Species might have Units of Information
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
	
	/**
	 * Create multiple SBML <code>GeneralGlyph</code>s from the list of SBGN <code>Glyph</code>s
	 * provided. Add the created <code>SWrapperGeneralGlyph</code>s to the sWrapperModel. 
	 */		
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
				
		sWrapperReactionGlyph = new SWrapperReactionGlyph(reaction, reactionGlyph, glyph);
		// Create all SpeciesReferenceGlyphs associated with this ReactionGlyph.
		createSpeciesReferenceGlyphs(reaction, reactionGlyph, sWrapperReactionGlyph);	
		
		return sWrapperReactionGlyph;
	} 
		
	
	/**
	 * Create a SBML <code>SpeciesReference</code> and <code>SpeciesReferenceGlyph</code>, 
	 * Construct a <code>SWrapperSpeciesReferenceGlyph</code>. 
	 */		
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
		curve = sUtil.createOneCurve(arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);		
		
		return new SWrapperSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, arc);
	}


	/**
	 * Create a SBML <code>SpeciesReference</code> and <code>SpeciesReferenceGlyph</code>, 
	 * Construct a <code>SWrapperSpeciesReferenceGlyph</code>. 
	 */		
	public SWrapperModifierSpeciesReferenceGlyph createOneModifierSpeciesReferenceGlyph(Reaction reaction, ReactionGlyph reactionGlyph,
			Arc arc, String speciesId, Glyph speciesGlyph, String reactionId, String speciesReferenceId) {
		Curve curve;
		Species species;
		ModifierSpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = sUtil.createModifierSpeciesReference(reaction, species, speciesReferenceId);
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = sUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, arc, 
			speciesReference, speciesGlyph);
		
		// create the center Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneCurve(arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);		
		
		return new SWrapperModifierSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, arc);
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
		SWrapperModifierSpeciesReferenceGlyph sWrapperModifierSpeciesReferenceGlyph;
		
		// There are 4 types of classified Arcs: 
		// the glyphToPortArcs has source from a Glyph and has target to a Port
		// the portToGlyphArcs has source from a Port and has target to a Glyph
		// the glyphToGlyphArcs has source from a Glyph and has target to a Glyph
		// the modifierArcs is a special case of portToGlyphArcs
		// The way they are handled is very similar, except for 
		// small parameter variations when creating SpeciesReferenceGlyphs
		for (String key: sWrapperModel.glyphToPortArcs.keySet()) {
			arc = sWrapperModel.glyphToPortArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			// sourceGlyph and targetPort
			sourceGlyph = (Glyph) source;
			targetPort = (Port) target;
			speciesId = sourceGlyph.getId();
			
			reactionId = sWrapperModel.findGlyphFromPort(targetPort);
			// Proceed only when the Arc is associated with the provided reaction.
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
			
			// sourcePort and targetGlyph
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			speciesId = targetGlyph.getId();
			
			// The rest is the same
			reactionId = sWrapperModel.findGlyphFromPort(sourcePort);
			if (reactionId != reaction.getId()){
				continue;
			} else {
				// Add the Production Arc in the Wrapper
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "portToGlyph");
			}
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addProduct(sWrapperSpeciesReferenceGlyph.speciesReference);
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "end");
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
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
			
			// The rest is the same
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
		for (String key: sWrapperModel.modifierArcs.keySet()) {
			arc = sWrapperModel.modifierArcs.get(key);
			speciesReferenceId = key;
			source = arc.getSource();
			target = arc.getTarget();
			
			// sourcePort and targetGlyph
			sourcePort = (Port) source;
			targetGlyph = (Glyph) target;	
			speciesId = sWrapperModel.findGlyphFromPort(sourcePort);
					
			// The rest is the same
			reactionId = targetGlyph.getId();
			if (reactionId != reaction.getId()){
				continue;
			} else {
				// Add the Modifier Arc in the Wrapper
				reactionGlyphTuple.addArc(speciesReferenceId, arc, "modifierArcs");
			}
			
			sWrapperModifierSpeciesReferenceGlyph = createOneModifierSpeciesReferenceGlyph(reaction, reactionGlyph,
					arc, speciesId, targetGlyph, reactionId, speciesReferenceId);
			reaction.addModifier(sWrapperModifierSpeciesReferenceGlyph.speciesReference);
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperModifierSpeciesReferenceGlyph.speciesReferenceGlyph);
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
				
	// obsolete
	public boolean isModifierArcOutOfLogicOperator(Arc arc){
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
	 * Create multiple SBML <code>CompartmentGlyph</code> and its associated
	 *  <code>Compartment</code> from list of SBGN <code>Glyph</code>. 
	 */		
	public void createCompartments() {

		for (String key: sWrapperModel.compartments.keySet()) {
			SWrapperCompartmentGlyph compartmentGlyphTuple = createOneCompartment(key);
			sWrapperModel.addSWrapperCompartmentGlyph(key, compartmentGlyphTuple);
			System.out.println("createCompartments " + sWrapperModel.listOfWrapperCompartmentGlyphs.size());
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
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, arc);
		return sWrapperGeneralGlyph;		
	}	
	
	/**
	 * Create a <code>ReferenceGlyph</code> that associates with an <code>GraphicalObject</code> object.
	 */		
	public SWrapperReferenceGlyph createOneReferenceGlyph(Arc arc, GraphicalObject object) {
		Curve curve;
		ReferenceGlyph referenceGlyph;
		
		// create a SpeciesReferenceGlyph
		referenceGlyph = sUtil.createOneReferenceGlyph(arc.getId(), arc, 
			null, object);
		
		// create the center Curve for the ReferenceGlyph
		curve = sUtil.createOneCurve(arc);
		referenceGlyph.setCurve(curve);

		return new SWrapperReferenceGlyph(referenceGlyph, arc);
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
			
			// Create a GeneralGlyph without a BoundingBox, add it to sWrapperModel
			sWrapperGeneralGlyph = createOneGeneralGlyph(arc);
			sWrapperModel.addWrapperGeneralGlyph(arc.getId(), sWrapperGeneralGlyph);
			
			// Create a ReferenceGlyph
			speciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(objectId).speciesGlyph;
			sWrapperReferenceGlyph = createOneReferenceGlyph(arc, speciesGlyph);
			// Add the ReferenceGlyph to the generalGlyph
			sWrapperGeneralGlyph.generalGlyph.addReferenceGlyph(sWrapperReferenceGlyph.referenceGlyph);
			// Add the ReferenceGlyph to the wrapper. This step is optional
			sWrapperGeneralGlyph.addSpeciesReferenceGlyph(arc.getId(), sWrapperReferenceGlyph.referenceGlyph, arc);
			
			// Add the GeneralGlyph created to the output
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
		
		// Read a .sbgn file
		workingDirectory = System.getProperty("user.dir");
		sbgnFileNameInput = args[0];
		sbgnFileNameInput = workingDirectory + sbgnFileNameInput;			
		sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
				
		sbgnObject = SBGNML2SBMLUtil.readSbgnFile(sbgnFileNameInput);

		map = sbgnObject.getMap();	
		// optional
		SBGNML2SBMLUtil.debugSbgnObject(map);
		
		// Create a new converter, convert the file
		converter = new SBGNML2SBML_GSOC2017(map);
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
