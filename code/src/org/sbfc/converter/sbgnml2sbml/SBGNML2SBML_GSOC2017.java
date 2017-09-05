package org.sbfc.converter.sbgnml2sbml;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBGNModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
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


/**
 * The SBGNML2SBML_GSOC2017 class is the primary converter for converting from SBGN to SBML core+layout+render. 
 * It converts from a libSBGN Sbgn object to the JSBML Model and its extensions. 
 * Model elements are added as the converter interprets the input libSBGN Sbgn.Map.
 * @author haoran
 */	
public class SBGNML2SBML_GSOC2017  extends GeneralConverter {
	// A SWrapperModel Model wrapper stores the Model as well as some objects contained in the Model. 
	// Example: Species, Reaction, Compartment, SpeciesGlyph, ReactionGlyph, CompartmentGlyph, etc.
	// SWrapperModel allows to retrieve information easily without having to search in the Model
	// SWrapperModel stores SWrapperSpeciesGlyph, SWrapperReactionGlyph, etc, which are Wrappers for SpeciesGlyph, ReactionGlyph, etc.
	// SBGNML2SBML_GSOC2017 does not store any Model objects.
	//public SWrapperModel sWrapperModel;
	
	// SBGNML2SBMLOutput contains all data structures needed to create the output XML document. 
	// Example: LayoutModelPlugin.
	//public SBGNML2SBMLOutput sOutput;
	
	// SBGNML2SBMLUtil contains methods that do not depend on any information in the Model. 
	// Example: finding a value from a given list.
	//public SBGNML2SBMLUtil sUtil;
	
	// SBGNML2SBMLRender contains methods to create the RenderInformation element of the Model.
	//public SBGNML2SBMLRender sRender;
	
	// debugging:
	private int consumptionArcErrors = 0;
	private int productionArcErrors = 0;
	public String fileName = "";
		
	
	public SBGNML2SBML_GSOC2017() {
	  
	}
	
	/**
	 * The constructor.
	 * Creates 4 helper classes for the converter: SBGNML2SBMLOutput, SBGNML2SBMLUtil, 
	 * SWrapperModel and SBGNML2SBMLRender.
	 * @param map: contained in Sbgn
	 */
	public SBGNML2SBML_GSOC2017(Map map) {
		
	}

//	public SWrapperModel getSWrapperModel(){
//		return sWrapperModel;
//	}
		
	
	/**
	 * Create all the elements of an SBML <code>Model</code>, 
	 * these created objects correspond to objects in the <code>Map</code> of <code>Sbgn</code>. 
	 * i.e. each <code>Glyph</code> or <code>Arc</code> of the <code>Map</code> 
	 * is mapped to some elements of the SBML <code>Model</code>.
	 */	
	public SWrapperModel convertToSBML(Map map) {
		System.out.println("File: " + fileName);
		
		// TODO - we should not be using class variables here so that the class can be used to convert several models at the same time without problems.
		// this method should just return an SBMLdocument and the sWrapperModel and any other needed variable should be passes as arguments of the methods that need them.
		
		// currently, this converter only works on Version 3 Level 1
		SBGNML2SBMLOutput sOutput = new SBGNML2SBMLOutput(3, 1);
        //sUtil = new SBGNML2SBMLUtil(3, 1);
		SWrapperModel sWrapperModel = new SWrapperModel(sOutput.getModel(), map);
        //sRender = new SBGNML2SBMLRender(sWrapperModel, sOutput, sUtil);
		storeTemplateRenderInformation(sOutput);
		
		List<Glyph> listOfGlyphs = sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = sWrapperModel.map.getArc();
		List<Arcgroup> listOfArcgroups = sWrapperModel.map.getArcgroup();
		
		// Go over every Glyph in the libSBGN Map, classify them, and 
		// store the Glyph in the appropriate container in SWrapperModel 		
		addGlyphsToSWrapperModel(sWrapperModel, listOfGlyphs, listOfArcgroups);

		// Create Compartments and CompartmentGlyphs for the SBGN Glyphs
		createCompartments(sWrapperModel, sOutput);
		// Create Species and SpeciesGlyphs for the SBGN Glyphs
		createSpecies(sWrapperModel, sOutput);	
		// Check if the Model contains any Compartments. For any Species not having a Comparment,
		// create a default one and assign every Species without a Compartment to this Compartment.
		// TODO: move to ModelCompleter.java
		SBGNML2SBMLUtil.createDefaultCompartment(sOutput.model);
	
		// Go over every Arc in the libSBGN Map, classify them, and 
		// store the Arc in the appropriate container in SWrapperModel 
		addArcsToSWrapperModel(sWrapperModel, listOfArcs, listOfArcgroups);
		
		// Create Reactions and ReactionGlyphs using the SBGN Glyphs.
		// Then create SpeciesReference and SpeciesReferenceGlyphs using the SBGN Arcs
		createReactions(sWrapperModel, sOutput);
		// See Problem #1 in Github Wiki: see "or-simple.sbgn"
		// Create GeneralGlyphs for Arcs that do not belong to any Reactions. i.e. Logics Arcs
		// Note that LogicOperators are classified as SpeciesGlyphs (not ReactionGlyphs),
		// because if LogicOperators are ReactionGlyphs, we cannot convert an SBGN "Modifier" Arc 
		// to a SpeciesReference. i.e. the "source" of the Arc
		// needs to be a Species (not a Reaction).  
		// Now, with LogicOperators classified as SpeciesGlyphs,
		// Modifier Arcs have a SpeciesReference, and are part of a Reaction.
		createGeneralGlyphs(sWrapperModel, sOutput);
		
		// Set the Dimensions for the Layout
		sOutput.createCanvasDimensions();
		
		// Render all elements currently in the Model Layout
		SBGNML2SBMLRender.renderCompartmentGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderSpeciesGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderReactionGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderGeneralGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderTextGlyphs(sWrapperModel, sOutput);
		
		// Fill in missing information of the converted SBML Model objects (such as Species concentrations)
		sOutput.completeModel();
		// Store Styles of SBML Layout objects, so that they can be rendered to images
		sOutput.removeExtraStyles();
		
		// Print some statistics
		System.out.println("-----BEFORE CONVERSION-----");
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

		System.out.println("-----AFTER CONVERSION-----");
		
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
		System.out.println("listOfWrapperReferenceGlyphs "+sWrapperModel.listOfWrapperReferenceGlyphs.size() );
		
		System.out.println("-----ERRORS-----");
		
		System.out.println("consumptionArcErrors "+consumptionArcErrors );
		System.out.println("productionArcErrors "+productionArcErrors );
		System.out.println("numOfSpeciesReferenceGlyphErrors "+sOutput.numOfSpeciesReferenceGlyphErrors );
		//System.out.println("createOneCurveError "+SBGNML2SBMLUtil.createOneCurveError );

		return sWrapperModel;
	}

	/**
	 * Classify every <code>Glyph</code> in the libSBGN <code>Map</code>, store the <code>Glyph</code> 
	 * in the appropriate container in SWrapperModel.
	 */		
	public void addGlyphsToSWrapperModel(SWrapperModel sWrapperModel, List<Glyph> listOfGlyphs, List<Arcgroup> listOfArcgroups) {
		for (Arcgroup ag: listOfArcgroups){
			List<Glyph> glyphs= ag.getGlyph();
			listOfGlyphs.addAll(glyphs);
		}
				
		String id;
		String clazz; 	
		for (Glyph glyph: listOfGlyphs) {
			id = glyph.getId();
			clazz = glyph.getClazz();

			if (SBGNML2SBMLUtil.isProcessNode(clazz)) {
				sWrapperModel.addSbgnProcessNode(id, glyph);
			} else if (SBGNML2SBMLUtil.isCompartment(clazz)) {
				sWrapperModel.addSbgnCompartment(id, glyph);
			} else if (SBGNML2SBMLUtil.isEntityPoolNode(clazz)) {
				sWrapperModel.addSbgnEntityPoolNode(id, glyph);
			} else if (SBGNML2SBMLUtil.isLogicOperator(clazz)) {
				sWrapperModel.addSbgnLogicOperator(id, glyph);
			} else if (SBGNML2SBMLUtil.isTag(clazz)) {
				// we classify Tag as a LogicOperator, because they are connected by a Logic Arc
				sWrapperModel.addSbgnLogicOperator(id, glyph);
			} else if (SBGNML2SBMLUtil.isAnnotation(clazz)){
				sWrapperModel.addAnnotation(id, glyph);
			}
		}
		
	}

	/**
	 * Classify every <code>Arc</code> in the libSBGN <code>Map</code>, store the <code>Arc</code>
	 * in the appropriate container in SWrapperModel.
	 */
	public void addArcsToSWrapperModel(SWrapperModel sWrapperModel, List<Arc> listOfArcs ,List<Arcgroup> listOfArcgroups) {
		for (Arcgroup ag: listOfArcgroups){
			List<Arc> arcs= ag.getArc();
			listOfArcs.addAll(arcs);
		}
				
		String id;
		SWrapperArc sWrapperArc;
		
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			// create a Wrapper for each arc, the Wrapper stores additional information about the Arc
			sWrapperArc = createWrapperArc(sWrapperModel, arc);
			
			if (SBGNML2SBMLUtil.isLogicArc(arc)){
				sWrapperModel.addLogicArc(id, sWrapperArc);
			} else if (sWrapperModel.getWrapperSpeciesGlyph(sWrapperArc.sourceId) != null &&
					sWrapperModel.getWrapperSpeciesGlyph(sWrapperArc.targetId) != null){
				// the arc needs to be able to find 2 existing glyphs (SWrapperSpeciesGlyph) connecting to it
				sWrapperModel.addLogicArc(id, sWrapperArc);
			} else if (SBGNML2SBMLUtil.isModifierArc(arc.getClazz())) {
				sWrapperModel.addModifierArc(id, sWrapperArc);
			} else if (SBGNML2SBMLUtil.isConsumptionArc(arc.getClazz())) {
				sWrapperModel.addConsumptionArc(id, sWrapperArc);
			} else if (SBGNML2SBMLUtil.isProductionArc(arc.getClazz())) {
				sWrapperModel.addProductionArc(id, sWrapperArc);
			} 		
		}	
	}	
	
	
	/**
	 * Create multiple SBML <code>SpeciesGlyph</code>s and its associated 
	 * <code>Species</code> from the list of SBGN <code>Glyph</code>s in sWrapperModel.
	 * Add the created <code>SWrapperSpeciesGlyph</code>s to the sWrapperModel.
	 */	
	public void createSpecies(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		SWrapperSpeciesGlyph sWrapperSpeciesGlyph;
		Glyph glyph;
		// create Species and SpeciesGlyphs for Entity Pool Nodes
		for (String key : sWrapperModel.entityPoolNodes.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			sWrapperSpeciesGlyph = createOneSpecies(sWrapperModel, sOutput, glyph);
			sWrapperModel.addSWrapperSpeciesGlyph(key, sWrapperSpeciesGlyph);
		}
		
		// create Species and SpeciesGlyphs for Logic Operators
		List<Arc> allArcs = sWrapperModel.map.getArc();

		for (String key : sWrapperModel.logicOperators.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
							
			// one possible way to create ReactionGlyphs (instead of SpeciesGlyphs) for Logic Operators
//			for (Arc candidate: allArcs){
//				Object source = candidate.getSource();
//				Object target = candidate.getTarget();
//				
//				Glyph connectingGlyph1 = getGlyph(source);
//				Glyph connectingGlyph2 = getGlyph(target);
//
//				if (!(connectingGlyph1.getId().equals(key)) && 
//						!(connectingGlyph2.getId().equals(key))){
//					continue;
//				} else if (connectingGlyph1.getId().equals(key)){
//					if (SBGNML2SBMLUtil.isLogicOperator(connectingGlyph2.getClazz())){
//						break;
//					}
//				} else if (connectingGlyph2.getId().equals(key)){
//					if (SBGNML2SBMLUtil.isLogicOperator(connectingGlyph1.getClazz())){
//						break;
//					}
//				}
//			}
			
			sWrapperSpeciesGlyph = createOneSpecies(sWrapperModel, sOutput, glyph);
			sWrapperModel.addSWrapperSpeciesGlyph(key, sWrapperSpeciesGlyph);				
		}
	}
	
	/**
	 * Create an SBML <code>Species</code> and <code>SpeciesGlyph</code> for <code>glyph</code>, 
	 * Create <code>TextGlyph</code>s for the Species. 
	 * Create <code>GeneralGlyph</code>s (if any) for the Species. 
	 */		
	public SWrapperSpeciesGlyph createOneSpecies(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, Glyph glyph) {
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
		
		name = SBGNML2SBMLUtil.getText(glyph);
		clazz = glyph.getClazz();
		speciesId = glyph.getId();
				
		// create a Species, add SBOTerms and Annotation. add Species to the output
		species = SBGNML2SBMLUtil.createJsbmlSpecies(speciesId, name, clazz, false, true);
		// store the Species in the output Model
		sOutput.addSpecies(species);
		
		// create a SpeciesGlyph, add it to the output 
		bbox = glyph.getBbox();
		speciesGlyph = SBGNML2SBMLUtil.createJsbmlSpeciesGlyph(speciesId, name, clazz, species, true, bbox);
		// store the SpeciesGlyph in the output Model
		sOutput.addSpeciesGlyph(speciesGlyph);
		
		// if the Glyph contains nested Glyphs such as State Variable or Units of Information, 
		// create GeneralGlyphs for these, add them to output
		if (glyph.getGlyph().size() != 0){
			nestedGlyphs = glyph.getGlyph();
			listOfGeneralGlyphs = createNestedGlyphs(sWrapperModel, sOutput, nestedGlyphs, speciesGlyph);
		} 
		
		// create TextGlyph for the SpeciesGlyph
		textGlyph = null;
		Bbox labelBbox = null;
		
		// a Label might have a Bbox
		if (glyph.getLabel() != null && glyph.getLabel().getBbox() != null){
			labelBbox = glyph.getLabel().getBbox();
		}
		
		if (SBGNML2SBMLUtil.isLogicOperator(clazz)){
			// a Logic Operator does not have any other text, except for "AND", "OR", "NOT"
			textGlyph = SBGNML2SBMLUtil.createJsbmlTextGlyph(speciesGlyph, clazz.toUpperCase(), labelBbox);
		} else if (clazz.equals("source and sink")) {
			textGlyph = null;
		} else {
			// the usual case
			textGlyph = SBGNML2SBMLUtil.createJsbmlTextGlyph(species, speciesGlyph, labelBbox);
		}
		sOutput.addTextGlyph(textGlyph);
		
		// optional. 
		if (glyph.getGlyph().size() != 0){
			sWrapperModel.getTextSourceMap().put(textGlyph.getId(), glyph.getId());
		}
		
		// create a new SWrapperSpeciesGlyph class, store a list of GeneralGlyphs if present
		speciesGlyphTuple =  new SWrapperSpeciesGlyph(species, speciesGlyph, glyph, textGlyph);
		speciesGlyphTuple.setListOfNestedGlyphs(listOfGeneralGlyphs);
		
		// Note that the texts in the Clone Marker cannot display correctly in the rendered image
		String text = SBGNML2SBMLUtil.getClone(glyph);
		if (text != null){
			speciesGlyphTuple.setCloneText(text);
		}
		
		return speciesGlyphTuple;
	}
	
	/**
	 * Create multiple SBML <code>GeneralGlyph</code>/<code>SpeciesGlyph</code>s from the list of SBGN <code>Glyph</code>s
	 * provided. 
	 * Add the created <code>SWrapperGeneralGlyph</code>/<code>SWrapperSpeciesGlyph</code>s to the sWrapperModel. 
	 */		
	public List<GraphicalObject> createNestedGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, List<Glyph> glyphs, GraphicalObject parent) {
		List<GraphicalObject> listOfGeneralGlyphs = new ArrayList<GraphicalObject>();
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperSpeciesGlyph sWrapperSpeciesGlyph;
		
		for (Glyph glyph : glyphs) {
			if (SBGNML2SBMLUtil.isEntityPoolNode(glyph.getClazz()) || (glyph.getClazz().equals("terminal"))){
				// if the Glyph is an Entity Pool or a Tag/Terminal, create a SpeciesGlyph
				sWrapperSpeciesGlyph = createOneSpecies(sWrapperModel, sOutput, glyph);
				sWrapperModel.addSWrapperSpeciesGlyph(glyph.getId(), sWrapperSpeciesGlyph);
				
				// append Annotation to the Species to note that there is a parent glyph
				// i.e. the glyph is part of a parent glyph
				// this is useful for roundtrip conversion SBGN->SBML->SBGN
				SBGNML2SBMLUtil.addAnnotation(sWrapperSpeciesGlyph.species, parent.getId(), Qualifier.BQB_IS_PART_OF);
				
			} else {
				// create a GeneralGlyph, add it to the SWrapperModel
				sWrapperGeneralGlyph = createOneGeneralGlyph(sWrapperModel, sOutput, glyph, parent, true, true);
				sWrapperModel.addWrapperGeneralGlyph(glyph.getId(), sWrapperGeneralGlyph);
				sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);		
				
				// optional
				listOfGeneralGlyphs.add(sWrapperGeneralGlyph.generalGlyph);
			}
		}		
		
		// optional
		return listOfGeneralGlyphs;
	}
	
	/**
	 * Create multiple SBML <code>ReactionGlyph</code>s and associated 
	 * <code>Reaction</code>s from the list of SBGN <code>Glyph</code>s. 
	 */			
	public void createReactions(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		SWrapperReactionGlyph sWrapperReactionGlyph;
		Glyph glyph;
		
		for (String key: sWrapperModel.processNodes.keySet()) {
			glyph = sWrapperModel.processNodes.get(key);
			sWrapperReactionGlyph =  createOneReactionGlyph(sWrapperModel, sOutput, glyph);
			sWrapperModel.addSWrapperReactionGlyph(key, sWrapperReactionGlyph);
		}
	}	
	
	/**
	 * Create a SBML <code>Reaction</code> and <code>ReactionGlyph</code> for <code>glyph</code>, 
	 * then create SpeciesReference/ModifierSpeciesReference and SpeciesReferenceGlyph that are part of the Reaction
	 * Return a <code>SWrapperSpeciesGlyph</code>. 
	 */		
	public SWrapperReactionGlyph createOneReactionGlyph(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, Glyph glyph) {
		String reactionId;
		String name;
		String clazz;
		Reaction reaction;
		ReactionGlyph reactionGlyph;
		Bbox bbox;
		SWrapperReactionGlyph sWrapperReactionGlyph;
		
		reactionId = glyph.getId();
		name = SBGNML2SBMLUtil.getText(glyph);
		clazz = glyph.getClazz();
		
		// Create a Reaction
		reaction = SBGNML2SBMLUtil.createJsbmlReaction(reactionId);
		SBGNML2SBMLUtil.addSBO(reaction, clazz);	
		sOutput.addReaction(reaction);
		
		// Create a ReactionGlyph
		bbox = glyph.getBbox();
		reactionGlyph = SBGNML2SBMLUtil.createJsbmlReactionGlyph(reactionId, name, clazz, reaction, true, bbox);
		sOutput.addReactionGlyph(reactionGlyph);
		
		// Create a temporary center Curve for the ReactionGlyph
		SBGNML2SBMLUtil.createReactionGlyphCurve(reactionGlyph, glyph);
				
		sWrapperReactionGlyph = new SWrapperReactionGlyph(reaction, reactionGlyph, glyph, sWrapperModel);
		
		// Create all SpeciesReference/SpeciesReferenceGlyphs associated with this Reaction/ReactionGlyph.
		createSpeciesReferenceGlyphs(sWrapperModel, sOutput, reaction, reactionGlyph, sWrapperReactionGlyph);
		
		// update the temporary center Curve with correct values
		setStartAndEndPointForCurve(sWrapperReactionGlyph);
		
		return sWrapperReactionGlyph;
	} 
		
	
	/**
	 * Create a SBML <code>SpeciesReference</code> and <code>SpeciesReferenceGlyph</code> using <code>sWrapperArc</code>, 
	 * which are part of the <code>reaction</code>, <code>reactionGlyph</code>.
	 * Return a <code>SWrapperSpeciesReferenceGlyph</code>. 
	 */		
	public SWrapperSpeciesReferenceGlyph createOneSpeciesReferenceGlyph(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, Reaction reaction, ReactionGlyph reactionGlyph,
			SWrapperArc sWrapperArc, String speciesId, Glyph speciesGlyph, String reactionId, String speciesReferenceId) {
		Curve curve;
		Species species;
		SpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph;
		
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = SBGNML2SBMLUtil.createSpeciesReference(reaction, species, speciesReferenceId);
		SBGNML2SBMLUtil.addSBO(speciesReference, sWrapperArc.arc.getClazz());	
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = SBGNML2SBMLUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, sWrapperArc.arc, 
			speciesReference, speciesGlyph, sOutput);
		
		// create the Curve for the SpeciesReferenceGlyph
		curve = SBGNML2SBMLUtil.createOneCurve(sWrapperArc.arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		sOutput.addSpeciesReferenceGlyph(reactionGlyph, speciesReferenceGlyph);
		
		sWrapperSpeciesReferenceGlyph = new SWrapperSpeciesReferenceGlyph(speciesReference, speciesReferenceGlyph, sWrapperArc);
		
		// if the Arc contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (sWrapperArc.arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = sWrapperArc.arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs(sWrapperModel, sOutput, nestedGlyphs, speciesReferenceGlyph);
			sWrapperSpeciesReferenceGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
		
		// add extra information in the object's annotation
		// the SpeciesReferenceGlyph stores the "source" and "target" glyphs, this helps with the roundtrip conversion
		addSourceTargetToAnnotation(speciesReferenceGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);
		
		return sWrapperSpeciesReferenceGlyph;
	}


	/**
	 * Create a SBML <code>ModifierSpeciesReference</code> and <code>SpeciesReferenceGlyph</code> using <code>sWrapperArc</code>, 
	 * which are part of the <code>reaction</code>, <code>reactionGlyph</code>.
	 * Return a <code>SWrapperSpeciesReferenceGlyph</code>. 
	 */		
	public SWrapperModifierSpeciesReferenceGlyph createOneModifierSpeciesReferenceGlyph(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, Reaction reaction, ReactionGlyph reactionGlyph,
			SWrapperArc sWrapperArc, String speciesId, Glyph speciesGlyph, String reactionId, String speciesReferenceId) {
		Curve curve;
		Species species;
		ModifierSpeciesReference speciesReference;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		SWrapperModifierSpeciesReferenceGlyph sWrapperModifierSpeciesReferenceGlyph;
		
		// TODO: our assumption is : Arc.start=Arc.source, end=target, but this might not be true
				
		// create a SpeciesReference
		species = sOutput.findSpecies(speciesId);
		speciesReference = SBGNML2SBMLUtil.createModifierSpeciesReference(reaction, species, speciesReferenceId);
		SBGNML2SBMLUtil.addSBO(speciesReference, sWrapperArc.arc.getClazz());	
		
		// create a SpeciesReferenceGlyph
		speciesReferenceGlyph = SBGNML2SBMLUtil.createOneSpeciesReferenceGlyph(speciesReferenceId, sWrapperArc.arc, 
			speciesReference, speciesGlyph, sOutput);
		
		// create the Curve for the SpeciesReferenceGlyph
		curve = SBGNML2SBMLUtil.createOneCurve(sWrapperArc.arc);
		speciesReferenceGlyph.setCurve(curve);
		
		// add the SpeciesReferenceGlyph to the ReactionGlyph, and add it to output
		reactionGlyph = sOutput.findReactionGlyph("ReactionGlyph_"+reactionId);
		sOutput.addSpeciesReferenceGlyph(reactionGlyph, speciesReferenceGlyph);
		
		sWrapperModifierSpeciesReferenceGlyph = new SWrapperModifierSpeciesReferenceGlyph(speciesReference, 
													speciesReferenceGlyph, sWrapperArc);
		
		// if the Arc contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (sWrapperArc.arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = sWrapperArc.arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs( sWrapperModel, sOutput, nestedGlyphs, speciesReferenceGlyph);
			sWrapperModifierSpeciesReferenceGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
		
		// add extra information in the object's annotation
		// the SpeciesReferenceGlyph stores the "source" and "target" glyphs, this helps with the roundtrip conversion
		addSourceTargetToAnnotation(speciesReferenceGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);
		
		return sWrapperModifierSpeciesReferenceGlyph;
	}
	
	/**
	 * Creates a SWrapperArc Wrapper for the <code>arc</code>.
	 * @param arc
	 * @return a SWrapperArc
	 */
	public SWrapperArc createWrapperArc(SWrapperModel sWrapperModel, Arc arc) {
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
		
		// There are 4 types of Arcs, each will end up being converted to a different type of SBML glyph: 
		// the glyphToPortArc has source from a Glyph and has target to a Port
		// the portToGlyphArc has source from a Port and has target to a Glyph
		// the glyphToGlyphArc has source from a Glyph and has target to a Glyph
		// the portToPortArc has source from a Port and has target to a Port
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
	 * from the list of all SBGN <code>Arcs</code>s.
	 * Creation a new <code>SpeciesReference</code> and <code>SpeciesReferenceGlyph</code> only when the <code>Arc</code> is 
	 * associated with the provided <code>reaction</code>.
	 * Add the created <code>SpeciesReferenceGlyph</code>s to the <code>reactionGlyph</code>. 
	 */			
	public List<SWrapperSpeciesReferenceGlyph> createSpeciesReferenceGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, Reaction reaction, ReactionGlyph reactionGlyph, 
			SWrapperReactionGlyph reactionGlyphTuple) {
		List<SWrapperSpeciesReferenceGlyph> listOfSWrappersSRG = new ArrayList<SWrapperSpeciesReferenceGlyph>();
		Arc arc;
		String speciesReferenceId;
		String reactionId;
		
		SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph;
		SWrapperModifierSpeciesReferenceGlyph sWrapperModifierSpeciesReferenceGlyph;
		SWrapperArc sWrapperArc;
		
		// consumption arcs
		for (String key: sWrapperModel.consumptionArcs.keySet()) {
			sWrapperArc = sWrapperModel.consumptionArcs.get(key);
			arc = sWrapperArc.arc;
			speciesReferenceId = key;
			reactionId = sWrapperArc.targetId;
			
			// Proceed only when the Arc is associated with the provided reaction.
			if (reactionId != reaction.getId()){ continue; } 
			else {
				// store the sWrapperArc in sModelWrapper
				reactionGlyphTuple.addArc(speciesReferenceId, sWrapperArc, "consumption");
			}
			
			// then,
			// create a SpeciesReference and a SpeciesReferenceGlyph, add the SpeciesReferenceGlyph to the ReactionGlyph
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(sWrapperModel, sOutput, reaction, reactionGlyph,
					sWrapperArc, sWrapperArc.sourceId, getGlyph(sWrapperModel, sWrapperArc.source), reactionId, speciesReferenceId);
			
			// add the SpeciesReference to the Reaction
			reaction.addReactant(sWrapperSpeciesReferenceGlyph.speciesReference);
			
			// this is a trick to correctly set the Start and End point of the center Curve of the ReactionGlyph
			// note that this trick works well so far
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "start", 
					reactionGlyphTuple);
		
			// add the SpeciesReferenceGlyph to the SWrapperReactionGlyph
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph);	
			// optional
			// add the enclosing SWrapperSpeciesReferenceGlyph to the List<SWrapperSpeciesReferenceGlyph>
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}	
		// production arcs
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
			
			sWrapperSpeciesReferenceGlyph = createOneSpeciesReferenceGlyph(sWrapperModel, sOutput, reaction, reactionGlyph,
					sWrapperArc, sWrapperArc.targetId, getGlyph(sWrapperModel, sWrapperArc.target), reactionId, speciesReferenceId);
			reaction.addProduct(sWrapperSpeciesReferenceGlyph.speciesReference);
			updateReactionGlyph(reactionGlyph, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph, "end",
					reactionGlyphTuple);
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperSpeciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperSpeciesReferenceGlyph);
		}
		// the modifier arcs
		// The way they are handled is very similar, except for 
		// small variations when creating SpeciesReferenceGlyphs.
		// assuming target is a process node, but may not be true.
		// It seems that Modifier Arcs point into a Process Node, 
		// the Arc could point into a Port, or points to the Glyph
		// handle these cases differently
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
			
			sWrapperModifierSpeciesReferenceGlyph = createOneModifierSpeciesReferenceGlyph(sWrapperModel, sOutput, reaction, reactionGlyph,
					sWrapperArc, sWrapperArc.sourceId, getGlyph(sWrapperModel, sWrapperArc.source), reactionId, speciesReferenceId);
			reaction.addModifier(sWrapperModifierSpeciesReferenceGlyph.speciesReference);
			
			reactionGlyphTuple.addSpeciesReferenceGlyph(speciesReferenceId, 
					sWrapperModifierSpeciesReferenceGlyph);
			listOfSWrappersSRG.add(sWrapperModifierSpeciesReferenceGlyph);
		}		
		
		// Note for "or-simple.sbgn"
		// Modifier Arcs comes out of a Logic Operator going into a Process Node
		// these Arcs, once converted to a SpeciesReference, does not have a Species to reference to.
		// i.e. the Arc points to a ReactionGlyph, not a SpeciesGlyph
		// Solution:
		// if the Arc is a Modifier Arc, and it comes out of a Logic Operator, then the Arc will be part of a Reaction that it points to
		// if the Arc is a Logic Arc, it will be part of a GeneralGlyph without have a SpeciesReference
		// i.e. the converted "core" Model will be missing some arcs
		
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
//				clazz.equals("inhibition") ){		// and many others
//			Object source = arc.getSource();
//			if (source instanceof Glyph){
//				if (SBGNML2SBMLUtil.isLogicOperator(((Glyph) source).getClazz())){
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
	 * Update the start or end <code>Point</code> of the center Curve of a <code>ReactionGlyph</code> using values 
	 * in a <code>SpeciesReferenceGlyph</code>. 
	 * Note that we don't set the values in this function, we just store the correct values. Then later in another function, 
	 * we set the values for the Curve.
	 */			
	public void updateReactionGlyph(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph speciesReferenceGlyph, 
			String reactionGlyphPointType, SWrapperReactionGlyph sWrapperReactionGlyph){
		Point curvePoint = null;
		
		// we set the "start" of the Curve
		if (reactionGlyphPointType.equals("start")) {
			int count = speciesReferenceGlyph.getCurve().getCurveSegmentCount();
			
			if (count != 0){
				// we assume the last CurveSegment in this Curve touches the ReactionGlyph
				curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(count - 1).getEnd();
				// add the correct Point to the sWrapperReactionGlyph Wrapper, 
				// so that the Point can be used later to set the correct value
				sWrapperReactionGlyph.addStartPoint(curvePoint);				
			} else {
				System.out.println("! updateReactionGlyph addStartPoint count="+count+" reactionGlyph id="+
						reactionGlyph.getId()+" speciesReferenceGlyph id="+speciesReferenceGlyph.getId());
				consumptionArcErrors++;
			}

		// we set the "end" of the Curve
		} else if (reactionGlyphPointType.equals("end")) {
			int count = speciesReferenceGlyph.getCurve().getCurveSegmentCount();
			
			if (count != 0){
				// we assume the last CurveSegment in this Curve touches the SpeciesGlyph
				curvePoint = speciesReferenceGlyph.getCurve().getCurveSegment(0).getStart();
				// add the correct Point to the sWrapperReactionGlyph Wrapper, 
				// so that the Point can be used later to set the correct value
				sWrapperReactionGlyph.addEndPoint(curvePoint);
			} else {
				System.out.println("! updateReactionGlyph addEndPoint count="+count+" reactionGlyph id="+
						reactionGlyph.getId()+" speciesReferenceGlyph id="+speciesReferenceGlyph.getId());
				productionArcErrors++;
			}
		}
	}
	
	/**
	 * Update the start or end <code>Point</code> of the center Curve of a <code>ReactionGlyph</code> using values 
	 * in a <code>SpeciesReferenceGlyph</code>. 
	 */		
	void setStartAndEndPointForCurve(SWrapperReactionGlyph sWrapperReactionGlyph){
//		 int _nrows = sWrapperReactionGlyph.listOfEndPoints.size();
//	     KMeans KM = new KMeans( sWrapperReactionGlyph.listOfEndPoints, null );
	     
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
	
	/**
	 * Update the start or end <code>Point</code> of the center Curve of a <code>GeneralGlyph</code> using values 
	 * in a <code>listOfEndPoints</code>. 
	 */	
	public void setStartAndEndPointForCurve(List<Point> listOfEndPoints, GeneralGlyph generalGlyph){
//	 	 int _nrows = listOfEndPoints.size();
//	     KMeans KM = new KMeans( listOfEndPoints, null );
	    
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
	 *  <code>Compartment</code> from list of SBGN <code>Glyph</code>s. 
	 */		
	public void createCompartments(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {

		for (String key: sWrapperModel.compartments.keySet()) {
			SWrapperCompartmentGlyph compartmentGlyphTuple = createOneCompartment(sWrapperModel, sOutput, key);
			sWrapperModel.addSWrapperCompartmentGlyph(key, compartmentGlyphTuple);
		}
	}
	
	/**
	 * Create a SBML <code>Compartment</code> and <code>CompartmentGlyph</code>, 
	 * Return a <code>SWrapperCompartmentGlyph</code>. 
	 */		
	public SWrapperCompartmentGlyph createOneCompartment(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, String key) {
		Glyph glyph;
		String compartmentId;
		String name;
		Compartment compartment;
		CompartmentGlyph compartmentGlyph;
		
		glyph = sWrapperModel.compartments.get(key);
		compartmentId = glyph.getId();		
		name = SBGNML2SBMLUtil.getText(glyph);
		
		// create a Compartment, add it to output
		compartment = SBGNML2SBMLUtil.createJsbmlCompartment(compartmentId, name);
		sOutput.addCompartment(compartment);
		
		// create a CompartmentGlyph, add it to output
		compartmentGlyph = SBGNML2SBMLUtil.createJsbmlCompartmentGlyph(glyph, compartmentId, compartment, true);
		sOutput.addCompartmentGlyph(compartmentGlyph);	
		
		// Set the compartmentOrder of the CompartmentGlyph
		SBGNML2SBMLUtil.setCompartmentOrder(compartmentGlyph, glyph);
		
		// Create a TextGlyph, add it to output
		Bbox labelBbox = glyph.getLabel().getBbox();
		TextGlyph textGlyph = SBGNML2SBMLUtil.createJsbmlTextGlyph(compartmentGlyph, glyph.getLabel().getText(), labelBbox);
		sOutput.addTextGlyph(textGlyph);
		// optional
		sWrapperModel.getTextSourceMap().put(textGlyph.getId(), key);
		
		return new SWrapperCompartmentGlyph(compartment, compartmentGlyph, glyph);
	}	

	/**
	 * Create a <code>GeneralGlyph</code> using the <code>glyph</code>.
	 * Set the parent (if any) that contains this <code>GeneralGlyph</code>
	 * Return a <code>SWrapperCompartmentGlyph</code>, 
	 */			
	public SWrapperGeneralGlyph createOneGeneralGlyph(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, Glyph glyph, GraphicalObject parent, boolean setBoundingBox, boolean setText) {
		String text;
		String clazz;
		String id;
		Bbox bbox;
		GeneralGlyph generalGlyph;
		TextGlyph textGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		text = SBGNML2SBMLUtil.getText(glyph);
		clazz = glyph.getClazz();
		bbox = glyph.getBbox();
		
		// create a GeneralGlyph
		generalGlyph = SBGNML2SBMLUtil.createJsbmlGeneralGlyph(glyph.getId(), setBoundingBox, bbox);
			
		// create a TextGlyph, add it to output
		Bbox labelBbox = null;
		if (glyph.getLabel() != null){
			labelBbox = glyph.getLabel().getBbox();
		}
		textGlyph = SBGNML2SBMLUtil.createJsbmlTextGlyph(generalGlyph, text, labelBbox);	
		if (setText){
		sOutput.addTextGlyph(textGlyph);}

		// create a new SWrapperGeneralGlyph, add it to the sWrapperModel
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, glyph, parent, textGlyph, sWrapperModel);
		// append an annotation to the object, saying who is the object's parent. This helps with the roundtrip conversion
		if (parent != null){
			SBGNML2SBMLUtil.addAnnotation(generalGlyph, parent.getId(), Qualifier.BQB_IS_PART_OF);
		}

		return sWrapperGeneralGlyph;		
	}
		
	/**
	 * Add annotation containing values of the sourceId and targetId of an Arc
	 * to the <code>graphicObject</code>
	 * @param graphicObject
	 * @param sourceId
	 * @param targetId
	 */
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
	 * Create a <code>GeneralGlyph</code> without a <code>BoundingBox</code> for a <code>sWrapperArc</code>.
	 * Note that this sWrapperArc is likely to a Logic Operator
	 * Return a <code>SWrapperGeneralGlyph</code>.
	 */		
	public SWrapperGeneralGlyph createOneGeneralGlyph(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, SWrapperArc sWrapperArc) {
		GeneralGlyph generalGlyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		Arc arc = sWrapperArc.arc;

		// No BoundingBox
		generalGlyph = SBGNML2SBMLUtil.createJsbmlGeneralGlyph(arc.getId(), false, null);
		
		// add annotation to the generalGlyph object, specifying its "source" and "target" glyphs
		addSourceTargetToAnnotation(generalGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);
		
		// create a new SWrapperGeneralGlyph
		sWrapperGeneralGlyph = new SWrapperGeneralGlyph(generalGlyph, arc, sWrapperModel);
		
		// if the Arc contains nested glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs(sWrapperModel, sOutput, nestedGlyphs, generalGlyph);
			sWrapperGeneralGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
						
		return sWrapperGeneralGlyph;		
	}	
	
	/**
	 * Create a <code>ReferenceGlyph</code> (using a sWrapperArc) that associates with an <code>GraphicalObject</code> object.
	 */		
	public SWrapperReferenceGlyph createOneReferenceGlyph(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, SWrapperArc sWrapperArc, SBase object) {
		Curve curve;
		ReferenceGlyph referenceGlyph;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		
		// TODO: we assume that ReferenceGlyph.glyph is species, ReferenceGlyph.reference is the "reaction" (reference is not 
		// the speciesReference)
		
		// create a ReferenceGlyph
		referenceGlyph = SBGNML2SBMLUtil.createOneReferenceGlyph(sWrapperArc.arc.getId(), sWrapperArc.arc, 
			null, object);
		SBGNML2SBMLUtil.addSBO(referenceGlyph, sWrapperArc.arc.getClazz());	
		
		// create the Curve for the ReferenceGlyph
		curve = SBGNML2SBMLUtil.createOneCurve(sWrapperArc.arc);
		referenceGlyph.setCurve(curve);
		
		sWrapperReferenceGlyph = new SWrapperReferenceGlyph(referenceGlyph, sWrapperArc);
		
		// if the Arc contains nested Glyphs, create GeneralGlyphs for these, add them to output
		// example: an Arc might have Units of Information
		if (sWrapperArc.arc.getGlyph().size() != 0){
			List<Glyph> nestedGlyphs = sWrapperArc.arc.getGlyph();
			List<GraphicalObject> listOfGeneralGlyphs = createNestedGlyphs(sWrapperModel, sOutput, nestedGlyphs, referenceGlyph);
			sWrapperReferenceGlyph.listOfGeneralGlyphs = listOfGeneralGlyphs;
		} 
		
		// add annotation to the generalGlyph object, specifying its "source" and "target" glyphs
		addSourceTargetToAnnotation(referenceGlyph, sWrapperArc.sourceId, sWrapperArc.targetId);

		return sWrapperReferenceGlyph;
	}
	
	/**
	 * Create multiple SBML <code>GeneralGlyph</code>s from a list of SBGN <code>Glyph</code>s. 
	 */		
	public void createGeneralGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
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
		SWrapperReferenceGlyph sWrapperReferenceGlyph = null;
		SWrapperArc sWrapperArc;
		
		// There are 4 types of SBGN glyphs that should be converted to GeneralGlyphs:
		// 1. Annotation
		// 2. Logic Arcs
		// 3. The center Curve of a Logic Operator (see example in SBGN-PD_all.sbgn)
		// 4. Auxiliary Information such as State Variables (not created here)
		
		// 1. SBGN glyph.clazz == "annotation"
		for (String key: sWrapperModel.annotations.keySet()) {
			Glyph glyph = sWrapperModel.annotations.get(key);
			
			// create a new GeneralGlyph
			sWrapperGeneralGlyph = createOneGeneralGlyph(sWrapperModel, sOutput, glyph, null, true, true);
			sWrapperGeneralGlyph.isAnnotation = true;
			
			// store additional information to the Wrapper, so that we can render the image
			// using these information later.
			Glyph calloutGlyph = (Glyph) glyph.getCallout().getTarget();
			sWrapperGeneralGlyph.calloutTarget = calloutGlyph.getId();
			org.sbgn.bindings.Point calloutPoint = glyph.getCallout().getPoint();
			sWrapperGeneralGlyph.calloutPoint = new Point(calloutPoint.getX(), calloutPoint.getY());

			// store the GeneralGlyph to output
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
			sWrapperModel.addWrapperGeneralGlyph(glyph.getId(), sWrapperGeneralGlyph);
		}
		
		// 2. create a GeneralGlyph for each Logic Arc
		for (String key: sWrapperModel.logicArcs.keySet()) {
			sWrapperArc = sWrapperModel.logicArcs.get(key);
			arc = sWrapperArc.arc;
			objectId = sWrapperArc.sourceId;
			
			// Create a GeneralGlyph without a BoundingBox, add it to sWrapperModel
			sWrapperGeneralGlyph = createOneGeneralGlyph(sWrapperModel, sOutput, sWrapperArc);
			sWrapperModel.addWrapperGeneralGlyph(arc.getId(), sWrapperGeneralGlyph);
			
			// Create a ReferenceGlyph
			speciesGlyph = null;
			try {
				// the ReferenceGlyph could associate with a SpeciesGlyph or a CompartmentGlyph
				if (sWrapperModel.getWrapperSpeciesGlyph(objectId)!=null){
					speciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(objectId).speciesGlyph;
					sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperModel, sOutput, sWrapperArc, speciesGlyph);
					//System.out.println("! sWrapperModel.logicArcs id="+key+" arc="+arc.getId());
				}
				if (speciesGlyph==null){
					CompartmentGlyph compartmentGlyph = sWrapperModel.getWrapperCompartmentGlyph(objectId).compartmentGlyph;
					sWrapperReferenceGlyph = createOneReferenceGlyph(sWrapperModel, sOutput, sWrapperArc, compartmentGlyph);
				}
			
			} catch (Exception e){
				e.printStackTrace();
				System.out.println("speciesGlyph objectId "+objectId); 
			}
			
			if (sWrapperGeneralGlyph == null){System.out.format("! sWrapperGeneralGlyph");}
			if (sWrapperReferenceGlyph == null){System.out.format("! sWrapperReferenceGlyph");}
			// Add the referenceGlyph to the generalGlyph
			sOutput.addReferenceGlyph(sWrapperGeneralGlyph.generalGlyph, sWrapperReferenceGlyph.referenceGlyph);
			
			// Add the ReferenceGlyph to the wrapper. This step is mandatory if we are converting to SBML qual
			sWrapperGeneralGlyph.addSpeciesReferenceGlyph(arc.getId(), sWrapperReferenceGlyph, sWrapperArc);
			
			// Add the GeneralGlyph to the output
			sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
		}	
		
		// 3. Creating center Curves for Logic Operator. 
		// Note that Logic Operator are converted to SpeciesGlyphs, but Logic Operators look like reactions where ReferenceGlyph arcs
		// are connecting to it. Since Logic Operator are SpeciesGlyphsso, we cannot set a Curve (like ReactionGlyphs). This is why
		// we need to create an extra GeneralGlyph that looks like the center Curve of a reaction
		// At the same time, we also note that Tags are also SpeciesGlyphs, but the Arcs that connect to the Tag were not added to logicArcs,
		// so we need to add them here as well.
		for (String key: sWrapperModel.logicOperators.keySet()) {
			sWrapperSpeciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(key);
			
			if (sWrapperSpeciesGlyph == null){continue;}

			// to correctly set the "start" and "end" values of the Curve, we need to find all ReactionGlyph arcs and SpeciesReactionGlyph arcs
			// connecting to it. We use the end points of these arcs to correctly set the values of the Curve
			ArrayList<Point> connectedPoints = new ArrayList<Point>();
			List<Arc> allArcs = sWrapperModel.map.getArc();
			
			Arc chosenArc = null;
			
			for (Arc candidate: allArcs){
				source = candidate.getSource();
				target = candidate.getTarget();

				// if either the Arc's source or target points to the Logic Operator, we add the Arc's value to the connectedPoints
				// we also take note whether this arc is connected to a Tag 
				Arc tagArc = checkArcConnectsToLogicOperator(sWrapperModel, connectedPoints, source, key, candidate, "source");
				if (tagArc != null){chosenArc = tagArc;}
				
				tagArc = checkArcConnectsToLogicOperator(sWrapperModel, connectedPoints, target, key, candidate, "target");
				if (tagArc != null){chosenArc = tagArc;}
			}

			// create a new GeneralGlyph
			GeneralGlyph generalGlyph = SBGNML2SBMLUtil.createJsbmlGeneralGlyph(key, true, sWrapperSpeciesGlyph.sbgnGlyph.getBbox());
			
			// set an empty Curve for the GeneralGlyph
			curve = new Curve();
			LineSegment curveSegment = new LineSegment();
			curveSegment.createEnd();
			curveSegment.createStart();
			curve.addCurveSegment(curveSegment);
			generalGlyph.setCurve(curve);
			
			if (sWrapperSpeciesGlyph.clazz.contains("tag")){
				// set the values of the Curve using only one chosenArc Arc
				curve = SBGNML2SBMLUtil.createOneCurve(chosenArc);
				generalGlyph.setCurve(curve);
			}
			else {
				// set the "start" and "end" of the Curve correctly, using the list of connectedPoints
				setStartAndEndPointForCurve(connectedPoints, generalGlyph);
			}
			
			// add GeneralGlyph to output
			sOutput.addGeneralGlyph(generalGlyph);

		}
		
	}
	
	/**
	 * If the <code>source</code> is an instance of a Glyph, return the glyph. Otherwise, if <code>source</code> is an instance of a Port,
	 * find the Glyph that the Port is part of.
	 * @param source
	 * @return glyph
	 */
	public Glyph getGlyph(SWrapperModel sWrapperModel, Object source) {
		Port connectingPort = null;
		Glyph glyph = null;
		
		if (source instanceof Glyph){
			glyph = (Glyph) source;
		} else if (source instanceof Port){
			connectingPort = (Port) source;
			glyph = sWrapperModel.getGlyph(sWrapperModel.findGlyphFromPort(connectingPort));
		}	
		
		if(glyph == null){
			System.out.println("! getGlyph null " + connectingPort.getId() + " " +sWrapperModel.findGlyphFromPort(connectingPort));
		} 
		
		return glyph;
	}
	
	/**
	 * Check whether the <code>candidate</code> Arc is connecting to a <code>source</code> object and that the <code>source</code>'s id 
	 * is the Logic Operator's id <code>key</code>. If this is the case, we store a Point to connectedPoints. The Point that we store depends
	 * on whether direction is "source" or "target". The connectedPoints will be used later functions to correctly set values in a Curve.
	 * @param connectedPoints a list of points that is already connected to the 
	 * @param source the object that the candidate Arc is connecting to
	 * @param key the Logic Operator id
	 * @param candidate the candidate Arc
	 * @param direction "source" or "target"
	 * @return an arc
	 */
	public Arc checkArcConnectsToLogicOperator(SWrapperModel sWrapperModel, ArrayList<Point> connectedPoints, Object source, String key, Arc candidate, String direction) {
		
		Arc returnArc = null;
		Glyph connectingGlyph = null;
		if (connectedPoints == null){
			connectedPoints = new ArrayList<Point>();
		}
		
		String arcId = candidate.getId();
		
		// find the glyph that the Arc is connecting to
		connectingGlyph = getGlyph(sWrapperModel, source);
		 
		// if the connecting glyph's id is the Logic Operator's id
		if (connectingGlyph != null && connectingGlyph.getId() == key){
			// if the Arc was converted to a SpeciesReferenceGlyph
			if (sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId) != null){
				// if the glyph is the Arc's source, then add the starting point of the Arc to the connectedPoints
				if (direction.equals("source")){
					connectedPoints.add(sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId).speciesReferenceGlyph.getCurve().getCurveSegment(0).getStart());
					returnArc = candidate;
				}
				// if the glyph is the Arc's target, then add the ending point of the Arc to the connectedPoints
				else if (direction.equals("target")){
					// we assume there is only one CurveSegment
					connectedPoints.add(sWrapperModel.getSWrapperSpeciesReferenceGlyph(arcId).speciesReferenceGlyph.getCurve().getCurveSegment(0).getEnd());
					returnArc = candidate;
				}
			} 
			// if the Arc was converted to a ReferenceGlyph
			else if (sWrapperModel.getSWrapperReferenceGlyph(arcId) != null){
				// if the glyph is the Arc's source, then add the starting point of the Arc to the connectedPoints
				if (direction.equals("source")){
					connectedPoints.add(sWrapperModel.getSWrapperReferenceGlyph(arcId).referenceGlyph.getCurve().getCurveSegment(0).getStart());
					returnArc = candidate;
				}
				// if the glyph is the Arc's target, then add the ending point of the Arc to the connectedPoints
				else if (direction.equals("target")){
					// we assume there is only one CurveSegment
					connectedPoints.add(sWrapperModel.getSWrapperReferenceGlyph(arcId).referenceGlyph.getCurve().getCurveSegment(0).getEnd());
					returnArc = candidate;
				}				
			}
		}	
		
		return returnArc;
	}
			
	public void storeTemplateRenderInformation(SBGNML2SBMLOutput sOutput) {
		sOutput.storeTemplateLocalRenderInformation(SBGNML2SBMLOutput.loadTemplateFromFile());
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
		//converter.storeTemplateRenderInformation();
		// Convert the file
		SWrapperModel sWrapperModel = converter.convertToSBML(map);
				
		// Write converted SBML file
		SBGNML2SBMLUtil.writeSbmlFile(sbmlFileNameOutput, sWrapperModel.model);
	}

	@Override
	public GeneralModel convert(GeneralModel sbgn) throws ConversionException, ReadModelException {

	  // this method is the main method to be integrated into SBFC, it will pass an SBGN model wrapped into
	  // a GeneralModel instance and need to send back the converted SBML model into an other GeneralModel.
	  
	  if (sbgn instanceof SBGNModel) {
	    SBGNModel sbgnSbfcModel = (SBGNModel) sbgn; 
	    
	    SWrapperModel sWrapperModel = convertToSBML(sbgnSbfcModel.getSbgnModel().getMap());

	    SBMLDocument sbmlDocument = new SBMLDocument(3, 1);
        sbmlDocument.setModel(sWrapperModel.model);
        
	     return new SBMLModel(sbmlDocument);

	  } else {
	    throw new ConversionException("We expect an SBGNML model as input, got '" + sbgn.getClass().getSimpleName() + "'.");
	  }
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
		return "SBGNML to SBML";
	}

	@Override
	public String getResultExtension() {
		return "-sbml.xml";
	}

}
