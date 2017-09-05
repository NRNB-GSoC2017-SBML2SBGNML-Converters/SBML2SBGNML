package org.sbfc.converter.sbgnml2sbml.qual;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLOutput;
import org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLRender;
import org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLUtil;
import org.sbfc.converter.sbgnml2sbml.SBGNML2SBML_GSOC2017;
import org.sbfc.converter.sbgnml2sbml.SWrapperArc;
import org.sbfc.converter.sbgnml2sbml.SWrapperGeneralGlyph;
import org.sbfc.converter.sbgnml2sbml.SWrapperModel;
import org.sbfc.converter.sbgnml2sbml.SWrapperReferenceGlyph;
import org.sbfc.converter.sbgnml2sbml.SWrapperSpeciesGlyph;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

/**
 * The SBGNML2SBMLQual class is an extended converter of SBGNML2SBML_GSOC2017,
 * for converting from SBGN to SBML qual+layout+render. 
 * It converts from a libSBGN Sbgn object to the JSBML Model and its extensions. 
 * Model elements are added as the converter interprets the input libSBGN Sbgn.Map.
 * 
 * @author haoran
 *
 */
public class SBGNML2SBMLQual {
//	public SBGNML2SBML_GSOC2017 converter;
//	public SWrapperModel sWrapperModel;
//	public SBGNML2SBMLOutput sOutput;
//	public SBGNML2SBMLUtil sUtil;
//	public SBGNML2SBMLRender sRender;
	
	/**
	 * The constructor inherits objects from the SBGNML2SBML_GSOC2017 converter, and the SBGNML2SBML_GSOC2017 converter itself
	 * @param converter
	 */
	SBGNML2SBMLQual(){
//		this.converter = converter;
//		this.sWrapperModel = converter.sWrapperModel;
//		this.sOutput = converter.sOutput;
//		this.sUtil = converter.sUtil;
//		this.sRender = converter.sRender;
	}
	
	/**
	 * Create all the elements of an SBML <code>Model</code>, 
	 * these created objects correspond to objects in the <code>Map</code> of <code>Sbgn</code>. 
	 * i.e. each <code>Glyph</code> or <code>Arc</code> of the <code>Map</code> 
	 * is mapped to some elements of the SBML <code>Model</code>.
	 */	
	public SWrapperModel convertToSBMLQual(Map map){
		SBGNML2SBML_GSOC2017 converter;
		
		// Create a new converter
		converter = new SBGNML2SBML_GSOC2017();

		SBGNML2SBMLOutput sOutput = new SBGNML2SBMLOutput(3, 1);
		SWrapperModel sWrapperModel = new SWrapperModel(sOutput.getModel(), map);
		
		// Load a template file containing predefined RenderInformation
		converter.storeTemplateRenderInformation(sOutput);
		
		// For more comments of the code below, please refer to SBGNML2SBML_GSOC2017.convertToSBML()
		List<Glyph> listOfGlyphs = sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = sWrapperModel.map.getArc();
		List<Arcgroup> listOfArcgroups = sWrapperModel.map.getArcgroup();
	
		converter.addGlyphsToSWrapperModel(sWrapperModel, listOfGlyphs, listOfArcgroups);

		converter.createCompartments(sWrapperModel, sOutput);
		// Note that we create QualitativeSpecies instead of Species.
		//createSpecies();	
		createQualitativeSpecies(sWrapperModel, sOutput);
		
		SBGNML2SBMLUtil.createDefaultCompartment(sOutput.model);
	
		converter.addArcsToSWrapperModel(sWrapperModel, listOfArcs, listOfArcgroups);
		
		createTransitions(sWrapperModel, sOutput, converter);
		createCompleteTransitions(sWrapperModel, sOutput);
		// Note that there are no ReactionGlyphs in SBGN AF
		//converter.createReactions();
		//converter.createGeneralGlyphs();

		sOutput.createCanvasDimensions();
		
		SBGNML2SBMLRender.renderCompartmentGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderSpeciesGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderReactionGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderGeneralGlyphs(sWrapperModel, sOutput);
		SBGNML2SBMLRender.renderTextGlyphs(sWrapperModel, sOutput);
		
		sOutput.completeModel();
		sOutput.removeExtraStyles();	
		
		return sWrapperModel;
	}
	
	/**
	 * Create a QualitativeSpecies, a SpeciesGlyph and TextGlyph for the Sbgn glyph
	 * @param glyph
	 * @return sWrapperQualitativeSpecies
	 */
	public SWrapperQualitativeSpecies createOneQualitativeSpecies(SBGNML2SBMLOutput sOutput, Glyph glyph) {
		QualitativeSpecies qualitativeSpecies;
		SpeciesGlyph speciesGlyph;
		String speciesId;
		String name;
		String clazz; 
		Bbox bbox;
		TextGlyph textGlyph;
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		
		name = SBGNML2SBMLUtil.getText(glyph);
		clazz = glyph.getClazz();
		speciesId = glyph.getId();
		
		// create a QualitativeSpecies, add it to the output
		qualitativeSpecies = SBGNML2SBMLUtil.createQualitativeSpecies(speciesId, name, clazz, false, true);
		sOutput.addQualitativeSpecies(qualitativeSpecies);
		
		// create a SpeciesGlyph, add it to the output 
		bbox = glyph.getBbox();
		speciesGlyph = SBGNML2SBMLUtil.createJsbmlSpeciesGlyph(speciesId, name, clazz, null, true, bbox);
		sOutput.addSpeciesGlyph(speciesGlyph);
		
		// create TextGlyph for the SpeciesGlyph, add it to the output 
		Bbox labelBbox = glyph.getLabel().getBbox();
		textGlyph = SBGNML2SBMLUtil.createJsbmlTextGlyph(speciesGlyph, qualitativeSpecies.getName(), labelBbox);
		sOutput.addTextGlyph(textGlyph);
		
		// create a new SWrapperSpeciesGlyph class to store the created QualitativeSpecies, SpeciesGlyph, and TextGlyph
		sWrapperQualitativeSpecies =  new SWrapperQualitativeSpecies(qualitativeSpecies, speciesGlyph, glyph, textGlyph);
		
		return sWrapperQualitativeSpecies;
	}	
	
	/**
	 * Create all the QualitativeSpecies and SpeciesGlyph in the model
	 */
	public void createQualitativeSpecies(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput){
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		Glyph glyph;
		for (String key : sWrapperModel.entityPoolNodes.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			sWrapperQualitativeSpecies = createOneQualitativeSpecies(sOutput, glyph);
			sWrapperModel.addSWrapperQualitativeSpecies(key, sWrapperQualitativeSpecies);
		}
	}
		
	/**
	 * Create a Transition using an Sbgn arc
	 * this is a special case: there are no logic operators, only a single arc (see modulation.sbgn)
	 * @param sWrapperArc
	 * @return
	 */
	public SWrapperTransition createOneTransition(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, SBGNML2SBML_GSOC2017 converter, SWrapperArc sWrapperArc) {
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		
		String sourceId;
		String targetId;
		Transition transition;
		SWrapperTransition sWrapperTransition;
		
		// The ids of the source Glyph and the target Glyph
		sourceId = sWrapperArc.sourceId;
		targetId = sWrapperArc.targetId;
		
		// check that the conditions for this special case holds
		if (!(sWrapperModel.getSWrapperQualitativeSpecies(sourceId) != null && 
				sWrapperModel.getSWrapperQualitativeSpecies(targetId) != null)){
			return null;
		}
		
		// Create a Transition
		// No BoundingBox for this special case because we have only an arc in this transition
		transition = SBGNML2SBMLUtil.createTransition(sWrapperArc.arcId, 
				sWrapperModel.getSWrapperQualitativeSpecies(sourceId).qualitativeSpecies,
				sWrapperModel.getSWrapperQualitativeSpecies(targetId).qualitativeSpecies);
		sOutput.addTransition(transition);

		// this arc will be converted to a GeneralGlyph containing one ReferenceGlyph
		sWrapperGeneralGlyph = converter.createOneGeneralGlyph(sWrapperModel, sOutput, sWrapperArc);
		
		// Create a ReferenceGlyph
		sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperModel, sOutput, sWrapperArc, sWrapperModel.getSWrapperQualitativeSpecies(sourceId).qualitativeSpecies);
		// Add the ReferenceGlyph to the generalGlyph
		sOutput.addReferenceGlyph(sWrapperGeneralGlyph.generalGlyph, sWrapperReferenceGlyph.referenceGlyph);

		// Add the ReferenceGlyph to the wrapper. This step is optional
		sWrapperGeneralGlyph.addSpeciesReferenceGlyph(sWrapperArc.arc.getId(), sWrapperReferenceGlyph, sWrapperArc);
		
		// Add the GeneralGlyph created to the output
		sOutput.addGeneralGlyph(sWrapperGeneralGlyph.generalGlyph);
		
		// create a new SWrapperGeneralGlyph
		sWrapperTransition = new SWrapperTransition(transition, sWrapperGeneralGlyph, sWrapperArc, sWrapperModel);
		
		return sWrapperTransition;	
	} 	
	
	/**
	 * Create a Transition for the general case where we have logic operators and logic arcs
	 * An empty Transition is created, the contents of the Transition will be modified by other functions later.
	 * @param logicOperator
	 * @param sWrapperGeneralGlyph
	 * @return
	 */
	public SWrapperTransition createOneTransition(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, Glyph logicOperator, SWrapperGeneralGlyph sWrapperGeneralGlyph){
		String clazz;
		String id;
		Transition transition;
		SWrapperTransition sWrapperTransition;	
		
		clazz = logicOperator.getClazz();
		id = logicOperator.getId();
		
		transition = SBGNML2SBMLUtil.createTransition(id);
		sOutput.addTransition(transition);	
		
		sWrapperTransition = new SWrapperTransition(id, transition, sWrapperGeneralGlyph, logicOperator, sWrapperModel);
		
		return sWrapperTransition;
	}
	
	/**
	 * Create all the associated GeneralGlyphs (for logic operators) and ReferenceGlyphs (for logic arcs) associated with Transitions in
	 * the model. There 4 steps to create all the SBML layout Glyphs: 
	 * 1. convert logic operators to GeneralGlyphs, 
	 * 2. convert the modifier arc (that connects to the output QualitativeSpecies) to a ReferenceGlyph, 
	 * 3. convert the logic arcs (that connects between logic operators, or between input QualitativeSpecies and 
	 * 		logic operator) to a ReferenceGlyph, 
	 * 4. create a center Curve for each of the converted logic operators so that they look like ReactionGlyphs (see 
	 * 		CreateGeneralGlyphs in SBGNML2SBML_GSOC2017 for more detail)
	 * @return
	 */
	public int createTransitions(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, SBGNML2SBML_GSOC2017 converter) {
		Arc arc;
		String objectId;
		SpeciesGlyph speciesGlyph;
		SWrapperTransition sWrapperTransition;
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		SWrapperArc sWrapperArc;
		Glyph glyph;
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		
		// 1. convert logic operators to GeneralGlyphs
		for (String key : sWrapperModel.logicOperators.keySet()) {
			glyph = sWrapperModel.getGlyph(key);
			// create a new GeneralGlyph, don't add to output.
			sWrapperGeneralGlyph = converter.createOneGeneralGlyph(sWrapperModel, sOutput, glyph, null, true, false);
			sWrapperModel.listOfWrapperGeneralGlyphs.put(key, sWrapperGeneralGlyph);
			
			// cannot add text to GeneralGlyph (this is a bug), so here a workaround:
			// we create a SpeciesGlyph instead of a GeneralGlyph
			Bbox bbox = glyph.getBbox();
			speciesGlyph = SBGNML2SBMLUtil.createJsbmlSpeciesGlyph(glyph.getId(), null, glyph.getClazz(), null, true, bbox);
			sOutput.addSpeciesGlyph(speciesGlyph);
			
			// create a TextGlyph
			TextGlyph textGlyph = SBGNML2SBMLUtil.createJsbmlTextGlyph(speciesGlyph, glyph.getClazz().toUpperCase(), null);
			sOutput.addTextGlyph(textGlyph);
			
			sWrapperModel.listOfWrapperSpeciesGlyphs.put(glyph.getId(), new SWrapperSpeciesGlyph(null, speciesGlyph, glyph, textGlyph));
		}			
		
		// 2. convert the modifier arc (that connects to the output QualitativeSpecies) to a ReferenceGlyph
		for (String key: sWrapperModel.modifierArcs.keySet()) {
			sWrapperArc = sWrapperModel.modifierArcs.get(key);
			arc = sWrapperArc.arc;
			
			// Create a transition of each modifier arc (that means, when we see a modifier arc, we will be have a set of
			// logic operators and arcs that forms a transition. And though the modifier arc, connects to the output)
			// assume: each Transition has a single output
			sWrapperTransition = createOneTransition(sWrapperModel, sOutput, converter, sWrapperArc);
			
			// check if we have the special case (the Arc is not connected to a Logic Operator)
			if (sWrapperTransition != null){
				// we have the common case
				sWrapperModel.addSWrapperTransition(arc.getId(), sWrapperTransition);
				
			} else{
				// we have the special case, so createOneTransition() returned null
				objectId = sWrapperArc.targetId;
				sWrapperQualitativeSpecies = sWrapperModel.listOfSWrapperQualitativeSpecies.get(objectId);
				sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperModel, sOutput, sWrapperArc, sWrapperQualitativeSpecies.speciesGlyph);
				sWrapperModel.listOfWrapperReferenceGlyphs.put(key, sWrapperReferenceGlyph);
				sOutput.addGraphicalObject(sWrapperReferenceGlyph.referenceGlyph);
				
			}			
		}
		
		// 3. create a GeneralGlyph for each Logic Arc
		for (String key: sWrapperModel.logicArcs.keySet()) {
			sWrapperArc = sWrapperModel.logicArcs.get(key);
			arc = sWrapperArc.arc;
			
			// assume Arc.Source corresponds to Arc.Start (i.e. source position = start position)
			objectId = sWrapperArc.sourceId;
			sWrapperGeneralGlyph = sWrapperModel.listOfWrapperGeneralGlyphs.get(objectId);
			
			// the ReferenceGlyph.glyph could be set to a GeneralGlyph or a SpeciesGlyph, it depends on what object type (a GeneralGlyph or a 
			// SpeciesGlyph) we created earlier
			if (sWrapperGeneralGlyph != null){
				sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperModel, sOutput, sWrapperArc, sWrapperGeneralGlyph.generalGlyph);
				
			} else {
				sWrapperQualitativeSpecies = sWrapperModel.listOfSWrapperQualitativeSpecies.get(objectId);
				sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperModel, sOutput, sWrapperArc, sWrapperQualitativeSpecies.speciesGlyph);
								
			}
			
			sWrapperModel.listOfWrapperReferenceGlyphs.put(key, sWrapperReferenceGlyph);
			sOutput.addGraphicalObject(sWrapperReferenceGlyph.referenceGlyph);
		}
		
		// 4. create a center Curve for each of the converted logic operators
		// see CreateGeneralGlyphs in SBGNML2SBML_GSOC2017 for more detailed comments
		for (String key : sWrapperModel.logicOperators.keySet()) {
			
			GeneralGlyph generalGlyph = new GeneralGlyph();
			ArrayList<Point> connectedPoints = new ArrayList<Point>();
			List<Arc> allArcs = sWrapperModel.map.getArc();

			Arc chosenArc = null;
			for (Arc candidate: allArcs){
				Object source = candidate.getSource();
				Object target = candidate.getTarget();
				Arc tagArc = converter.checkArcConnectsToLogicOperator(sWrapperModel, connectedPoints, source, key, candidate, "source");
				tagArc = converter.checkArcConnectsToLogicOperator(sWrapperModel, connectedPoints, target, key, candidate, "target");
			}
			
			Curve curve = new Curve();
			LineSegment curveSegment = new LineSegment();
			curveSegment.createEnd();
			curveSegment.createStart();
			curve.addCurveSegment(curveSegment);
			generalGlyph.setCurve(curve);
			generalGlyph.setBoundingBox(sWrapperModel.listOfWrapperSpeciesGlyphs.get(key).speciesGlyph.getBoundingBox());
			
			//System.out.println("%%%createTransitions "+connectedPoints.size());
			converter.setStartAndEndPointForCurve(connectedPoints, generalGlyph);
			sOutput.addGeneralGlyph(generalGlyph);			
		}		
		
		return sWrapperModel.listOfWrapperGeneralGlyphs.size() + sWrapperModel.listOfWrapperReferenceGlyphs.size();
	}
	
	/**
	 * The function adds more semantics to the Transition we created in createTransitions():
	 * Add the input and output QualitativeSpecies to the Transition we created,
	 * Add DefaultTerm and FunctionTerm to the transition and 
	 * Add a MathML describing the layout of the logic network in terms of boolean equations (note that this equation is recursive
	 * so that the ASTNode tree in MathML can be of any depth)
	 * For example, convert compartment_extended.sbgn 
	 */
	public void createCompleteTransitions(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput){
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		SWrapperTransition sWrapperTransition;
		
		// initialize some data structures for use in our recursive depth-first search functions to create MathML for a FunctionTerm
		HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs = 
				new HashMap<String, SWrapperGeneralGlyph>();
		HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs = 
				new HashMap<String, SWrapperReferenceGlyph>();

		// Create a FunctionTerm for each logic operator that connects to a modifier arc 
		// note that if the logic operator does not connect to a modifier arc, we don't create a new FunctionTerm because we know 
		// that the logic operator is part of a transition that we already created a FunctionTerm for.
		for (String key: sWrapperModel.listOfWrapperGeneralGlyphs.keySet()) {
			sWrapperGeneralGlyph = listOfWGeneralGlyphs.get(key);
			if (sWrapperGeneralGlyph != null){
				continue;
			}
			sWrapperGeneralGlyph = sWrapperModel.listOfWrapperGeneralGlyphs.get(key);
			// run the recursive functions to find all the objects in a logic network of the transition
			// then create a Transition wrapper and add it to the model wrapper
			// note that the Transition does not have any values stored, we need to add them in the next step 
			createOneCompleteTransition(sWrapperModel, sOutput, sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
		}
		
		// Add the input and output QualitativeSpecies, FunctionTerm, and MathML to the transition
		for (String key: sWrapperModel.listOfSWrapperTransitions.keySet()) {
			sWrapperTransition = sWrapperModel.listOfSWrapperTransitions.get(key);
			
			// the leaves are the input QualitativeSpecies
			HashMap<String, ASTNode> leaves = new HashMap<String, ASTNode>();
			
			for (String inputId : sWrapperTransition.inputs.keySet()){
				SWrapperQualitativeSpecies input = sWrapperTransition.inputs.get(inputId);
				Input tInput = SBGNML2SBMLUtil.addInputToTransition(sWrapperTransition.transition, input.qualitativeSpecies);
				
				// create a ASTNode for each input QualitativeSpecies, so that we can add them to the MathML later
				ASTNode functionTermMath = new ASTNode(tInput.getId());
				leaves.put(inputId, functionTermMath);
			}
			
			for (String outputId : sWrapperTransition.outputs.keySet()){
				SWrapperQualitativeSpecies output = sWrapperTransition.outputs.get(outputId);
				SBGNML2SBMLUtil.addOutputToTransition(sWrapperTransition.transition, output.qualitativeSpecies);
			}
			
			// guess the resultLevel of the functionTerms we create 
			int resultLevel = 0;
			
			if (sWrapperTransition.outputClazz == null){
				continue;
			} if (sWrapperTransition.outputClazz.equals("necessary stimulation")){
				resultLevel = 1;
			} if (sWrapperTransition.outputClazz.equals("positive influence")){
				resultLevel = 1;
			} if (sWrapperTransition.outputClazz.equals("negative influence")){
				resultLevel = 0;
			} if (sWrapperTransition.outputClazz.equals("unknown influence")){
				// assume there is a increasing effect (an increasing effect in the positive or the negative direction)
				resultLevel = 1;
			}
			
			// create empty functionTerms
			// add a defaultTerm
			SBGNML2SBMLUtil.addFunctionTermToTransition(sWrapperTransition.transition, true, resultLevel == 1 ? 0 : 1);	
			// functionTerm
			FunctionTerm functionTerm;
			functionTerm = SBGNML2SBMLUtil.addFunctionTermToTransition(sWrapperTransition.transition, false, resultLevel == 1 ? 1 : 0);
			
			// get the logicOperator that immediately precedes the output modifier Arc
			String rootId = sWrapperTransition.outputModifierArc.sWrapperArc.sourceId;
			String rootClazz = sWrapperTransition.listOfWrapperGeneralGlyphs.get(rootId).clazz;
			
			// create the root ASTNode, set its container to the functionTerm
			ASTNode rootNode = SBGNML2SBMLUtil.createMath(rootClazz, functionTerm);
			sWrapperTransition.rootFunctionTerm = rootNode;
						
			// get the root's children, by finding all the Arcs connected to the root node, they are stored in sWrapperGeneralGlyph
			sWrapperGeneralGlyph = sWrapperTransition.listOfWrapperGeneralGlyphs.get(rootId);
			
			// create the MathML using all the provided information we have in the sWrapperGeneralGlyph
			buildTree(sWrapperModel, sWrapperGeneralGlyph, rootId, rootNode, leaves);

			functionTerm.setMath(rootNode);
		}
	}
	
	/**
	 * Create the MathML element in the FunctionTerm, all the nodes are stored recursively in the parent ASTNode.
	 * @param sWrapperGeneralGlyph
	 * @param parentId
	 * @param parent
	 * @param leaves
	 */
	public void buildTree(SWrapperModel sWrapperModel, SWrapperGeneralGlyph sWrapperGeneralGlyph, String parentId, ASTNode parent, 
			HashMap<String, ASTNode> leaves){

		// finding all the Arcs connected to the parent node
		for (String referenceId: sWrapperModel.listOfWrapperGeneralGlyphs.get(parentId).arcs.keySet()) {
			String childId = sWrapperModel.listOfWrapperGeneralGlyphs.get(parentId).arcs.get(referenceId).sourceId;

			// if this logicArc points "into" the parent logicOperator (the arrow points to the logicOperator)
			if (childId != parentId){
				
				SWrapperQualitativeSpecies child = sWrapperModel.listOfSWrapperQualitativeSpecies.get(childId);
				
				if (child != null){
					// we have reached the leaf the the tree, stop recursing
					parent.addChild(leaves.get(childId));
				} else {
					// create a new ASTNode, and add to parent
					ASTNode childNode = SBGNML2SBMLUtil.createMath(parent, sWrapperModel.listOfWrapperGeneralGlyphs.get(childId).clazz);
					
					// recurse
					buildTree(sWrapperModel, sWrapperGeneralGlyph, childId, childNode, leaves);
				}
			}
		}		
	}
	
	public SWrapperQualitativeSpecies isQualitativeSpecies(SWrapperModel sWrapperModel, String id){
		for (String key : sWrapperModel.listOfSWrapperQualitativeSpecies.keySet()){
			SWrapperQualitativeSpecies sWrapperQualitativeSpecies = sWrapperModel.listOfSWrapperQualitativeSpecies.get(key);
			if (key.equals(id)){
				return sWrapperQualitativeSpecies;
			}
		}
		return null;
	}
	
	/**
	 * Create a new Transition, store the input and output QualitativeSpecies and all objects (logic operators and logic arcs) 
	 * in the Transition wrapper, so that we can add them to the Transition later.
	 * Run the recursive functions to find all the objects in a logic network of the transition,
	 * then create a Transition wrapper and add it to the model wrapper. 
	 * Note that the Transition does not have any values stored, we need to add them in another function.
	 * @param sWrapperGeneralGlyph
	 * @param listOfWGeneralGlyphs
	 * @param listOfWReferenceGlyphs
	 * @return
	 */
	public SWrapperTransition createOneCompleteTransition(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, SWrapperGeneralGlyph sWrapperGeneralGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		boolean createANewTransition = false;
		//assume only 1 output exists for each transition
		SWrapperQualitativeSpecies output = null;
		SWrapperReferenceGlyph outputModifierArc = null;
		
		// search all the ReferenceGlyphs in the model, find the output QualitativeSpecies and the output modifier ReferenceGlyph
		for (String key: sWrapperModel.listOfWrapperReferenceGlyphs.keySet()) {

			sWrapperReferenceGlyph = sWrapperModel.listOfWrapperReferenceGlyphs.get(key);
			String generalGlyphId = sWrapperGeneralGlyph.id;
			String sourceId = sWrapperReferenceGlyph.sWrapperArc.sourceId;
			String targetId = sWrapperReferenceGlyph.sWrapperArc.targetId;
			
			if (generalGlyphId.equals(sourceId) || generalGlyphId.equals(targetId)){
				// if ReferenceGlyph is a modifier arc, we create a new transition
				if (sWrapperReferenceGlyph.arc.getClazz().equals("necessary stimulation")){
					createANewTransition = true;
				} if (sWrapperReferenceGlyph.arc.getClazz().equals("positive influence")){
					createANewTransition = true;
				} if (sWrapperReferenceGlyph.arc.getClazz().equals("negative influence")){
					createANewTransition = true;
				} if (sWrapperReferenceGlyph.arc.getClazz().equals("unknown influence")){
					// assume there is a increasing effect (an increasing effect in the positive or the negative direction)
					createANewTransition = true;
				}
				
				// the output QualitativeSpecies and the output modifier ReferenceGlyph
				if (isQualitativeSpecies(sWrapperModel, targetId) != null){
					output = isQualitativeSpecies(sWrapperModel, targetId);
					outputModifierArc = sWrapperReferenceGlyph;
				}
				
			} else {
				continue;
			}
		}
		
		// Create a new Transition, run the recursive createOneCompleteTransition() to find all objects (logic operators and logic arcs)
		// involved in the Transition and add them to the Transition wrapper.
		if (createANewTransition){
			SWrapperTransition sWrapperTransition = createOneTransition(sWrapperModel, sOutput, sWrapperGeneralGlyph.glyph, sWrapperGeneralGlyph);
			sWrapperModel.addSWrapperTransition(sWrapperGeneralGlyph.id, sWrapperTransition);
			
			// recursively store objects into sWrapperTransition
			createOneCompleteTransition(sWrapperModel, sWrapperTransition, sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
			
			if (output != null){
				sWrapperTransition.outputs.put(output.qualitativeSpecies.getId(), output);
				sWrapperTransition.outputClazz = output.clazz;
				sWrapperTransition.outputModifierArc = outputModifierArc;
			}
			//System.out.println("===createOneCompleteTransition ops "+ sWrapperTransition.listOfWrapperGeneralGlyphs.size()+" refs "+sWrapperTransition.listOfWrapperReferenceGlyphs.size());
			
			return sWrapperTransition;
		}
		
		 return null;
	}
	
	int recursiveDepth = 0;
	
	/**
	 * Find all arcs connected to the sWrapperGeneralGlyph.glyph, and call the recursive function createOneCompleteTransition.
	 * @param sWrapperTransition
	 * @param sWrapperGeneralGlyph the logic operator that we are considering, we want to find all logic arcs that point "into" this logic operator
	 * @param listOfWGeneralGlyphs store all the logic operators we added to the transition
	 * @param listOfWReferenceGlyphs store all the logic arcs we added to the transition
	 */
	public void createOneCompleteTransition(SWrapperModel sWrapperModel, SWrapperTransition sWrapperTransition,
			SWrapperGeneralGlyph sWrapperGeneralGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		//System.out.println(" sWrapperGeneralGlyph="+sWrapperGeneralGlyph.id+" sWrapperTransition="+sWrapperTransition.id+" listOfWGeneralGlyphs "+listOfWGeneralGlyphs.size()+" listOfWReferenceGlyphs "+listOfWReferenceGlyphs.size());
		
		// if we added all the ReferenceGlyphs to the Transitions, we know we are done. Break the recursion
		if (listOfWReferenceGlyphs.size() == sWrapperModel.listOfWrapperReferenceGlyphs.size()){
			return;
		}
		
		// store any logic arcs connected to sWrapperGeneralGlyph.glyph
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
			
			// if the arc's arrow points to the sWrapperGeneralGlyph.glyph, then we know the arc is part of the transition
			if (generalGlyphId.equals(targetId)){
				sWrapperTransition.addReference(sWrapperReferenceGlyph, sWrapperReferenceGlyph.sWrapperArc);
				// store this ReferenceGlyph to this transition
				listOfWReferenceGlyphs.put(key, null);
				
				recursiveDepth++;
				if (recursiveDepth > 10){
					System.out.println("!! sWrapperGeneralGlyph sWrapperTransition="+sWrapperTransition.id); 
					return;
				}
				
				// recurse
				createOneCompleteTransition(sWrapperModel, sWrapperTransition, sWrapperReferenceGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
				
				// we store the sWrapperReferenceGlyph to the sWrapperGeneralGlyph
				sWrapperGeneralGlyph.addSpeciesReferenceGlyph(key, sWrapperReferenceGlyph, sWrapperReferenceGlyph.sWrapperArc);
				//System.out.println("???sWrapperModel sWrapperGeneralGlyph "+sWrapperGeneralGlyph.id+" logicArc " + sWrapperReferenceGlyph.sWrapperArc.arc.getId());
				
			} else {
				continue;
			}
		}
	}
	
	/**
	 * Find all glyohs connected to the sWrapperReferenceGlyph.arc, and call the recursive function createOneCompleteTransition. 
	 * @param sWrapperTransition
	 * @param sWrapperReferenceGlyph the logic arc we are considering, we want to find the glyph that the logic arc originates from (the source)
	 * @param listOfWGeneralGlyphs store all the logic operators we added to the transition
	 * @param listOfWReferenceGlyphs store all the logic arcs we added to the transition
	 */
	public void createOneCompleteTransition(SWrapperModel sWrapperModel, SWrapperTransition sWrapperTransition,
			SWrapperReferenceGlyph sWrapperReferenceGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		//System.out.println(" sWrapperReferenceGlyph="+sWrapperReferenceGlyph.id+" sWrapperTransition="+sWrapperTransition.id+" listOfWGeneralGlyphs "+listOfWGeneralGlyphs.size()+" listOfWReferenceGlyphs "+listOfWReferenceGlyphs.size());
		
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
			
			if (generalGlyphId.equals(sourceId) ){		
				sWrapperTransition.addGeneralGlyph(generalGlyphId, sWrapperGeneralGlyph, sWrapperGeneralGlyph.glyph);
				// store this GeneralGlyph to this transition
				listOfWGeneralGlyphs.put(key, null);
				
				recursiveDepth++;
				if (recursiveDepth > 10){
					System.out.println("!! sWrapperReferenceGlyph sWrapperTransition="+sWrapperTransition.id); 
					return;
				}
				
				// recurse
				createOneCompleteTransition(sWrapperModel, sWrapperTransition, sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
				
			} else {
				continue;
			}
		}		
	}
		
	public static void main(String[] args) throws FileNotFoundException {
		String sbgnFileNameInput;
		String sbmlFileNameOutput;
		String workingDirectory;
		
		Sbgn sbgnObject = null;

		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLQual <SBGNML filename>. ");
			return;
		}		
		
		// Read a .sbgn file
		workingDirectory = System.getProperty("user.dir");
		sbgnFileNameInput = args[0];
		sbgnFileNameInput = workingDirectory + sbgnFileNameInput;			
		sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
				
		sbgnObject = SBGNML2SBMLUtil.readSbgnFile(sbgnFileNameInput);

		Map map;
		map = sbgnObject.getMap();
		
		SBGNML2SBMLQual converterQual = new SBGNML2SBMLQual();
		// Convert the file	
		SWrapperModel sWrapperModel = converterQual.convertToSBMLQual(map);
			
		// optional
		//SBGNML2SBMLUtil.debugSbgnObject(map);
		
		// Write converted SBML file
		SBGNML2SBMLUtil.writeSbmlFile(sbmlFileNameOutput, sWrapperModel.model);
	}
		
}
