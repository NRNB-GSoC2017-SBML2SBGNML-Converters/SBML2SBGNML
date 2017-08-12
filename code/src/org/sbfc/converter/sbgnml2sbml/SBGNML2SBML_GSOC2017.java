package org.sbfc.converter.sbgnml2sbml;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
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
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.sbgnml2sbml.qual.SWrapperQualitativeSpecies;
import org.sbfc.converter.sbgnml2sbml.qual.SWrapperTransition;

/**
 * The SBGNML2SBML_GSOC2017 class is the primary converter. 
 * It coordinates the creation of the JSBML Model and its extensions. 
 * Model elements are added after interpreting the input libSBGN Map.
 */	
public class SBGNML2SBML_GSOC2017  extends GeneralConverter{
	// A Model wrapper that stores every Model element. 
	// Example: Species, Reaction, Compartment, etc.
	// SBGNML2SBML_GSOC2017 does not store any Model information.
	public SWrapperModel sWrapperModel;
	// Contains all data structures needed to create the output XML document. 
	// Example: LayoutModelPlugin.
	public SBGNML2SBMLOutput sOutput;
	// Contains methods that do not depend on any information in the Model. 
	// Example: finding a value from a given list.
	public SBGNML2SBMLUtil sUtil;
	// Contains methods to create the RenderInformation.
	public SBGNML2SBMLRender sRender;
	private int consumptionArcErrors = 0;
	private int productionArcErrors = 0;
		
	public SBGNML2SBML_GSOC2017(Map map) {
		sOutput = new SBGNML2SBMLOutput(3, 1, map.getLanguage());
		sUtil = new SBGNML2SBMLUtil(3, 1);
		sWrapperModel = new SWrapperModel(sOutput.getModel(), map);
		sRender = new SBGNML2SBMLRender(sWrapperModel, sOutput, sUtil);
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
		sRender.renderTextGlyphs();
		
		sOutput.completeModel();
		sOutput.removeExtraStyles();
		
		//System.out.println("getListOfLocalStyles ==>"+sOutput.renderLayoutPlugin.getLocalRenderInformation(0).getListOfLocalStyles().size());
		System.out.println("listOfGlyphs:"+listOfGlyphs.size()+" listOfArcs:"+listOfArcs.size());
		System.out.println("processNodes "+sWrapperModel.processNodes.size());
		System.out.println("compartments "+sWrapperModel.compartments.size());
		System.out.println("entityPoolNodes "+sWrapperModel.entityPoolNodes.size());
		System.out.println("logicOperators "+sWrapperModel.logicOperators.size());
		System.out.println("annotations "+sWrapperModel.annotations.size());
		
		System.out.println("logicArcs "+sWrapperModel.logicArcs.size());
		System.out.println("modifierArcs "+sWrapperModel.modifierArcs.size());
		System.out.println("consumptionArcs "+sWrapperModel.consumptionArcs.size());
		System.out.println("productionArcs "+sWrapperModel.productionArcs.size());

		System.out.println("-----");
		
		System.out.println("numOfSpecies "+sOutput.numOfSpecies );
		System.out.println("numOfSpeciesGlyphs "+sOutput.numOfSpeciesGlyphs );
		System.out.println("numOfReactions "+sOutput.numOfReactions );
		System.out.println("numOfReactionGlyphs "+sOutput.numOfReactionGlyphs );
		System.out.println("numOfSpeciesReferences "+sOutput.numOfSpeciesReferences );
		System.out.println("numOfModifierSpeciesReferences "+sOutput.numOfModifierSpeciesReferences );
		System.out.println("numOfSpeciesReferenceGlyphs "+sOutput.numOfSpeciesReferenceGlyphs );
		System.out.println("numOfCompartments "+sOutput.numOfCompartments );
		System.out.println("numOfCompartmentGlyphs "+sOutput.numOfCompartmentGlyphs );
		System.out.println("numOfTextGlyphs "+sOutput.numOfTextGlyphs );
		System.out.println("numOfGeneralGlyphs "+sOutput.numOfGeneralGlyphs );
		System.out.println("numOfAdditionalGraphicalObjects "+sOutput.numOfAdditionalGraphicalObjects );
		System.out.println("numOfReferenceGlyphs "+sOutput.numOfReferenceGlyphs );
		
		System.out.println("-----");
		
		System.out.println("consumptionArcErrors "+consumptionArcErrors );
		System.out.println("productionArcErrors "+productionArcErrors );
		System.out.println("numOfSpeciesReferenceGlyphErrors "+sOutput.numOfSpeciesReferenceGlyphErrors );
		System.out.println("createOneCurveError "+sUtil.createOneCurveError );
	
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
			} else if (sUtil.isTag(clazz)) {
				sWrapperModel.addSbgnLogicOperator(id, glyph);
			} else if (sUtil.isAnnotation(clazz)){
				sWrapperModel.addAnnotation(id, glyph);
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
				// todo: check in QualitativeSpecies too
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
		
		List<Arc> allArcs = sWrapperModel.map.getArc();
		//System.out.println(Arrays.toString(sWrapperModel.portGlyphMap.keySet().toArray()));
		for (String key : sWrapperModel.logicOperators.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			
			boolean createReactionGlyph = true;
						
			for (Arc candidate: allArcs){

				Object source = candidate.getSource();
				Object target = candidate.getTarget();
				
				// todo: move tag to separate function
				Glyph connectingGlyph1 = getGlyph(source);
				Glyph connectingGlyph2 = getGlyph(target);

				if (!(connectingGlyph1.getId().equals(key)) && !(connectingGlyph2.getId().equals(key))){continue;}
				else if (connectingGlyph1.getId().equals(key)){
					if (sUtil.isLogicOperator(connectingGlyph2.getClazz())){createReactionGlyph = false; break;}
				}
				else if (connectingGlyph2.getId().equals(key)){
					if (sUtil.isLogicOperator(connectingGlyph1.getClazz())){createReactionGlyph = false; break;}
				}
			}
			
//			if (createReactionGlyph){
//				SWrapperReactionGlyph sWrapperReactionGlyph;
//
//				//glyph = sWrapperModel.processNodes.get(key);
//				sWrapperReactionGlyph =  createOneReactionGlyph(glyph);
//				sWrapperModel.addSWrapperReactionGlyph(key, sWrapperReactionGlyph);				
//			} else {
				speciesGlyphTuple = createOneSpecies(glyph);
				
				Bbox bb = glyph.getBbox();
				sWrapperModel.addSWrapperSpeciesGlyph(key, speciesGlyphTuple);				
//			}

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
		textGlyph = null;
		Bbox labelBbox = null;
		if (glyph.getLabel() != null && glyph.getLabel().getBbox() != null){
			//System.out.format("createOneSpecies glyph.getLabel().getBbox() != null id=%s text=%s \n", glyph.getId(), glyph.getLabel().getText());
			labelBbox = glyph.getLabel().getBbox();
		}
		
		if (sUtil.isLogicOperator(clazz)){
			textGlyph = sUtil.createJsbmlTextGlyph(speciesGlyph, clazz.toUpperCase(), labelBbox);
		} else if (clazz.equals("source and sink")) {
			textGlyph = null;
		} else {
			textGlyph = sUtil.createJsbmlTextGlyph(species, speciesGlyph, labelBbox);
		}
		sOutput.addTextGlyph(textGlyph);
		
		if (glyph.getGlyph().size() != 0){
			sWrapperModel.textSourceMap.put(textGlyph.getId(), glyph.getId());
		}
		
		// create a new SWrapperSpeciesGlyph class, store a list of GeneralGlyphs if present
		speciesGlyphTuple =  new SWrapperSpeciesGlyph(species, speciesGlyph, glyph, textGlyph);
		speciesGlyphTuple.setListOfNestedGlyphs(listOfGeneralGlyphs);
		
		String text = sUtil.getClone(glyph);
		if (text != null){speciesGlyphTuple.setCloneText(text);}
		//System.out.format("createOneSpecies clone=%s text=%s \n", text != null ? "yes" : "no", text);
		
		
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
			if (sUtil.isEntityPoolNode(glyph.getClazz()) || (glyph.getClazz().equals("terminal"))){
				sWrapperSpeciesGlyph = createOneSpecies(glyph);
				//System.out.println("createNestedGlyphs"+sWrapperSpeciesGlyph.speciesGlyph.getId());
//				sOutput.addTextGlyph(sWrapperSpeciesGlyph.textGlyph);
//				sOutput.addSpeciesGlyph(sWrapperSpeciesGlyph.speciesGlyph);
				
				sWrapperModel.addSWrapperSpeciesGlyph(glyph.getId(), sWrapperSpeciesGlyph);
				sUtil.addAnnotation(sWrapperSpeciesGlyph.species, parent.getId(), Qualifier.BQB_IS_PART_OF);
				
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
		SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph;
		
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = sUtil.createSpeciesReference(reaction, species, speciesReferenceId);
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = sUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, sWrapperArc.arc, 
			speciesReference, speciesGlyph, sOutput);
		
		// create the Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneCurve(sWrapperArc.arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		
		sOutput.addSpeciesReferenceGlyph(reactionGlyph, speciesReferenceGlyph);
		
		sWrapperSpeciesReferenceGlyph = new SWrapperSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, sWrapperArc);
		
		// if the Arc contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (sWrapperArc.arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = sWrapperArc.arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs(nestedGlyphs, speciesReferenceGlyph);
			sWrapperSpeciesReferenceGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
		
		addSourceTargetToAnnotation(speciesReferenceGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);
		
		return sWrapperSpeciesReferenceGlyph;
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
		SWrapperModifierSpeciesReferenceGlyph sWrapperModifierSpeciesReferenceGlyph;
		
		// todo: <!-- assumption: start=source, end=target-->
		
		
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = sUtil.createModifierSpeciesReference(reaction, species, speciesReferenceId);
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = sUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, sWrapperArc.arc, 
			speciesReference, speciesGlyph, sOutput);
		
		// create the center Curve for the SpeciesReferenceGlyph
		curve = sUtil.createOneCurve(sWrapperArc.arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		//reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);		
		sOutput.addSpeciesReferenceGlyph(reactionGlyph, speciesReferenceGlyph);
		
		sWrapperModifierSpeciesReferenceGlyph = new SWrapperModifierSpeciesReferenceGlyph(speciesReference, 
													speciesReferenceGlyph, sWrapperArc);
		
		// if the Arc contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (sWrapperArc.arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = sWrapperArc.arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs(nestedGlyphs, speciesReferenceGlyph);
			sWrapperModifierSpeciesReferenceGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
		
		addSourceTargetToAnnotation(speciesReferenceGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);
		
		return sWrapperModifierSpeciesReferenceGlyph;
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
				reactionGlyphTuple.addArc(speciesReferenceId, sWrapperArc, "consumption");
				//System.out.println("===createSpeciesReferenceGlyphs "+reactionGlyphTuple.consumptionArcs.size());
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
				reactionGlyphTuple.addArc(speciesReferenceId, sWrapperArc, "production");
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
				reactionGlyphTuple.addArc(speciesReferenceId, sWrapperArc, "modifierArcs");
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
			// we assume the last CurveSegment in this Curve touches the ReactionGlyph
			int count = speciesReferenceGlyph.getCurve().getCurveSegmentCount();
			
			if (count != 0){
				curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(count - 1).getEnd();
				sWrapperReactionGlyph.addStartPoint(curvePoint);				
			} else {
				System.out.println("! updateReactionGlyph addStartPoint count="+count+" reactionGlyph id="+reactionGlyph.getId()+" speciesReferenceGlyph id="+speciesReferenceGlyph.getId());
				consumptionArcErrors++;
			}

			// update the Start Point of the ReactionGlyph
			//reactionGlyphPoint = reactionGlyph.getCurve().getCurveSegment(0).getStart();
		} else if (reactionGlyphPointType.equals("end")) {
			int count = speciesReferenceGlyph.getCurve().getCurveSegmentCount();
			
			if (count != 0){
				curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(0).getStart();
				sWrapperReactionGlyph.addEndPoint(curvePoint);
			} else {
				//System.out.println("! updateReactionGlyph addEndPoint count="+count+" reactionGlyph id="+reactionGlyph.getId()+" speciesReferenceGlyph id="+speciesReferenceGlyph.getId());
				productionArcErrors++;
			}
				
			// update the End Point of the ReactionGlyph
			//reactionGlyphPoint = reactionGlyph.getCurve().getCurveSegment(0).getEnd();			
		}
	
	}
	
	void setStartAndEndPointForCurve(SWrapperReactionGlyph sWrapperReactionGlyph){
		int _nrows = sWrapperReactionGlyph.listOfEndPoints.size();
		//System.out.println("setStartAndEndPointForCurve _nrows="+_nrows);
		
//	     KMeans KM = new KMeans( sWrapperReactionGlyph.listOfEndPoints, null );
	     
	     //System.out.println("setStartAndEndPointForCurve id="+sWrapperReactionGlyph.reactionId+" listOfEndPoints "+sWrapperReactionGlyph.listOfEndPoints.size());
	     if (sWrapperReactionGlyph.listOfEndPoints.size() == 0){return;}
	     
//	     KM.clustering(2, 10, null); // 2 clusters, maximum 10 iterations
//	     //KM.printResults();
//	     double[][] centroids = KM._centroids;
//	     
//	     // assume only 1 CurveSegment for the Curve
//	     Point start = sWrapperReactionGlyph.reactionGlyph.getCurve().getCurveSegment(0).getStart();
//	     Point end = sWrapperReactionGlyph.reactionGlyph.getCurve().getCurveSegment(0).getEnd();
//	     
//	     // arbitrary assignment of values
//	     start.setX(centroids[0][0]);
//	     start.setY(centroids[0][1]);
//	     end.setX(centroids[1][0]);
//	     end.setY(centroids[1][1]);
	     
	     ReactionGlyph reactionGlyph = sWrapperReactionGlyph.reactionGlyph;
	     
	     Point topLeftPoint = reactionGlyph.getBoundingBox().getPosition();
	     Point centerPoint = new Point();
	     centerPoint.setX(topLeftPoint.getX() + reactionGlyph.getBoundingBox().getDimensions().getWidth()/2);
	     centerPoint.setY(topLeftPoint.getY() + reactionGlyph.getBoundingBox().getDimensions().getHeight()/2);
	    		 
	     for (Point p : sWrapperReactionGlyph.listOfEndPoints){
	    	 CurveSegment cs = new LineSegment();
	    	 Point start = new Point();
	    	 Point end = new Point(); 
	    	 start.setX(centerPoint.getX());
	    	 start.setY(centerPoint.getY());
	    	 end.setX(p.getX());
	    	 end.setY(p.getY());
	    	 cs.setStart(start);
	    	 cs.setEnd(end);
	    	 reactionGlyph.getCurve().addCurveSegment(cs);
	     }
	     
	     for (Point p : sWrapperReactionGlyph.listOfStartPoints){
	    	 CurveSegment cs = new LineSegment();
	    	 Point start = new Point();
	    	 Point end = new Point(); 
	    	 end.setX(centerPoint.getX());
	    	 end.setY(centerPoint.getY());
	    	 start.setX(p.getX());
	    	 start.setY(p.getY());
	    	 cs.setStart(start);
	    	 cs.setEnd(end);
	    	 reactionGlyph.getCurve().addCurveSegment(cs);
	     }
	}
	
	void setStartAndEndPointForCurve(List<Point> listOfEndPoints, GeneralGlyph generalGlyph){
		int _nrows = listOfEndPoints.size();
		//System.out.println("setStartAndEndPointForCurve _nrows="+_nrows);
		
//	     KMeans KM = new KMeans( listOfEndPoints, null );
	     
	     //System.out.println("[] setStartAndEndPointForCurve id="+generalGlyph.getId()+" listOfEndPoints "+listOfEndPoints.size());
	     if (listOfEndPoints.size() == 0){return;}
	     
//	     KM.clustering(2, 10, null); // 2 clusters, maximum 10 iterations
//	     //KM.printResults();
//	     double[][] centroids = KM._centroids;
	     
	     // assume only 1 CurveSegment for the Curve
//	     Point start = new Point();
//	     generalGlyph.getCurve().getCurveSegment(0).setStart(start);
//	     Point end = new Point(); 
//	     generalGlyph.getCurve().getCurveSegment(0).setEnd(end);
//	     
	     // arbitrary assignment of values
//	     start.setX(centroids[0][0]);
//	     start.setY(centroids[0][1]);
//	     end.setX(centroids[1][0]);
//	     end.setY(centroids[1][1]);
	     
	     Point topLeftPoint = generalGlyph.getBoundingBox().getPosition();
	     Point centerPoint = new Point();
	     centerPoint.setX(topLeftPoint.getX() + generalGlyph.getBoundingBox().getDimensions().getWidth()/2);
	     centerPoint.setY(topLeftPoint.getY() + generalGlyph.getBoundingBox().getDimensions().getHeight()/2);
	    		 
	     for (Point p : listOfEndPoints){
	    	 CurveSegment cs = new LineSegment();
	    	 Point start = new Point();
	    	 Point end = new Point(); 
	    	 start.setX(centerPoint.getX());
	    	 start.setY(centerPoint.getY());
	    	 end.setX(p.getX());
	    	 end.setY(p.getY());
	    	 cs.setStart(start);
	    	 cs.setEnd(end);
	    	 generalGlyph.getCurve().addCurveSegment(cs);
	     }
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
		
		Bbox labelBbox = glyph.getLabel().getBbox();
		TextGlyph textGlyph = sUtil.createJsbmlTextGlyph(compartmentGlyph, glyph.getLabel().getText(), labelBbox);
		sWrapperModel.textSourceMap.put(textGlyph.getId(), key);

		sOutput.addTextGlyph(textGlyph);
		
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
		
		
		Bbox labelBbox = null;
		if (glyph.getLabel() != null){
			labelBbox = glyph.getLabel().getBbox();
		}
		textGlyph = sUtil.createJsbmlTextGlyph(generalGlyph, text, labelBbox);		
		sOutput.addTextGlyph(textGlyph);

		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, glyph, parent, textGlyph, sWrapperModel);
		sUtil.addAnnotation(generalGlyph, parent.getId(), Qualifier.BQB_IS_PART_OF);
		//System.out.println(sWrapperGeneralGlyph.glyph);
		return sWrapperGeneralGlyph;		
	}
		
	public void addSourceTargetToAnnotation(GraphicalObject graphicObject, String sourceId, String targetId){
		Annotation annotation =graphicObject.getAnnotation();
		CVTerm cvTerm = new CVTerm(Type.BIOLOGICAL_QUALIFIER, Qualifier.BQB_HAS_PROPERTY);
		// source
		cvTerm.addResource(sourceId);
		// target
		cvTerm.addResource(targetId);
		annotation.addCVTerm(cvTerm);		
	}
	
	
	/**
	 * Create a <code>GeneralGlyph</code> without a <code>BoundingBox</code>.
	 * Construct a <code>SWrapperCompartmentGlyph</code>.
	 */		
	public SWrapperGeneralGlyph createOneGeneralGlyph(SWrapperArc sWrapperArc) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		Arc arc = sWrapperArc.arc;
		clazz = arc.getClazz();
		// No BoundingBox
		generalGlyph = sUtil.createJsbmlGeneralGlyph(arc.getId(), false, null);
		
		
		addSourceTargetToAnnotation(generalGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);

		
		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, arc, sWrapperModel);
		
		// if the Arc contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs(nestedGlyphs, generalGlyph);
			sWrapperGeneralGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
						
		return sWrapperGeneralGlyph;		
	}	
	
	/**
	 * Create a <code>ReferenceGlyph</code> that associates with an <code>GraphicalObject</code> object.
	 */		
	public SWrapperReferenceGlyph createOneReferenceGlyph(SWrapperArc sWrapperArc, SBase object) {
		Curve curve;
		ReferenceGlyph referenceGlyph;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		
		// todo: assume: ReferenceGlyph.glyph is species, ReferenceGlyph.reference is the "reaction" (not the speciesReference)
		
		// create a SpeciesReferenceGlyph
		referenceGlyph = sUtil.createOneReferenceGlyph(sWrapperArc.arc.getId(), sWrapperArc.arc, 
			null, object);
		
		// create the center Curve for the ReferenceGlyph
		curve = sUtil.createOneCurve(sWrapperArc.arc);
		referenceGlyph.setCurve(curve);
		
		sWrapperReferenceGlyph = new SWrapperReferenceGlyph(referenceGlyph, sWrapperArc);
		
		// if the Arc contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (sWrapperArc.arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = sWrapperArc.arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs(nestedGlyphs, referenceGlyph);
			sWrapperReferenceGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
		
		addSourceTargetToAnnotation(referenceGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);

		return sWrapperReferenceGlyph;
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
		
		for (String key: sWrapperModel.annotations.keySet()) {
			Glyph glyph = sWrapperModel.annotations.get(key);
			
			sWrapperGeneralGlyph = createOneGeneralGlyph(glyph, null, true);
			sWrapperGeneralGlyph.isAnnotation = true;
			Glyph calloutGlyph = (Glyph) glyph.getCallout().getTarget();
			sWrapperGeneralGlyph.calloutTarget = calloutGlyph.getId();
			org.sbgn.bindings.Point calloutPoint = glyph.getCallout().getPoint();
			sWrapperGeneralGlyph.calloutPoint = new Point(calloutPoint.getX(), calloutPoint.getY());
			//System.out.println(sWrapperGeneralGlyph.calloutPoint.getX() + " " + sWrapperGeneralGlyph.calloutPoint.getY());
			
			sWrapperModel.addWrapperGeneralGlyph(glyph.getId(), sWrapperGeneralGlyph);
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
		}
		
		// create a GeneralGlyph for each Logic Arc
		for (String key: sWrapperModel.logicArcs.keySet()) {
			sWrapperArc = sWrapperModel.logicArcs.get(key);
			arc = sWrapperArc.arc;
			objectId = sWrapperArc.sourceId;
			
			// Create a GeneralGlyph without a BoundingBox, add it to sWrapperModel
			sWrapperGeneralGlyph = createOneGeneralGlyph(sWrapperArc);
			sWrapperModel.addWrapperGeneralGlyph(arc.getId(), sWrapperGeneralGlyph);
			
			// Create a ReferenceGlyph
			speciesGlyph = null;
			try{
			speciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(objectId).speciesGlyph;
			} catch (Exception e){System.out.println("speciesGlyph objectId "+objectId); }
			sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperArc, speciesGlyph);
			// Add the ReferenceGlyph to the generalGlyph
			sOutput.addReferenceGlyph(sWrapperGeneralGlyph.generalGlyph, sWrapperReferenceGlyph.referenceGlyph);
			// Add the ReferenceGlyph to the wrapper. This step is optional
			sWrapperGeneralGlyph.addSpeciesReferenceGlyph(arc.getId(), sWrapperReferenceGlyph, sWrapperArc);
			
			// Add the GeneralGlyph created to the output
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
			// todo sWrapperGeneralGlyph add to sWrapperModel
		}	
		
		// todo: explanations to be added later
		for (String key: sWrapperModel.logicOperators.keySet()) {
			sWrapperSpeciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(key);
			
			if (sWrapperSpeciesGlyph == null){continue;}

			ArrayList<Point> connectedPoints = new ArrayList<Point>();
			List<Arc> allArcs = sWrapperModel.map.getArc();
			
			Arc tagArc = null;
			
			for (Arc candidate: allArcs){

				source = candidate.getSource();
				target = candidate.getTarget();
				
				// todo: move tag to separate function
				tagArc = checkLogicOperatorId(connectedPoints, source, key, candidate, "source");
				tagArc = checkLogicOperatorId(connectedPoints, target, key, candidate, "target");
			}
				
			//System.out.println("sWrapperModel.logicOperators" + connectedPoints.size());
			//sWrapperSpeciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(key);
			//sOutput.addTextGlyph(sWrapperSpeciesGlyph.textGlyph);
			// todo: SpeciesGlyph already added, need to remove it
			//sOutput.addSpeciesGlyph(sWrapperSpeciesGlyph.speciesGlyph);
			//Glyph g = sWrapperSpeciesGlyph.sbgnGlyph;
			//System.out.println("sWrapperSpeciesGlyph = "+ sWrapperSpeciesGlyph.id);
			//System.out.println("sWrapperSpeciesGlyph = "+ sWrapperSpeciesGlyph.sbgnGlyph.getId());
			GeneralGlyph generalGlyph = sUtil.createJsbmlGeneralGlyph(key, true, sWrapperSpeciesGlyph.sbgnGlyph.getBbox());
			
			curve = new Curve();
			LineSegment curveSegment = new LineSegment();
			curveSegment.createEnd();
			curveSegment.createStart();
			curve.addCurveSegment(curveSegment);
			generalGlyph.setCurve(curve);
			
			//boolean added = generalGlyph.addSubGlyph(sWrapperSpeciesGlyph.speciesGlyph);
			// todo: added = false
			//System.out.println("added?"+added);
			
			if (sWrapperSpeciesGlyph.clazz.contains("tag")){			
				curve = sUtil.createOneCurve(tagArc);
				generalGlyph.setCurve(curve);
			}
			else {setStartAndEndPointForCurve(connectedPoints, generalGlyph);}
			
			sOutput.addGeneralGlyph(generalGlyph);
			
//			sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, sWrapperSpeciesGlyph.sbgnGlyph, 
//					sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.textGlyph,
//					sWrapperModel);
			// todo sWrapperGeneralGlyph add to sWrapperModel
		}
		
	}
	
	public Glyph getGlyph(Object source) {
		Port connectingPort = null;
		Glyph glyph = null;
		
		if (source instanceof Glyph){
			glyph = (Glyph) source;
		} else if (source instanceof Port){
			connectingPort = (Port) source;
			glyph = sWrapperModel.getGlyph(sWrapperModel.findGlyphFromPort(connectingPort));
		}	
		
		if(glyph == null){System.out.println("getGlyph null " + connectingPort.getId() + sWrapperModel.findGlyphFromPort(connectingPort));} 
		
		return glyph;
	}
	
	public Arc checkLogicOperatorId(ArrayList<Point> connectedPoints, Object source, String key, Arc candidate, String direction) {
		
		if (connectedPoints == null){
			connectedPoints = new ArrayList<Point>();
		}
		
		Glyph connectingGlyph = null;
		String arcId = candidate.getId();
		
		connectingGlyph = getGlyph(source);
		 
		if (connectingGlyph != null && connectingGlyph.getId() == key){
			
			if (sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId) != null){
				//System.out.println("sWrapperModel.logicOperators");
				if (direction.equals("source")){
					connectedPoints.add(sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId).speciesReferenceGlyph.getCurve().getCurveSegment(0).getStart());
					
				}
				else if (direction.equals("target")){
					connectedPoints.add(sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId).speciesReferenceGlyph.getCurve().getCurveSegment(0).getEnd());
					
				}
			} else if (sWrapperModel.getSWrapperReferenceGlyph(arcId) != null){
				//System.out.println("sWrapperModel.logicOperators");
				if (direction.equals("source")){
					connectedPoints.add(sWrapperModel.getSWrapperReferenceGlyph(arcId).referenceGlyph.getCurve().getCurveSegment(0).getStart());
				}
				else if (direction.equals("target")){
					connectedPoints.add(sWrapperModel.getSWrapperReferenceGlyph(arcId).referenceGlyph.getCurve().getCurveSegment(0).getEnd());
				}				
			}
		}	
		
		return null;
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
	
	public SWrapperQualitativeSpecies createOneQualitativeSpecies(Glyph glyph) {
		QualitativeSpecies qualitativeSpecies;
		SpeciesGlyph speciesGlyph;
		String speciesId;
		String name;
		String clazz; 
		Bbox bbox;
		TextGlyph textGlyph;
		//List<Glyph> nestedGlyphs;	
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		//List<GraphicalObject> listOfGeneralGlyphs = null;
		
		name = sUtil.getText(glyph);
		clazz = glyph.getClazz();
		speciesId = glyph.getId();
		
		// create a Species, add it to the output
		qualitativeSpecies = sUtil.createQualitativeSpecies(speciesId, name, clazz, false, true);
		sOutput.addQualitativeSpecies(qualitativeSpecies);
		
		// create a SpeciesGlyph, add it to the output 
		bbox = glyph.getBbox();
		speciesGlyph = sUtil.createJsbmlSpeciesGlyph(speciesId, name, clazz, null, true, bbox);
		sOutput.addSpeciesGlyph(speciesGlyph);
		
		// if the Glyph contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: a Species might have Units of Information
//		if (glyph.getGlyph().size() != 0){
//			nestedGlyphs = glyph.getGlyph();
//			listOfGeneralGlyphs = createNestedGlyphs(nestedGlyphs, speciesGlyph);
//		} 
		
		// create TextGlyph for the SpeciesGlyph
		Bbox labelBbox = glyph.getLabel().getBbox();
		textGlyph = sUtil.createJsbmlTextGlyph(qualitativeSpecies, speciesGlyph, labelBbox);
		sOutput.addTextGlyph(textGlyph);
		
		// create a new SWrapperSpeciesGlyph class, store a list of GeneralGlyphs if present
		sWrapperQualitativeSpecies =  new SWrapperQualitativeSpecies(qualitativeSpecies, speciesGlyph, glyph, textGlyph);
		//sWrapperQualitativeSpecies.setListOfNestedGlyphs(listOfGeneralGlyphs);
		
		return sWrapperQualitativeSpecies;
	}	
	
	public void createQualitativeSpecies(){
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		Glyph glyph;
		for (String key : sWrapperModel.entityPoolNodes.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			sWrapperQualitativeSpecies = createOneQualitativeSpecies(glyph);
			sWrapperModel.addSWrapperQualitativeSpecies(key, sWrapperQualitativeSpecies);

		}
		//System.out.println(sWrapperModel.listOfSWrapperQualitativeSpecies);
			
	}
		
	// special case
	public SWrapperTransition createOneTransition(SWrapperArc sWrapperArc) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		
		String sourceId;
		String targetId;
		Transition transition;
		SWrapperTransition sWrapperTransition;
					
		clazz = sWrapperArc.arcClazz;
		sourceId = sWrapperArc.sourceId;
		targetId = sWrapperArc.targetId;
		
		// check that the conditions for this special case holds
		if (!(sWrapperModel.getSWrapperQualitativeSpecies(sourceId) != null && 
				sWrapperModel.getSWrapperQualitativeSpecies(targetId) != null)){
			return null;
		}
		
		// No BoundingBox
		transition = sUtil.createTransition(sWrapperArc.arcId, 
				sWrapperModel.getSWrapperQualitativeSpecies(sourceId).qualitativeSpecies,
				sWrapperModel.getSWrapperQualitativeSpecies(targetId).qualitativeSpecies);
		sOutput.addTransition(transition);

		
		sWrapperGeneralGlyph = createOneGeneralGlyph(sWrapperArc);
		
		// Create a ReferenceGlyph
		sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperArc, sWrapperModel.getSWrapperQualitativeSpecies(sourceId).qualitativeSpecies);
		// Add the ReferenceGlyph to the generalGlyph
		sOutput.addReferenceGlyph(sWrapperGeneralGlyph.generalGlyph, sWrapperReferenceGlyph.referenceGlyph);
		//sWrapperGeneralGlyph.generalGlyph.addReferenceGlyph(sWrapperReferenceGlyph.referenceGlyph);
		// Add the ReferenceGlyph to the wrapper. This step is optional
		sWrapperGeneralGlyph.addSpeciesReferenceGlyph(sWrapperArc.arc.getId(), sWrapperReferenceGlyph, sWrapperArc);
		
		// Add the GeneralGlyph created to the output
		sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
		// todo sWrapperGeneralGlyph add to sWrapperModel		
		
		
		// create a new SWrapperGeneralGlyph, add it to the SWrapperModel
		//System.out.println(sWrapperGeneralGlyph.id);
		sWrapperTransition = new SWrapperTransition(transition, sWrapperGeneralGlyph, sWrapperArc, sWrapperModel);
		
		return sWrapperTransition;	
	} 	
	
	public SWrapperTransition createOneTransition(Glyph logicOperator, SWrapperGeneralGlyph sWrapperGeneralGlyph){
		String clazz;
		String id;
		Transition transition;
		SWrapperTransition sWrapperTransition;	
		
		clazz = logicOperator.getClazz();
		id = logicOperator.getId();
		transition = sUtil.createTransition(id);
		sOutput.addTransition(transition);		
		sWrapperTransition = new SWrapperTransition(id, transition, sWrapperGeneralGlyph, logicOperator, sWrapperModel);
		
		return sWrapperTransition;
	}
	
	public int createTransitions() {
		Arc arc;

		String objectId;

		SpeciesGlyph speciesGlyph;
		
		SWrapperTransition sWrapperTransition;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		SWrapperArc sWrapperArc;
		
		Glyph glyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		
		for (String key : sWrapperModel.logicOperators.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			sWrapperGeneralGlyph = createOneGeneralGlyph(glyph, null, true);
			sWrapperModel.listOfWrapperGeneralGlyphs.put(key, sWrapperGeneralGlyph);
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
		}			
		
		for (String key: sWrapperModel.modifierArcs.keySet()) {
			sWrapperArc = sWrapperModel.modifierArcs.get(key);
			arc = sWrapperArc.arc;
			
			// Check if the Arc is not connected to a Logic Operator, proceed if not connected
			sWrapperTransition = createOneTransition(sWrapperArc);
			if (sWrapperTransition != null){
				sWrapperModel.addSWrapperTransition(arc.getId(), sWrapperTransition);
			} else{
				
				objectId = sWrapperArc.targetId;
				sWrapperQualitativeSpecies = sWrapperModel.listOfSWrapperQualitativeSpecies.get(objectId);
				sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperArc, sWrapperQualitativeSpecies.speciesGlyph);
				sWrapperModel.listOfWrapperReferenceGlyphs.put(key, sWrapperReferenceGlyph);
				sOutput.addGraphicalObject(sWrapperReferenceGlyph.referenceGlyph);
			}			
		}
		
		// create a GeneralGlyph for each Logic Arc
		for (String key: sWrapperModel.logicArcs.keySet()) {
			sWrapperArc = sWrapperModel.logicArcs.get(key);
			arc = sWrapperArc.arc;
			// assume Arc.Source corresponds to Arc.Start
			objectId = sWrapperArc.sourceId;
			//objectId = sWrapperArc.targetId;
			
			sWrapperGeneralGlyph = sWrapperModel.listOfWrapperGeneralGlyphs.get(objectId);
			if (sWrapperGeneralGlyph != null){
				sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperArc, sWrapperGeneralGlyph.generalGlyph);
				
			} else {
				sWrapperQualitativeSpecies = sWrapperModel.listOfSWrapperQualitativeSpecies.get(objectId);
				sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperArc, sWrapperQualitativeSpecies.speciesGlyph);
								
			}
			
			sWrapperModel.listOfWrapperReferenceGlyphs.put(key, sWrapperReferenceGlyph);
			sOutput.addGraphicalObject(sWrapperReferenceGlyph.referenceGlyph);
		}
		// Create a Transition for a GeneralGlyph
		// If connected to a Logic Operator, should not create a new Transition or new GeneralGlyph, add to existing
		//sWrapperTransition.addReferenceGlyph
		//System.out.println(sWrapperModel.listOfWrapperGeneralGlyphs);
		//System.out.println(sWrapperModel.listOfWrapperReferenceGlyphs);
				
		return sWrapperModel.listOfWrapperGeneralGlyphs.size() + sWrapperModel.listOfWrapperReferenceGlyphs.size();
	}
	
	public void createCompleteTransitions(){
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs = 
				new HashMap<String, SWrapperGeneralGlyph>();
		HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs = 
				new HashMap<String, SWrapperReferenceGlyph>();
		SWrapperTransition sWrapperTransition;
		
		for (String key: sWrapperModel.listOfWrapperGeneralGlyphs.keySet()) {
			sWrapperGeneralGlyph = listOfWGeneralGlyphs.get(key);
			if (sWrapperGeneralGlyph != null){
				continue;
			}
			sWrapperGeneralGlyph = sWrapperModel.listOfWrapperGeneralGlyphs.get(key);
			createOneCompleteTransition(sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
		}
		
		for (String key: sWrapperModel.listOfSWrapperTransitions.keySet()) {
			sWrapperTransition = sWrapperModel.listOfSWrapperTransitions.get(key);
			
			//System.out.println(sWrapperTransition.id);
			//System.out.println(sWrapperTransition.inputs);
			//System.out.println(sWrapperTransition.outputs+ "\n");
			
			HashMap<String, ASTNode> leaves = new HashMap<String, ASTNode>();
			
			for (String inputId : sWrapperTransition.inputs.keySet()){
				SWrapperQualitativeSpecies input = sWrapperTransition.inputs.get(inputId);
				Input tInput = sUtil.addInputToTransition(sWrapperTransition.transition, input.qualitativeSpecies);
				
				// create a child node wrapper
				ASTNode functionTermMath = new ASTNode(tInput.getId());
				leaves.put(inputId, functionTermMath);
			}
			
			for (String outputId : sWrapperTransition.outputs.keySet()){
				SWrapperQualitativeSpecies output = sWrapperTransition.outputs.get(outputId);
				sUtil.addOutputToTransition(sWrapperTransition.transition, output.qualitativeSpecies);
			}
			
			int resultLevel = 0;
			if (sWrapperTransition.outputClazz.equals("necessary stimulation")){
				resultLevel = 1;
			} // ...
			
			sUtil.addFunctionTermToTransition(sWrapperTransition.transition, true, resultLevel == 1 ? 0 : 1);	
			FunctionTerm functionTerm;
			functionTerm = sUtil.addFunctionTermToTransition(sWrapperTransition.transition, false, resultLevel == 1 ? 1 : 0);
			
			// get the logicOperator that immediately precedes the output Arc
			String rootId = sWrapperTransition.outputModifierArc.sWrapperArc.sourceId;
			String rootClazz = sWrapperTransition.listOfWrapperGeneralGlyphs.get(rootId).clazz;
			// create the root node, set its container
			ASTNode rootNode = sUtil.createMath(rootClazz, functionTerm);
			sWrapperTransition.rootFunctionTerm = rootNode;
			
//			for (String generalGlyphId: sWrapperTransition.listOfWrapperGeneralGlyphs.keySet()) {
//				sWrapperGeneralGlyph = sWrapperTransition.listOfWrapperGeneralGlyphs.get(key);
//				System.out.println("sWrapperGeneralGlyph.referenceGlyphs");
//				System.out.println(sWrapperGeneralGlyph.referenceGlyphs);
//			
//			}
			
			// get the root's children. first, find all the Arcs connected to the root
			sWrapperGeneralGlyph = sWrapperTransition.listOfWrapperGeneralGlyphs.get(rootId);
			buildTree(sWrapperGeneralGlyph, rootId, rootNode, leaves);
			//System.out.println(rootNode.toMathML());
			functionTerm.setMath(rootNode);
		}
	}
	
	public void buildTree(SWrapperGeneralGlyph sWrapperGeneralGlyph, String parentId, ASTNode parent, 
			HashMap<String, ASTNode> leaves){

		for (String referenceId: sWrapperGeneralGlyph.arcs.keySet()) {
			String childId = sWrapperGeneralGlyph.arcs.get(referenceId).sourceId;
			
			// if logicArc that points into the parent logicOperator
			if (childId != parentId){
				//System.out.println("childId");
				//System.out.println(childId);
				
				SWrapperQualitativeSpecies child = sWrapperModel.listOfSWrapperQualitativeSpecies.get(childId);
				if (child != null){
					//System.out.println("buildTree" + leaves.get(childId));
					parent.addChild(leaves.get(childId));
				} else {
					// create a new ASTNode, and add to parent
					ASTNode childNode = sUtil.createMath(parent, sWrapperModel.listOfWrapperGeneralGlyphs.get(childId).clazz);
					buildTree(sWrapperGeneralGlyph, childId, childNode, leaves);
				}
			}
		}		
	}
	
	public void createOneCompleteTransition(SWrapperGeneralGlyph sWrapperGeneralGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		//System.out.println(sWrapperGeneralGlyph.glyph);
		SWrapperTransition sWrapperTransition = createOneTransition(sWrapperGeneralGlyph.glyph, sWrapperGeneralGlyph);
		sWrapperModel.addSWrapperTransition(sWrapperGeneralGlyph.id, sWrapperTransition);
		
		createOneCompleteTransition(sWrapperTransition, sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
		
//		System.out.println(sWrapperTransition.id);
//		System.out.println(sWrapperTransition.listOfWrapperGeneralGlyphs);
//		System.out.println(sWrapperTransition.listOfWrapperReferenceGlyphs+ "\n");
	}
	
	public void createOneCompleteTransition(SWrapperTransition sWrapperTransition,
			SWrapperGeneralGlyph sWrapperGeneralGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		if (listOfWReferenceGlyphs.size() == sWrapperModel.listOfWrapperReferenceGlyphs.size()){return;}
		
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		for (String key: sWrapperModel.listOfWrapperReferenceGlyphs.keySet()) {
			sWrapperReferenceGlyph = listOfWReferenceGlyphs.get(key);
			if (sWrapperReferenceGlyph != null){
				continue;
			}
			sWrapperReferenceGlyph = sWrapperModel.listOfWrapperReferenceGlyphs.get(key);
			String generalGlyphId = sWrapperGeneralGlyph.id;
			String sourceId = sWrapperReferenceGlyph.sWrapperArc.sourceId;
			String targetId = sWrapperReferenceGlyph.sWrapperArc.targetId;
			
			if (generalGlyphId.equals(sourceId) || generalGlyphId.equals(targetId)){
				sWrapperTransition.addReference(sWrapperReferenceGlyph, sWrapperReferenceGlyph.sWrapperArc);
				listOfWReferenceGlyphs.put(key, null);
				createOneCompleteTransition(sWrapperTransition, sWrapperReferenceGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
				// store any Arc connected to GeneralGlyph
				sWrapperGeneralGlyph.addSpeciesReferenceGlyph(key, sWrapperReferenceGlyph, sWrapperReferenceGlyph.sWrapperArc);
				
			} else {
				continue;
			}
		}
	}
	
	public void createOneCompleteTransition(SWrapperTransition sWrapperTransition,
			SWrapperReferenceGlyph sWrapperReferenceGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		if (listOfWGeneralGlyphs.size() == sWrapperModel.listOfWrapperGeneralGlyphs.size()){return;}
		
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		for (String key: sWrapperModel.listOfWrapperGeneralGlyphs.keySet()) {
			sWrapperGeneralGlyph = listOfWGeneralGlyphs.get(key);
			if (sWrapperGeneralGlyph != null){
				continue;
			}	
			sWrapperGeneralGlyph = sWrapperModel.listOfWrapperGeneralGlyphs.get(key);
			
			String sourceId = sWrapperReferenceGlyph.sWrapperArc.sourceId;
			String targetId = sWrapperReferenceGlyph.sWrapperArc.targetId;
			
			String generalGlyphId = sWrapperGeneralGlyph.id;
			
			if (generalGlyphId.equals(sourceId) || generalGlyphId.equals(targetId)){
				sWrapperTransition.addGeneralGlyph(generalGlyphId, sWrapperGeneralGlyph, sWrapperGeneralGlyph.glyph);
				listOfWGeneralGlyphs.put(key, null);
				createOneCompleteTransition(sWrapperTransition, sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
				
			} else {
				continue;
			}
		}		
	}	
}
