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
	public SBGNML2SBML_GSOC2017 converter;
	public SWrapperModel sWrapperModel;
	public SBGNML2SBMLOutput sOutput;
	public SBGNML2SBMLUtil sUtil;
	public SBGNML2SBMLRender sRender;
	
	/**
	 * The constructor inherits objects from the SBGNML2SBML_GSOC2017 converter, and the SBGNML2SBML_GSOC2017 converter itself
	 * @param converter
	 */
	SBGNML2SBMLQual(SBGNML2SBML_GSOC2017 converter){
		this.converter = converter;
		this.sWrapperModel = converter.sWrapperModel;
		this.sOutput = converter.sOutput;
		this.sUtil = converter.sUtil;
		this.sRender = converter.sRender;
	}
	
	/**
	 * Create all the elements of an SBML <code>Model</code>, 
	 * these created objects correspond to objects in the <code>Map</code> of <code>Sbgn</code>. 
	 * i.e. each <code>Glyph</code> or <code>Arc</code> of the <code>Map</code> 
	 * is mapped to some elements of the SBML <code>Model</code>.
	 */	
	public void convertToSBMLQual(){
		
		// For more comments of the code below, please refer to SBGNML2SBML_GSOC2017.convertToSBML()
		List<Glyph> listOfGlyphs = converter.sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = converter.sWrapperModel.map.getArc();
		List<Arcgroup> listOfArcgroups = converter.sWrapperModel.map.getArcgroup();
	
		converter.addGlyphsToSWrapperModel(listOfGlyphs, listOfArcgroups);

		converter.createCompartments();
		// Note that we create QualitativeSpecies instead of Species.
		//createSpecies();	
		createQualitativeSpecies();
		
		converter.sUtil.createDefaultCompartment(converter.sOutput.model);
	
		converter.addArcsToSWrapperModel(listOfArcs, listOfArcgroups);
		
		createTransitions();
		createCompleteTransitions();
		//converter.createReactions();
		//converter.createGeneralGlyphs();

		converter.sOutput.createCanvasDimensions();
		
		converter.sRender.renderCompartmentGlyphs();
		converter.sRender.renderSpeciesGlyphs();
		converter.sRender.renderReactionGlyphs();
		converter.sRender.renderGeneralGlyphs();
		converter.sRender.renderTextGlyphs();
		
		converter.sOutput.completeModel();
		converter.sOutput.removeExtraStyles();		
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
		textGlyph = sUtil.createJsbmlTextGlyph(speciesGlyph, qualitativeSpecies.getName(), labelBbox);
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
		
	// special case: there is no logic operators
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

		
		sWrapperGeneralGlyph = converter.createOneGeneralGlyph(sWrapperArc);
		
		// Create a ReferenceGlyph
		sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperArc, sWrapperModel.getSWrapperQualitativeSpecies(sourceId).qualitativeSpecies);
		// Add the ReferenceGlyph to the generalGlyph
		sOutput.addReferenceGlyph(sWrapperGeneralGlyph.generalGlyph, sWrapperReferenceGlyph.referenceGlyph);
		//sWrapperGeneralGlyph.generalGlyph.addReferenceGlyph(sWrapperReferenceGlyph.referenceGlyph);
		// Add the ReferenceGlyph to the wrapper. This step is optional
		//System.out.println("???===sWrapperModel sWrapperGeneralGlyph "+sWrapperGeneralGlyph.id+" logicArc " + sWrapperReferenceGlyph.sWrapperArc.arc.getId());
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
			sWrapperGeneralGlyph = converter.createOneGeneralGlyph(glyph, null, true, false);
			sWrapperModel.listOfWrapperGeneralGlyphs.put(key, sWrapperGeneralGlyph);
			
			// cannot add text to GeneralGlyph!
			Bbox bbox = glyph.getBbox();
			speciesGlyph = sUtil.createJsbmlSpeciesGlyph(glyph.getId(), null, glyph.getClazz(), null, true, bbox);
			sOutput.addSpeciesGlyph(speciesGlyph);
			

			TextGlyph textGlyph = sUtil.createJsbmlTextGlyph(speciesGlyph, glyph.getClazz().toUpperCase(), null);
			sOutput.addTextGlyph(textGlyph);
			sWrapperModel.listOfWrapperSpeciesGlyphs.put(glyph.getId(), new SWrapperSpeciesGlyph(null, speciesGlyph, glyph, textGlyph));

			
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
				sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperArc, sWrapperQualitativeSpecies.speciesGlyph);
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
				sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperArc, sWrapperGeneralGlyph.generalGlyph);
				
			} else {
				sWrapperQualitativeSpecies = sWrapperModel.listOfSWrapperQualitativeSpecies.get(objectId);
				sWrapperReferenceGlyph = converter.createOneReferenceGlyph(sWrapperArc, sWrapperQualitativeSpecies.speciesGlyph);
								
			}
			
			sWrapperModel.listOfWrapperReferenceGlyphs.put(key, sWrapperReferenceGlyph);
			sOutput.addGraphicalObject(sWrapperReferenceGlyph.referenceGlyph);
		}
		// Create a Transition for a GeneralGlyph
		// If connected to a Logic Operator, should not create a new Transition or new GeneralGlyph, add to existing
		//sWrapperTransition.addReferenceGlyph
		//System.out.println(sWrapperModel.listOfWrapperGeneralGlyphs);
		//System.out.println(sWrapperModel.listOfWrapperReferenceGlyphs);
		for (String key : sWrapperModel.logicOperators.keySet()) {
			
			GeneralGlyph generalGlyph = new GeneralGlyph();
			ArrayList<Point> connectedPoints = new ArrayList<Point>();
			List<Arc> allArcs = sWrapperModel.map.getArc();
			

			Arc chosenArc = null;
			
			for (Arc candidate: allArcs){

				Object source = candidate.getSource();
				Object target = candidate.getTarget();
				
				// todo: move tag to separate function
				Arc tagArc = converter.checkArcConnectsToLogicOperator(connectedPoints, source, key, candidate, "source");
				
				tagArc = converter.checkArcConnectsToLogicOperator(connectedPoints, target, key, candidate, "target");

			}
			
			Curve curve = new Curve();
			LineSegment curveSegment = new LineSegment();
			curveSegment.createEnd();
			curveSegment.createStart();
			curve.addCurveSegment(curveSegment);
			generalGlyph.setCurve(curve);
			generalGlyph.setBoundingBox(sWrapperModel.listOfWrapperSpeciesGlyphs.get(key).speciesGlyph.getBoundingBox());
			
			System.out.println("%%%createTransitions "+connectedPoints.size());
			converter.setStartAndEndPointForCurve(connectedPoints, generalGlyph);
			sOutput.addGeneralGlyph(generalGlyph);			
			
		}		
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
			
			if (sWrapperTransition.outputClazz == null){continue;}
			if (sWrapperTransition.outputClazz.equals("necessary stimulation")){
				resultLevel = 1;
			} // todo: ...
			
			// defaultTerm
			sUtil.addFunctionTermToTransition(sWrapperTransition.transition, true, resultLevel == 1 ? 0 : 1);	
			// functionTerm
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

		//System.out.println("buildTree parentId "+parentId+" arcs "+sWrapperModel.listOfWrapperGeneralGlyphs.get(parentId).arcs.size());
		
		for (String referenceId: sWrapperModel.listOfWrapperGeneralGlyphs.get(parentId).arcs.keySet()) {
			String childId = sWrapperModel.listOfWrapperGeneralGlyphs.get(parentId).arcs.get(referenceId).sourceId;
			//System.out.println("===buildTree parentId "+parentId+" referenceId "+referenceId+" childId "+ childId);
			
			// if logicArc that points into the parent logicOperator
			if (childId != parentId){
				//System.out.println("childId");
				//System.out.println(childId);
				
				SWrapperQualitativeSpecies child = sWrapperModel.listOfSWrapperQualitativeSpecies.get(childId);
				if (child != null){
					//System.out.println("buildTree parentId "+parentId+ " childId "+ leaves.get(childId));
					parent.addChild(leaves.get(childId));
				} else {
					// create a new ASTNode, and add to parent
					ASTNode childNode = sUtil.createMath(parent, sWrapperModel.listOfWrapperGeneralGlyphs.get(childId).clazz);
					buildTree(sWrapperGeneralGlyph, childId, childNode, leaves);
				}
			}
		}		
	}
	
	public SWrapperQualitativeSpecies isQualitativeSpecies(String id){
		for (String key : sWrapperModel.listOfSWrapperQualitativeSpecies.keySet()){
			SWrapperQualitativeSpecies sWrapperQualitativeSpecies = sWrapperModel.listOfSWrapperQualitativeSpecies.get(key);
			if (key.equals(id)){return sWrapperQualitativeSpecies;}
		}
		return null;
	}
	
	public void createOneCompleteTransition(SWrapperGeneralGlyph sWrapperGeneralGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		SWrapperReferenceGlyph sWrapperReferenceGlyph;
		boolean createANewTransition = false;
		//assume only 1 output exists for each transition
		SWrapperQualitativeSpecies output = null;
		SWrapperReferenceGlyph outputModifierArc = null;
		for (String key: sWrapperModel.listOfWrapperReferenceGlyphs.keySet()) {

			sWrapperReferenceGlyph = sWrapperModel.listOfWrapperReferenceGlyphs.get(key);
			String generalGlyphId = sWrapperGeneralGlyph.id;
			String sourceId = sWrapperReferenceGlyph.sWrapperArc.sourceId;
			String targetId = sWrapperReferenceGlyph.sWrapperArc.targetId;
			
			if (generalGlyphId.equals(sourceId) || generalGlyphId.equals(targetId)){
				if (sWrapperReferenceGlyph.arc.getClazz().equals("necessary stimulation")){
					createANewTransition = true;
				} // todo: etc...
				
				if (isQualitativeSpecies(targetId) != null){
					output = isQualitativeSpecies(targetId);
					outputModifierArc = sWrapperReferenceGlyph;
				}
				
			} else {
				continue;
			}
			

		}
		
		if (createANewTransition){
			SWrapperTransition sWrapperTransition = createOneTransition(sWrapperGeneralGlyph.glyph, sWrapperGeneralGlyph);
			sWrapperModel.addSWrapperTransition(sWrapperGeneralGlyph.id, sWrapperTransition);
			
			createOneCompleteTransition(sWrapperTransition, sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
			
			if (output != null){
				sWrapperTransition.outputs.put(output.qualitativeSpecies.getId(), output);
				sWrapperTransition.outputClazz = output.clazz;
				sWrapperTransition.outputModifierArc = outputModifierArc;
			}
			System.out.println("===createOneCompleteTransition ops "+ sWrapperTransition.listOfWrapperGeneralGlyphs.size()+" refs "+sWrapperTransition.listOfWrapperReferenceGlyphs.size());
		}
		

//		System.out.println(sWrapperTransition.id);
//		System.out.println(sWrapperTransition.listOfWrapperGeneralGlyphs);
//		System.out.println(sWrapperTransition.listOfWrapperReferenceGlyphs+ "\n");
	}
	
	public void createOneCompleteTransition(SWrapperTransition sWrapperTransition,
			SWrapperGeneralGlyph sWrapperGeneralGlyph,
			HashMap<String, SWrapperGeneralGlyph> listOfWGeneralGlyphs,
			HashMap<String, SWrapperReferenceGlyph> listOfWReferenceGlyphs){
		
		//System.out.println(" sWrapperGeneralGlyph="+sWrapperGeneralGlyph.id+" sWrapperTransition="+sWrapperTransition.id+" listOfWGeneralGlyphs "+listOfWGeneralGlyphs.size()+" listOfWReferenceGlyphs "+listOfWReferenceGlyphs.size());
		
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
			
			//if (generalGlyphId.equals(sourceId) || generalGlyphId.equals(targetId)){
			if (generalGlyphId.equals(targetId)){
				sWrapperTransition.addReference(sWrapperReferenceGlyph, sWrapperReferenceGlyph.sWrapperArc);
				listOfWReferenceGlyphs.put(key, null);
				
				recursiveDepth++;
				if (recursiveDepth > 10){System.out.println("!! sWrapperGeneralGlyph sWrapperTransition="+sWrapperTransition.id); return;}
				createOneCompleteTransition(sWrapperTransition, sWrapperReferenceGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
				// store any Arc connected to GeneralGlyph
				
				System.out.println("???sWrapperModel sWrapperGeneralGlyph "+sWrapperGeneralGlyph.id+" logicArc " + sWrapperReferenceGlyph.sWrapperArc.arc.getId());
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
			
			//if (generalGlyphId.equals(sourceId) || generalGlyphId.equals(targetId)){
			if (generalGlyphId.equals(sourceId) ){
				//System.out.println("=== "+sourceId);
				
				sWrapperTransition.addGeneralGlyph(generalGlyphId, sWrapperGeneralGlyph, sWrapperGeneralGlyph.glyph);
				listOfWGeneralGlyphs.put(key, null);
				
				recursiveDepth++;
				if (recursiveDepth > 10){System.out.println("!! sWrapperReferenceGlyph sWrapperTransition="+sWrapperTransition.id); return;}
				createOneCompleteTransition(sWrapperTransition, sWrapperGeneralGlyph, listOfWGeneralGlyphs, listOfWReferenceGlyphs);
				
			} else {
				continue;
			}
		}		
	}
	
	int recursiveDepth = 0;
	
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
		SBGNML2SBML_GSOC2017 converter;		
		
		map = sbgnObject.getMap();	
		// optional
		//SBGNML2SBMLUtil.debugSbgnObject(map);
		
		// Create a new converter
		converter = new SBGNML2SBML_GSOC2017(map);
		// Load a template file containing predefined RenderInformation
		converter.storeTemplateRenderInformation();
		// Convert the file
		//converter.convertToSBML();
		
		SBGNML2SBMLQual converterQual = new SBGNML2SBMLQual(converter);
		converterQual.convertToSBMLQual();
		
		
		// Write converted SBML file
		SBGNML2SBMLUtil.writeSbmlFile(sbmlFileNameOutput, converter.sOutput.model);
	}
		
}
