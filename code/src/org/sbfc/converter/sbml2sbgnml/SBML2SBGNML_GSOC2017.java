package org.sbfc.converter.sbml2sbgnml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBGNModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase;
import org.sbgn.bindings.Sbgn;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.PropertyUndefinedError;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGraphicalObjectPlugin;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The SBML2SBGNML_GSOC2017 class is the primary converter for converting from SBML to SBGN. The conversion interprets the
 * core, the layout, the render, and the qual packages of SBML.  
 * It converts from a JSBML Model and its extension Plugins to a libSBGN Sbgn object. 
 * Model elements are added as the converter interprets the JSBML Model.
 * @author haoran
 *
 */
public class SBML2SBGNML_GSOC2017 extends GeneralConverter { 
	private static Logger logger;
	
	// the SBGN language to convert into
	// either "process description" or "activity flow"
	//public String toLanguage = "process description"; 

	// The helper classes for conversion:
	// SBML2SBGNMLUtil contains methods that do not depend on any information in the Sbgn model. 
	// Example: finding a value from a given list.
	//public SBML2SBGNMLUtil sUtil;
	
	// SBML2SBGNMLOutput contains all data structures retrieved from the SBML Model and 
	// lists to create the output Sbgn document. 
	//public SBML2SBGNMLOutput sOutput;
	
	// A SWrapperMap Sbgn.Map wrapper stores the Map as well as lists of glyphs and arcs contained in the Map. 
	// SWrapperMap allows to retrieve information easily without having to search in the Map
	// SWrapperMap stores SWrapperArc, SWrapperGlyphEntityPool, etc, which are Wrappers for Arc, Entity Pool Glyph, etc.
	// SBML2SBGNML_GSOC2017 does not store any glyph and arcs while performing conversion.
	//public SWrapperMap sWrapperMap;
	
	// debugging
	int generalGlyphErrors = 0;
			
	/**
	 * Initialize the converter with a SBML2SBGNMLUtil, a SWrapperMap and a SBML2SBGNMLOutput
	 * 
	 * @param <code>SBMLDocument</code> sbmlDocument
	 */	
	public SBML2SBGNML_GSOC2017() {

	}
	
	/**
	 * Create an <code>Sbgn</code>, by converting objects in an SBML <code>Model</code> . 
	 * Each element in the SBML <code>Model</code> is mapped to an SBGN <code>Glyph</code> (glyph or arc).
	 * 
	 * @param <code>SBMLDocument</code> sbmlDocument
	 * @return <code>Sbgn</code> sbgnObject
	 */			
	public Sbgn convertToSBGNML(SBMLDocument sbmlDocument) throws SBMLException {
		logger = Logger.getLogger(SBML2SBGNML_GSOC2017.class);
		
		//SBML2SBGNMLUtil sUtil = new SBML2SBGNMLUtil();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		SWrapperMap sWrapperMap = new SWrapperMap(sOutput.map, sOutput.sbmlModel);
		
		if (sOutput.sbmlLayoutModel != null){
			// Note: the order of execution matters
			// convert CompartmentGlyphs to Encapsulation glyphs
			createFromCompartmentGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfCompartmentGlyphs);
			// convert SpeciesGlyphs to Entity Pool glyphs
			createFromSpeciesGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfSpeciesGlyphs);
			// for converting additional GraphicalObjects (including ReferenceGlyphs) to Auxiliary Units
			createFromGeneralGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfAdditionalGraphicalObjects);
			// for converting ReactionGlyphs (and SpeciesReferenceGlyphs) to Process glyphs (and Arcs)
			createFromReactionGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfReactionGlyphs);	
			
			// Add nested glyphs inside parent glyphs
			// Example: add units of information of an Entity Pool glyph
			addChildGlyphsToParent(sWrapperMap);
			
			// for converting SBML ReferenceGlyphs to SBGN Arcs:
			// add Port for each glyph that a converted Arc interacts with
			// Example: a ReferenceGlyphs (converted to SBGN Arc) goes from a Logic Operator to a Entity Pool,
			// then need to create a Port for the Logic Operator and a Port for the Entity Pool
			for (String key: sWrapperMap.listOfSWrapperArcs.keySet()) {
				SWrapperArc sWrapperArc = sWrapperMap.listOfSWrapperArcs.get(key);
				addPortForArc(sWrapperMap, sWrapperArc.speciesReferenceGlyph, sWrapperArc.arc);
			}
			
			// convert TextGlyphs to Labels of a SBGN glyph 
			createLabelsFromTextGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfTextGlyphs);
			
			// for each Species that has multiple SpeciesGlyph in the SBML Model, create an SBGN Glyph.Clone 
			addCloneMarkers(sOutput, sOutput.listOfSpeciesGlyphs);
		}
		
		// preserve each Model information such as UnitDefinition, FunctionDefinition, Rules, etc in an SBGN Extension element
		createExtensionsForModelObjects(sOutput);
		
		// preserve each Model's qual Plugin's information in an SBGN Extension element 
		if (sOutput.listOfQualitativeSpecies != null){
			createExtensionsForQualMathML(sOutput);
		}

		// print some statistics
		System.out.println("\n File: ");
		System.out.println("-----INPUT-----");
		System.out.println("SBML2SBGNMLOutput");
		System.out.println("　getNumCompartments　"+sOutput.sbmlModel.getNumCompartments());
		System.out.println("　getNumSpecies　"+sOutput.sbmlModel.getNumSpecies());
		System.out.println("　getNumReactions　"+sOutput.sbmlModel.getNumReactions());
		System.out.println("　getNumModifierSpeciesReferences　"+sOutput.sbmlModel.getNumModifierSpeciesReferences());
		System.out.println("　getNumSpeciesReferences　"+sOutput.sbmlModel.getNumSpeciesReferences());

		System.out.println("-----OUTPUT-----");
		System.out.println("sbgnObject.getMap().getGlyph() ="+sOutput.sbgnObject.getMap().getGlyph().size());
		System.out.println("sbgnObject.getMap().getArc() ="+sOutput.sbgnObject.getMap().getArc().size());
		System.out.println("sbgnObject.getMap().getArcgroup() ="+sOutput.sbgnObject.getMap().getArcgroup().size());
		System.out.println("-----DEBUG-----");
		System.out.println(Arrays.toString(sWrapperMap.listOfSWrapperGlyphEntityPools.keySet().toArray()));
		System.out.println(Arrays.toString(sWrapperMap.listOfSWrapperArcs.keySet().toArray()));
		System.out.println(" listOfSWrapperAuxiliary size="+sWrapperMap.listOfSWrapperAuxiliary.size());
		System.out.println(" generalGlyphErrors ="+generalGlyphErrors);

		// set the language of the SBGN-ML (PD or AF)
		//sOutput.sbgnObject.getMap().setLanguage(toLanguage);
		
		// return one sbgnObject
		return sOutput.sbgnObject;		
	}
			
	/**
	 * Create multiple SBGN <code>Glyph</code>, each corresponding to an SBML <code>CompartmentGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<CompartmentGlyph></code> listOfCompartmentGlyphs
	 */			
	public void createFromCompartmentGlyphs(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, ListOf<CompartmentGlyph> listOfCompartmentGlyphs) {
		Glyph sbgnCompartmentGlyph;
		
		if (listOfCompartmentGlyphs == null){return;}
		
		for (CompartmentGlyph compartmentGlyph : listOfCompartmentGlyphs){
			sbgnCompartmentGlyph = createFromOneCompartmentGlyph(sOutput, sWrapperMap, sbgnObject, compartmentGlyph);
			
			// add the created Glyph to the output
			sOutput.addGlyphToMap(sbgnCompartmentGlyph);
		}
		
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>s, each corresponding to an SBML <code>CompartmentGlyph</code>. 
	 * @param sbgnObject
	 * @param compartmentGlyph
	 * @return sbgnCompartmentGlyph
	 */
	public Glyph createFromOneCompartmentGlyph(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, CompartmentGlyph compartmentGlyph){
		Glyph sbgnCompartmentGlyph;
		
		// This step is optional, we already know the clazz will be "compartment"
		String clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(compartmentGlyph.getCompartmentInstance(), "compartment");
		if (!clazz.equals("compartment")){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(compartmentGlyph, "compartment");
		}
		
		// create a new Glyph, set its Bbox, but don't set a Label (we'll set a Label later)
		sbgnCompartmentGlyph = SBML2SBGNMLUtil.createGlyph(compartmentGlyph.getId(), clazz, 
				true, compartmentGlyph, 
				false, compartmentGlyph.getCompartment());	
		
		// if the compartmentGlyph has Annotation, we transfer the contents to the new glyph's Extension element
		try {
			SBML2SBGNMLUtil.addAnnotationInExtension(sbgnCompartmentGlyph, compartmentGlyph.getAnnotation());
			SBML2SBGNMLUtil.addAnnotationInExtension(sbgnCompartmentGlyph, compartmentGlyph.getCompartmentInstance().getAnnotation());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sbgnCompartmentGlyph;
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>s, each corresponding to an SBML <code>SpeciesGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<SpeciesGlyph></code> listOfSpeciesGlyphs
	 */		
	public void createFromSpeciesGlyphs(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, ListOf<SpeciesGlyph> listOfSpeciesGlyphs) {
		SWrapperGlyphEntityPool sbgnSpeciesGlyph;
		
		if (listOfSpeciesGlyphs == null){return;}
		
		for (SpeciesGlyph speciesGlyph : listOfSpeciesGlyphs){
			// create an Sbgn glyph using information in the speciesGlyph
			sbgnSpeciesGlyph = createFromOneSpeciesGlyph(sOutput, sbgnObject, speciesGlyph);
			
			// store the wrapper (containing the glyph we created as well as the speciesGlyph) so that we can access them later
			sWrapperMap.listOfSWrapperGlyphEntityPools.put(sbgnSpeciesGlyph.id, sbgnSpeciesGlyph);
			
			// since we add the child glyphs near the end of the conversion, we store what we need, and use them later
			// store the parent id so that we know who is the parent of the child glyph
			boolean hasParent = false;
			checkParentChildGlyph(sWrapperMap, sbgnSpeciesGlyph.species, speciesGlyph.getSpecies());
			
			// we only add the glyph to the output if it does not have a parent, we add child glyphs (such as units of information) later
			if (!hasParent){
				sOutput.addGlyphToMap(sbgnSpeciesGlyph.glyph);				
			}	
		}
	}
	
	/**
	 * Create an Sbgn glyph using information in the speciesGlyph. Store the "clone" and "orientation" attributes in the glyph.
	 * @param sbgnObject
	 * @param speciesGlyph
	 * @return sWrapperGlyphEntityPool
	 */
	public SWrapperGlyphEntityPool createFromOneSpeciesGlyph(SBML2SBGNMLOutput sOutput, Sbgn sbgnObject, SpeciesGlyph speciesGlyph){
		Glyph sbgnSpeciesGlyph;
		SWrapperGlyphEntityPool sWrapperGlyphEntityPool = null;
		
		// default clazz is "unspecified entity"
		String clazz = "unspecified entity";
		// if the speciesGlyph store render information, determine the clazz from this information
		// note: not all SBML models have render information
		try{
			RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesGlyph.getPlugin(RenderConstants.shortLabel);
			String objectRole = renderGraphicalObjectPlugin.getObjectRole();
			clazz = mapObjectRoleToClazz(objectRole);
		} catch (Exception e) {
			if (sOutput.sbmlModel.isSetPlugin("render")){
				System.out.println("createFromOneSpeciesGlyph " + "cannot get objectRole for "+speciesGlyph.getId());
			}
		}
		
		// if unable to determine a clazz using render information, then try to determine using the Species object of the speciesGlyph
		if (clazz.equals("unspecified entity")){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(speciesGlyph.getSpeciesInstance(), "unspecified entity");
		}
		// if unable to determine a clazz using the Species object of the speciesGlyph, then use the speciesGlyph
		if (clazz.equals("unspecified entity")){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(speciesGlyph, "unspecified entity");
		}
		
		// this step is for roundtrip conversion (in the non-roundtrip case, there is a different mechanism to determine 
		// the clone information, in a separate function)
		String orientation = null;
		if (clazz.contains("tag")){
			String[] array = clazz.split("_");
			clazz = array[0];
			orientation = array[1];
		}
		org.sbgn.bindings.Glyph.Clone clone = null;
		if (clazz.contains("clone")){
			String[] array = clazz.split("_");
			clazz = array[0];
			clone = new Glyph.Clone();
		}
		
		// create a new Glyph, set its Bbox, set a temporary Label (to be updated later)
		sbgnSpeciesGlyph = SBML2SBGNMLUtil.createGlyph(speciesGlyph.getId(), clazz, 
				true, speciesGlyph, 
				false, speciesGlyph.getSpecies());	
		
		if (orientation != null){
			sbgnSpeciesGlyph.setOrientation(orientation);
		}
		if (clone != null){
			sbgnSpeciesGlyph.setClone(clone);
		}

		// the speciesGlyph we created from could be representing a Species (of core package) or a QualitativeSpecies (of qual package)
		if (speciesGlyph.getSpeciesInstance() instanceof Species){
			sWrapperGlyphEntityPool = new SWrapperGlyphEntityPool(sbgnSpeciesGlyph, 
					(Species) speciesGlyph.getSpeciesInstance(), speciesGlyph);
		} else if (speciesGlyph.getSpeciesInstance() instanceof QualitativeSpecies){
			sWrapperGlyphEntityPool = new SWrapperGlyphEntityPool(sbgnSpeciesGlyph, 
					(QualitativeSpecies) speciesGlyph.getSpeciesInstance(), speciesGlyph);
		}
		
		// if the speciesGlyph has Annotation, we transfer the contents to the new glyph's Extension element
		// we want to preserve as much information as possible
		try {
			SBML2SBGNMLUtil.addAnnotationInExtension(sbgnSpeciesGlyph, speciesGlyph.getAnnotation());
			SBML2SBGNMLUtil.addAnnotationInExtension(sbgnSpeciesGlyph, speciesGlyph.getSpeciesInstance().getAnnotation());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// we also want to preserve the information store in the Species (referenced by the speciesGlyph)
		// store the attributes of the Species into the new glyph's Extension element
		// TODO: add other functions like this for ReactionGlyphs, SpeciesReferenceGlyphs, CompartmentGlyphs, etc.
		if (speciesGlyph.getSpeciesInstance() instanceof Species){
			SBML2SBGNMLUtil.addSpeciesInformationInExtension(sbgnSpeciesGlyph, (Species) speciesGlyph.getSpeciesInstance());
		}
		else if (speciesGlyph.getSpeciesInstance() instanceof QualitativeSpecies){
//			SBML2SBGNMLUtil.addSpeciesInformationInExtension(sbgnSpeciesGlyph, (QualitativeSpecies) speciesGlyph.getSpeciesInstance());
		}
		
		
		return sWrapperGlyphEntityPool;
	}	
	
	/**
	 * Create multiple SBGN <code>Glyph</code>s, each corresponding to an SBML <code>ReactionGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<ReactionGlyph></code> listOfReactionGlyphs
	 */			
	public void createFromReactionGlyphs(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, ListOf<ReactionGlyph> listOfReactionGlyphs) {
		SWrapperArcGroup sbgnReactionGlyph;
		SWrapperGlyphProcess sWrapperGlyphProcess;

		if (listOfReactionGlyphs == null){
			//System.out.println("createFromReactionGlyphs "+"listOfReactionGlyphs is null");
			return;
		}
		
		for (ReactionGlyph reactionGlyph : listOfReactionGlyphs){
			// create a Sbgn glyph from the information in the reactionGlyph
			sbgnReactionGlyph = createFromOneReactionGlyph(sOutput, sWrapperMap, sbgnObject, reactionGlyph);

			if (sbgnReactionGlyph == null){
				//System.out.println("! createFromReactionGlyphs id="+reactionGlyph.getId());
				continue;
			}
			
			// create a wrapper containing the Sbgn glyph we created as well as the reactionGlyph
			sWrapperGlyphProcess = new SWrapperGlyphProcess(sbgnReactionGlyph, reactionGlyph, 
										(Reaction) reactionGlyph.getReactionInstance(), null, 
										// by design, the first Glyph in the arcGroup is the Process Node
										sbgnReactionGlyph.arcGroup.getGlyph().get(0));
			
			sWrapperMap.listOfSWrapperGlyphProcesses.put(sbgnReactionGlyph.reactionId, sWrapperGlyphProcess);
			
		}		
	}
	
	/**
	 * Create SBGN <code>Glyph</code> and <code>Arc</code>s, corresponding to an SBML <code>ReactionGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ReactionGlyph</code> reactionGlyph
	 */		
	public SWrapperArcGroup createFromOneReactionGlyph(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, ReactionGlyph reactionGlyph) {
		Arcgroup processNode = null;
		Curve sbmlCurve;
		ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs;
		Reaction reaction;
		SWrapperArcGroup sWrapperArcGroup = null;
		
		if (!reactionGlyph.isSetReaction()) {
			//System.out.println("createFromOneReactionGlyph reactionGlyph " +reactionGlyph.getId()+ " does not have a reaction");
		}
		
		if (reactionGlyph.isSetCurve()) {
			// does not matter is reaction has a curve or not
		}
		
		// default clazz is "unspecified entity"
		String clazz = "unspecified entity";
		// if the reactionGlyph store render information, determine the clazz from this information
		// note: not all SBML model elements have render information
		try{
			RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) reactionGlyph.getPlugin(RenderConstants.shortLabel);
			String objectRole = renderGraphicalObjectPlugin.getObjectRole();
			clazz = mapObjectRoleToClazz(objectRole);
		} catch (Exception e) {
			if (sOutput.sbmlModel.isSetPlugin("render")){
				System.out.println("createFromOneSpeciesGlyph " + "cannot get objectRole for "+reactionGlyph.getId());
			}
		}	
				
		// if unable to determine a clazz using render information, then try to determine using the Reaction object of the reactionGlyph				
		if (clazz.equals("unspecified entity")){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(reactionGlyph.getReactionInstance(), "process");
		}
		// if unable to determine a clazz using the Reaction object of the reactionGlyph, then use the reactionGlyph
		if (clazz.equals("process")){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(reactionGlyph, "process");
		}
			
		// Create a Process glyph from dimensions of the Curve
		// note: we don't need to know the dimensions of the curve to create a Process glyph
		sbmlCurve = reactionGlyph.getCurve();
		processNode = SBML2SBGNMLUtil.createOneProcessNode(reactionGlyph.getReaction(), sbmlCurve, clazz);
			
		// failed to create a Process glyph
		if (processNode == null){	
			//System.out.println("!! createFromOneReactionGlyph "+reactionGlyph.getReaction());
			return null;
		}
			
		// for now, set the dimensioning of the Process glyph to just a Bbox (i.e. override the style of the 
		// Process glyph created in createOneProcessNode, the style has an alternative appearance that resembles more to the
		// layout of the SBML diagram)
		if (reactionGlyph.getBoundingBox() != null){
			SBML2SBGNMLUtil.createBBox(reactionGlyph, processNode.getGlyph().get(0));	
		}
		
		// for now, we don't need all the extra things created by createOneProcessNode,
		// just remove all the extra arcs created in createOneProcessNode
		if (processNode.getArc().size() > 0){
			processNode.getArc().set(0, null);
		}
			
		// add the Process glyph to the output 
		sOutput.addArcgroupToMap(processNode);
		// create a wrapper for the processNode (so that it can be accessed easily later)
		sWrapperArcGroup = new SWrapperArcGroup(reactionGlyph.getReaction(), processNode);
			
		// now, convert all the SpeciesReferenceGlyphs associated with the reactionGlyph to Sbgn Arcs
		listOfSpeciesReferenceGlyphs = reactionGlyph.getListOfSpeciesReferenceGlyphs();
		if (listOfSpeciesReferenceGlyphs.size() > 0) {
			createFromSpeciesReferenceGlyphs(sOutput, sWrapperMap, listOfSpeciesReferenceGlyphs, processNode.getGlyph().get(0));
		}	
		
		// store the additional information contained in the SBML reaction into the SBGN glyph's Extension
		// we want to preserve as much information as possible
		reaction = (Reaction) reactionGlyph.getReactionInstance();
		if (reaction.getKineticLaw() != null && reactionGlyph.isSetCurve()) {
			String math = reaction.getKineticLaw().getMathMLString();
			SBML2SBGNMLUtil.addExtensionElement(processNode, math);
		}

		// if the reactionGlyph has Annotation, we transfer the contents to the new glyph's Extension element
		// we want to preserve as much information as possible
		try {
			SBML2SBGNMLUtil.addAnnotationInExtension(processNode, reactionGlyph.getAnnotation());
			SBML2SBGNMLUtil.addAnnotationInExtension(processNode, reactionGlyph.getReactionInstance().getAnnotation());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sWrapperArcGroup;
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>s, each corresponding to an SBML <code>SpeciesReferenceGlyph</code>.
	 * @param listOfSpeciesReferenceGlyphs
	 * @param reactionGlyph
	 */
	public void createFromSpeciesReferenceGlyphs(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs, Glyph reactionGlyph) {
		Arc arc;
		SWrapperArc sWrapperArc;
		
		if (listOfSpeciesReferenceGlyphs == null){return;}
		
		for (SpeciesReferenceGlyph speciesReferenceGlyph : listOfSpeciesReferenceGlyphs){
			SBML2SBGNMLUtil.printHelper("createGlyphFromReactionGlyph", 
					String.format("speciesGlyph = %s, speciesReference = %s \n", 
					speciesReferenceGlyph.getSpeciesGlyph(), speciesReferenceGlyph.getSpeciesReference()));
			
			sWrapperArc = createFromOneSpeciesReferenceGlyph(sOutput, sWrapperMap, speciesReferenceGlyph, reactionGlyph);
			// store the created Arc into SBGN
			sOutput.addArcToMap(sWrapperArc.arc);	
			
			sWrapperMap.listOfSWrapperArcs.put(sWrapperArc.id, sWrapperArc);
		}		
	}
	
	/**
	 * Create SBGN an <code>Arc</code> corresponding to the SBML <code>SpeciesReferenceGlyph</code>.
	 * @param speciesReferenceGlyph
	 * @param reactionGlyph an SBGN glyph
	 * @return
	 */
	public SWrapperArc createFromOneSpeciesReferenceGlyph(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, SpeciesReferenceGlyph speciesReferenceGlyph, Glyph reactionGlyph){
		Arc arc;
		Curve sbmlCurve;
		SWrapperArc sWrapperArc = null;
		
		// the Curve contains multiple CurveSegments
		sbmlCurve = speciesReferenceGlyph.getCurve();
		
		// create an Arc using information stored in the SpeciesReferenceGlyph
		// CubicBezier in SBML layout supports curved lines, these are preserved
		// the source/target glyphs of arcs is set later
		// the Port that the new Arc points to will be created later
		arc = SBML2SBGNMLUtil.createOneArc(sbmlCurve);
		arc.setId(speciesReferenceGlyph.getSpeciesReference());
		
		// Set clazz of the Arc:
		// default clazz is "unspecified entity" (but this is incorrect)
		String clazz = "unspecified entity";
		// if the reactionGlyph store render information, determine the clazz from this information
		// note: not all SBML model elements have render information
		try{
			RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesReferenceGlyph.getPlugin(RenderConstants.shortLabel);
			String objectRole = renderGraphicalObjectPlugin.getObjectRole();
			clazz = mapObjectRoleToClazz(objectRole);
		} catch (Exception e) {
			if (sOutput.sbmlModel.isSetPlugin("render")){
				System.out.println("createFromOneSpeciesGlyph " + "cannot get objectRole for "+speciesReferenceGlyph.getId());
			}
		}
	
		// if the speciesReferenceGlyph contains attribute speciesReferenceRole, then determine the clazz using that
		if (clazz.equals("unspecified entity")){
			if (speciesReferenceGlyph.getSpeciesReferenceRole() != null){
				clazz = SBML2SBGNMLUtil.searchForReactionRole(speciesReferenceGlyph.getSpeciesReferenceRole());
			}
		}
		
		// if still unable to determine a clazz, then try to determine using the speciesReferenceGlyph
		if (clazz == null){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(speciesReferenceGlyph, "modulation");
		}
		// if unable to determine a clazz using the speciesReferenceGlyph, try SpeciesReference object of speciesReferenceGlyph
		if (clazz.equals("modulation")){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(speciesReferenceGlyph.getReferenceInstance(), "modulation");
		}
		
		// finally, set the clazz of the Sbgn glyph
		arc.setClazz(clazz);
				
		// create a wrapper for the SpeciesReference and the converted Arc, store it, so that we can access the meta information later
		SimpleSpeciesReference simpleSpeciesReference = (SimpleSpeciesReference) speciesReferenceGlyph.getSpeciesReferenceInstance();
		if (simpleSpeciesReference instanceof SpeciesReference){
			sWrapperArc = new SWrapperArc(arc, speciesReferenceGlyph,
					(SpeciesReference) simpleSpeciesReference);			
		} else if (simpleSpeciesReference instanceof ModifierSpeciesReference) {
			sWrapperArc = new SWrapperArc(arc, speciesReferenceGlyph,
					(ModifierSpeciesReference) simpleSpeciesReference);			
		}

		// now, set the "source" and "target" attributes of the new Arc
		// there are 2 types of arcs that we classified;
		// 1. reactionToSpecies (such as a production arc)
		// 2. speciesToReaction (such as a consumption arc or a modifier arc)
		// we handle these 2 types differently
		String sourceTargetType;
		String reactionId = speciesReferenceGlyph.getSpeciesReferenceInstance().getParentSBMLObject().getParentSBMLObject().getId();
		String speciesId = null;
		Glyph glyph = null;
		try {
			// the speciesReferenceGlyph contains an attribute for the Species that the speciesReferenceGlyph is associated with
			speciesId = speciesReferenceGlyph.getSpeciesGlyphInstance().getSpecies();
			// then, we can find the glyph that we converted for the Species
			glyph = sWrapperMap.listOfSWrapperGlyphEntityPools.get(speciesId).glyph;
		} catch (Exception e){}
		
		arc.setSource(null);
		arc.setTarget(null);
		
		if (clazz.equals("production")){
			sourceTargetType="reactionToSpecies";

			// create a new Port, which will be the source of the arc
			Port port = new Port();
			port.setId("Port" + "_" + reactionId + "_" + speciesId);
			port.setX(arc.getStart().getX());
			port.setY(arc.getStart().getY());
			// the glyph that contains this port is a reactionGlyph
			reactionGlyph.getPort().add(port);
			
			// now we set the source to be the Port of the reactionGlyph
			arc.setSource(port);
			// set the target to be the species
			arc.setTarget(glyph);
			
		} else{
			sourceTargetType="speciesToReaction";

			// create a new Port, which will be the target of the arc
			Port port = new Port();
			port.setId("Port" + "_" + speciesId + "_" + reactionId);
			port.setX(arc.getEnd().getX());
			port.setY(arc.getEnd().getY());
			// the glyph that contains this port is a reactionGlyph
			reactionGlyph.getPort().add(port);
			
			// now we set the target to be the Port of the reactionGlyph
			arc.setTarget(port);
			// set the source to be the species
			arc.setSource(glyph);
		}
		
		// store the meta information we retrieved above into the wrapper
		sWrapperArc.setSourceTarget(reactionId, speciesId, sourceTargetType);
				
		// if the speciesReferenceGlyph has Annotation, we transfer the contents to the new arc's Extension element
		// we want to preserve as much information as possible
		try {
			SBML2SBGNMLUtil.addAnnotationInExtension(arc, speciesReferenceGlyph.getAnnotation());
			SBML2SBGNMLUtil.addAnnotationInExtension(arc, speciesReferenceGlyph.getReferenceInstance().getAnnotation());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (arc.getSource() == null || arc.getTarget() == null){
			System.out.println("createFromOneSpeciesReferenceGlyph" + arc.getId());
		} else {
			//System.out.println("createFromOneSpeciesReferenceGlyph" + arc.getId() + "sourceId "+ " targetId ");
		}

		return sWrapperArc;
	}
	
	/**
	 * Create multiple SBGN <code>Label</code>s, each corresponding to an SBML <code>TextGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<TextGlyph></code> listOfTextGlyphs
	 */			
	public void createLabelsFromTextGlyphs(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, ListOf<TextGlyph> listOfTextGlyphs) {
		
		if (listOfTextGlyphs == null){return;}
		
		for (TextGlyph textGlyph : listOfTextGlyphs){	
			createLabelFromOneTextGlyph(sOutput, sWrapperMap, textGlyph);
		}
	}
	
	/**
	 * Find the GraphicalObject that the textGlyph is associated with, and find the Sbgn glyph that the 
	 * GraphicalObject is converted into, then set the glyph.Label using the text in textGlyph. 
	 * @param textGlyph
	 */
	public void createLabelFromOneTextGlyph(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, TextGlyph textGlyph) {
		Glyph sbgnGlyph;
		String id = null;
		String text;
		List<Glyph> listOfGlyphs;
		int indexOfSpeciesGlyph;		
		
		// get the text stored in the textGlyph
		if (textGlyph.isSetText()) {
			text = textGlyph.getText();
		} else if (textGlyph.isSetOriginOfText()) {
			// get the text stored in the origin Species/Reaction, etc
			text = textGlyph.getOriginOfTextInstance().getName();
		} else {
			text = "";
		}
		
		if (textGlyph.isSetGraphicalObject()) {
			id = textGlyph.getGraphicalObjectInstance().getId();
			listOfGlyphs = sOutput.sbgnObject.getMap().getGlyph();
			
			// find the Glyph that should contain this text
			sbgnGlyph = SBML2SBGNMLUtil.searchForGlyph(listOfGlyphs, id);
			if (sbgnGlyph != null){
				if ((!text.equals("")) || (sbgnGlyph.getLabel() == null)){
					SBML2SBGNMLUtil.setLabel(sbgnGlyph, text, textGlyph.getBoundingBox());
				}
			} else {
				// if we can't find the glyph, it means the glyph is a Auxiliary, we need to search in all the child glyphs of the Arcs
				for (Arc a:  sOutput.sbgnObject.getMap().getArc()) {
					listOfGlyphs = a.getGlyph();
					sbgnGlyph = SBML2SBGNMLUtil.searchForGlyph(listOfGlyphs, id);
					if (sbgnGlyph != null){
						SBML2SBGNMLUtil.setLabel(sbgnGlyph, text, textGlyph.getBoundingBox());
					}
				}
			}	
			
		} else {
			// we should never reach here if the Model is well-formed
			// clazz is unknown
			sbgnGlyph = SBML2SBGNMLUtil.createGlyph(textGlyph.getId(), "unspecified entity", false, null, true, text);
			SBML2SBGNMLUtil.createVoidBBox(sbgnGlyph);
			// add this new glyph to Map
			sOutput.addGlyphToMap(sbgnGlyph);		
		}		
	}

	/**
	 * Create multiple SBGN <code>Glyph</code>s, each corresponding to a SBML <code>GeneralGlyph</code>. 
	 * Each <code>GraphicalObject</code> is casted to be a <code>GeneralGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<GraphicalObject></code> listOfAdditionalGraphicalObjects
	 */		
	public void createFromGeneralGlyphs(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, ListOf<GraphicalObject> listOfAdditionalGraphicalObjects) {
		GeneralGlyph generalGlyph;
		BoundingBox bbox;
		Arcgroup arcgroup;
		
		if (listOfAdditionalGraphicalObjects == null){return;}
		
		// we treat each GeneralGlyph like a ReactionGlyph, each containing a center glyph and multiple arcs
		for (GraphicalObject graphicalObject : listOfAdditionalGraphicalObjects) {
			bbox = graphicalObject.getBoundingBox();
			generalGlyph = (GeneralGlyph) graphicalObject;

			// we create an ArcGroup for each GeneralGlyph
			arcgroup = createFromOneGeneralGlyph(sOutput, sWrapperMap, sbgnObject, generalGlyph);
			if (arcgroup == null){continue;}
			
			// we get the meta information about the graphicalObject, whether it has a parent glyph or not
			boolean hasParent = false;
			checkParentChildGlyph(sWrapperMap, graphicalObject, graphicalObject.getId());
			
			// we only add the glyph we created to output if it does not have a parent, otherwise, we add 
			// it later, once all the parent glyphs are converted and stored
			if (!hasParent){
				sOutput.addArcgroupToMap(arcgroup);			
			}	
						
		}

	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>s from an SBML <code>GeneralGlyph</code>. 
	 */		
	public Arcgroup createFromOneGeneralGlyph(SBML2SBGNMLOutput sOutput, SWrapperMap sWrapperMap, Sbgn sbgnObject, GeneralGlyph generalGlyph){
		ListOf<ReferenceGlyph> listOfReferenceGlyphs;
		ListOf<GraphicalObject> listOfSubGlyphs;	
		List<Glyph> listOfGlyphs = new ArrayList<Glyph>();
		List<Arc> listOfArcs = new ArrayList<Arc>();
		Glyph glyph;
		Arc arc;
		Arcgroup arcgroup;
		Curve sbmlCurve;
		
		// default clazz is "unspecified entity"
		String clazz = "unspecified entity";
		// if the generalGlyph store render information, determine the clazz from this information
		// note: not all SBML models have render information
		try{
			RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) generalGlyph.getPlugin(RenderConstants.shortLabel);
			String objectRole = renderGraphicalObjectPlugin.getObjectRole();	
		} catch (Exception e) {
			System.out.println("createFromOneGeneralGlyph " + "cannot get object role for "+generalGlyph.getId());
		}
		
		// if unable to determine a clazz using render information, then try to determine using the referenced object of the generalGlyph
		if (generalGlyph.getReferenceInstance() != null){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(generalGlyph.getReferenceInstance(), "unspecified entity");				
		}
		
		// if unable to determine a clazz using the referenced object of the generalGlyph, then use the generalGlyph
		if (clazz.equals("unspecified entity")){
			try{
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(generalGlyph, "unspecified entity");
			} catch(Exception e){}
		}
		

		if (generalGlyph.isSetCurve()){
			sbmlCurve = generalGlyph.getCurve();				// doesn't matter if the generalGlyph has a curve or not

			//processNode = SBML2SBGNMLUtil.createOneProcessNode(generalGlyph.getId(), sbmlCurve, clazz);
			//sOutput.addArcgroupToMap(processNode);	
			
			// reset the style of the glyph using just a simple Bbox
			//SBML2SBGNMLUtil.createBBox(generalGlyph, processNode.getGlyph().get(0));
		}
		
		// we want to create a glyph if there is a BoundingBox (this is a trick used in this converter for roundtrip conversion, 
		// to avoid the extra GeneralGlyphs we created to resemble the center Curve of a ReactionGlyph)
		if (generalGlyph.isSetBoundingBox()){
			// create a glyph using information in the generalGlyph
			glyph = createFromOneGraphicalObject(sOutput, sbgnObject, generalGlyph);
			// this step is required
			listOfGlyphs.add(glyph);

			// since we add the child glyphs near the end of the conversion, we store what we need, and use them later
			// store the parent id so that we know who is the parent of the child glyph
			boolean hasParent = checkParentChildGlyph(sWrapperMap, generalGlyph, generalGlyph.getId());
			if (hasParent){
				// TODO: need a better splitting pattern
				String id = splitString(generalGlyph.getId(), 1);
				
				// we temporarily store this glyph in listOfSWrapperAuxiliary, and not add it to output,
				// we will add the glyph later
				sWrapperMap.listOfSWrapperAuxiliary.put(generalGlyph.getId(), new SWrapperAuxiliary(glyph, generalGlyph, id));
				return null;
			}		
		}
		
		// if we encountered a GeneralGlyph that stores listOfReferenceGlyphs
		if (generalGlyph.isSetListOfReferenceGlyphs()){
			listOfReferenceGlyphs = generalGlyph.getListOfReferenceGlyphs();
			
			for (ReferenceGlyph referenceGlyph : listOfReferenceGlyphs) {
				// create an Arc using information in the referenceGlyph
				arc = createFromOneReferenceGlyph(sWrapperMap, referenceGlyph);
				// this step is required
				listOfArcs.add(arc);
			}
		}
		
		// if we encountered a GeneralGlyph that stores listOfSubGlyphs
		if (generalGlyph.isSetListOfSubGlyphs()){
			listOfSubGlyphs = generalGlyph.getListOfSubGlyphs();
			
			for (GraphicalObject graphicalObject : listOfSubGlyphs) {
				// create a glyph using information in the graphicalObject
				glyph = createFromOneGraphicalObject(sOutput, sbgnObject, graphicalObject);
				// this step is required
				listOfGlyphs.add(glyph);
			}
		}	
		
		// we store all the glyphs and arcs we created earlier into an ArcGroup
		arcgroup = SBML2SBGNMLUtil.createOneArcgroup(listOfGlyphs, listOfArcs, generalGlyph.getId());

		// if the generalGlyph has Annotation, we transfer the contents to the new arcgroup's Extension element
		// we want to preserve as much information as possible
		try {
			SBML2SBGNMLUtil.addAnnotationInExtension(arcgroup, generalGlyph.getAnnotation());
			if (generalGlyph.getReferenceInstance() != null){
				SBML2SBGNMLUtil.addAnnotationInExtension(arcgroup, generalGlyph.getReferenceInstance().getAnnotation());
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return arcgroup;
	}
	
	/**
	 * Create SBGN an <code>Arc</code> corresponding to the SBML <code>ReferenceGlyph</code>.
	 * @param referenceGlyph
	 * @return
	 */
	public Arc createFromOneReferenceGlyph(SWrapperMap sWrapperMap, ReferenceGlyph referenceGlyph){
		Arc arc;
		Curve sbmlCurve;
		String referenceGlyphId;
		String glyph;
		String reference;
		String role;
		
		referenceGlyphId = referenceGlyph.getId();
		glyph = referenceGlyph.getGlyph();
		reference = referenceGlyph.getReference();
		role = referenceGlyph.getRole();
		
//		SBML2SBGNMLUtil.printHelper("createGlyphsFromGeneralGlyphs", 
//				String.format("id=%s, glyph=%s, reference=%s, role=%s \n", 
//				referenceGlyphId, glyph, reference, role));
		
		sbmlCurve = referenceGlyph.getCurve();
		arc = SBML2SBGNMLUtil.createOneArc(sbmlCurve);
		
		// Set clazz of the Arc:
		// please see the detailed comments in createFromOneSpeciesReferenceGlyph
		String clazz = "unspecified entity";
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) referenceGlyph.getPlugin(RenderConstants.shortLabel);
		try{
			String objectRole = renderGraphicalObjectPlugin.getObjectRole();
			clazz = mapObjectRoleToClazz(objectRole);		
		} catch (Exception e) {
			System.out.println("createFromOneReferenceGlyph " + "cannot get object role for "+referenceGlyph.getId());
		}
		if (clazz.equals("unspecified entity")){
			if (role != null){
				clazz = SBML2SBGNMLUtil.searchForReactionRole(role);
			}
		}
		if (clazz == null){
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(referenceGlyph, "unknown influence");
		} 
		// a ReferenceGlyph does not have a referenced Object
//		if (clazz.equals("unknown influence")){
//			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(referenceGlyph.getReferenceInstance(), "unknown influence");
//		}
		arc.setClazz(clazz);

		// if the referenceGlyph has Annotation, we transfer the contents to the new arc's Extension element
		// we want to preserve as much information as possible			
		try {
			SBML2SBGNMLUtil.addAnnotationInExtension(arc, referenceGlyph.getAnnotation());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// this step is critical, because we didn't create a wrapper for the arc we created here, therefore we didn't add 
		// it to the so the listOfSWrapperArcs. The call to addPortForArc in convertToSBGNML skips all referenceGlyphs.
		addPortForArc(sWrapperMap, referenceGlyph, arc);
			
		return arc;
	}
	
	/**
	 * 
	 * @param sbgnObject
	 * @param graphicalObject
	 * @return
	 */
	public Glyph createFromOneGraphicalObject(SBML2SBGNMLOutput sOutput, Sbgn sbgnObject, GraphicalObject graphicalObject){
		Glyph sbgnGlyph;
		
		// Set clazz of the Arc:
		// please see the detailed comments in createFromOneSpeciesGlyph
		SBase sbase = sOutput.sbmlModel.getSBaseById(graphicalObject.getMetaidRef());
		if (sbase == null){
			sbase = graphicalObject;
		}
		String clazz = "unspecified entity";
		try{
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(sbase, "unspecified entity");
		} catch(Exception e){}
		if (clazz.equals("unspecified entity")){
			try{
			clazz = SBML2SBGNMLUtil.sbu.getOutputFromClass(graphicalObject, "unspecified entity");
			} catch(Exception e){}
		}
		if (clazz.equals("unspecified entity")){
			RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) graphicalObject.getPlugin(RenderConstants.shortLabel);
			try {
				String objectRole = renderGraphicalObjectPlugin.getObjectRole();
				clazz = mapObjectRoleToClazz(objectRole);
	
			} catch (PropertyUndefinedError e){}
		}
		
		
		// create a new Glyph, set its Bbox, don't set a Label
		sbgnGlyph = SBML2SBGNMLUtil.createGlyph(graphicalObject.getId(), clazz, 
				true, graphicalObject, 
				false, null);	
		
		// if the graphicalObject has Annotation, we transfer the contents to the new sbgnGlyph's Extension element
		// we want to preserve as much information as possible		
		try {
			SBML2SBGNMLUtil.addAnnotationInExtension(sbgnGlyph, graphicalObject.getAnnotation());
			SBML2SBGNMLUtil.addAnnotationInExtension(sbgnGlyph, sbase.getAnnotation());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sbgnGlyph;
	}		
	
	/**
	 * Return the Sbgn clazz given the value of SBML render:objectRole
	 * @param objectRole
	 * @return
	 */
	private String mapObjectRoleToClazz(String objectRole) {
		String clazz = null;
		
		// TODO: horizontal or vertical orientation
		if (objectRole.equals("or")){
			clazz = "or";
		} else if (objectRole.equals("and")){
			clazz = "and";
		} else if (objectRole.equals("not")){
			clazz = "not";
		} 
		
		else if (objectRole.equals("SBO0000167")){
			clazz = "process";
		} else if (objectRole.equals("SBO0000177")){
			clazz = "association";
		} else if (objectRole.equals("SBO0000180")){
			clazz = "dissociation";
		} else if (objectRole.equals("SBO0000397")){
			clazz = "omitted process";
		} else if (objectRole.equals("SBO0000396")){
			clazz = "uncertain process";
		}
		
		else if (objectRole.equals("SBO0000245")){
			clazz = "macromolecule";
		} else if (objectRole.equals("SBO0000247")){
			clazz = "simple chemical";
		} else if (objectRole.equals("SBO0000291")){
			clazz = "source and sink";
		} else if (objectRole.equals("SBO0000354")){
			clazz = "nucleic acid feature";
		} else if (objectRole.equals("SBO0000253")){
			clazz = "complex";
		} else if (objectRole.equals("SBO0000405")){
			clazz = "perturbing agent";
		 }else if (objectRole.equals("SBO0000285")){
			clazz = "unspecified entity";
		}
		
		else if (objectRole.equals("SBO0000358")){
			clazz = "phenotype";
		} else if (objectRole.equals("SBO0000412")){
			clazz = "biological activity";
		} 
		
		else if (objectRole.equals("SBO0000247clone")){
			clazz = "simple chemical_clone";
		} else if (objectRole.equals("SBO0000245clone")){
			clazz = "macromolecule_clone";
		} else if (objectRole.equals("SBO0000354clone")){
			clazz = "nucleic acid feature_clone";
		} 
		
		else if (objectRole.equals("SBO0000245multimerclone")){
			clazz = "macromolecule multimer_clone";
		} else if (objectRole.equals("SBO0000247multimerclone")){
			clazz = "simple chemical multimer_clone";
		} else if (objectRole.equals("SBO0000354multimerclone")){
			clazz = "nucleic acid feature multimer_clone";
		} 
		
		else if (objectRole.equals("SBO0000247multimer")){
			clazz = "simple chemical multimer";
		} else if (objectRole.equals("SBO0000245multimer")){
			clazz = "macromolecule multimer";
		} else if (objectRole.equals("SBO0000354multimer")){
			clazz = "nucleic acid feature multimer";
		} else if (objectRole.equals("SBO0000253multimer")){
			clazz = "complex multimer";
		}
		
		else if (objectRole.equals("unitofinfo")){
			clazz = "unit of information";
		} else if (objectRole.equals("unitofinfo")){
			clazz = "cardinality";
		} else if (objectRole.equals("statevar")){
			clazz = "state variable";
		} 
		
		else if (objectRole.equals("SBO0000289")){
			clazz = "compartment";
		} else if (objectRole.equals("SBO0000395")){
			clazz = "submap";
		} 
		
		else if (objectRole.equals("Tagleft")){
			clazz = "tag_left";
		} else if (objectRole.equals("Tagright")){
			clazz = "tag_right";
		} else if (objectRole.equals("Tagup")){
			clazz = "tag_up";
		} else if (objectRole.equals("Tagdown")){
			clazz = "tag_down";
		}
		
		else if (objectRole.equals("Tagleft")){
			clazz = "terminal_left";
		} else if (objectRole.equals("Tagright")){
			clazz = "terminal_right";
		} else if (objectRole.equals("Tagup")){
			clazz = "terminal_up";
		} else if (objectRole.equals("Tagdown")){
			clazz = "terminal_down";
		}		
		
		
		if (objectRole.equals("SBO0000172")){
			clazz = "catalysis";
		} else if (objectRole.equals("product")){
			clazz = "production";
		} else if (objectRole.equals("substrate")){	
			clazz = "consumption";
		} else if (objectRole.equals("SBO0000170")){
			clazz = "stimulation";
		} else if (objectRole.equals("SBO0000171")){
			clazz = "necessary stimulation";
		} else if (objectRole.equals("SBO0000168")){
			clazz = "unknown influence";
		} else if (objectRole.equals("SBO0000169")){
			clazz = "inhibition";
		} else if (objectRole.equals("SBO0000168")){
			clazz = "modulation";
		} // TODO: what about activity flow modulation arcs?
		
		else if (objectRole.equals("equivalence")){
			clazz = "equivalence arc";
		} else if (objectRole.equals("SBO0000398")){
			clazz = "logic arc";
		}
		
		if (clazz == null){
			return "unspecified entity";
		} else {
			return clazz;
		}
		
	}
	
	
	/**
	 * Retrive the id of the parent of the sbmlGlyph and store it in a hash map. 
	 * This function is used in a roundtrip conversion.
	 * @param sbmlGlyph
	 * @param childId
	 * @return hasParent
	 */
	public boolean  checkParentChildGlyph(SWrapperMap sWrapperMap, SBase sbmlGlyph, String childId){
		boolean hasParent = false;
		if (sbmlGlyph == null){
			// we should never get an error like this
			return hasParent;
		}
		
		List<CVTerm> cvTerms = sbmlGlyph.getAnnotation().getListOfCVTerms();
		
		for (CVTerm cvt : cvTerms){
			// if the CVTerm is a BQB_IS_PART_OF qualifier
			if (cvt.getBiologicalQualifierType().getElementNameEquivalent().equals(Qualifier.BQB_IS_PART_OF.getElementNameEquivalent())){
				// we assume the first item in the list of Resources is what we want
				String parent = cvt.getResources().get(0);
				hasParent = true;
				
				// In this converter, we assume the String parent is of the form "SpeciesGlyph_parentId", so the parentId 
				// can be found by splitting the string and taking the substring
				// TODO: need a better splitting pattern
				String parentId = splitString(parent, 1);
				sWrapperMap.notAdded.put(childId, parentId);

			}
		}
		return hasParent;
	}
	
	/**
	 * Find all the SpeciesGlyphs that should contain a Clone marker and add meta information in the corresponding Sbgn glyphs. So
	 * that we can add the Clone markers in another function.
	 * Note: listOfSWrapperGlyphEntityPools does not work if there are clones! 
	 * 	e.g. 244 glyphs for 72 species, only 72 of the 244 are stored
	 * 	this means we can't convert back and forth between sbgn and sbml (sbgn->sbml->sbgn->sbml->etc)
	 * 	TODO: change the architecture of listOfSWrapperGlyphEntityPools
	 * 	 
	 * @param listOfSpeciesGlyphs
	 */
	private void addCloneMarkers(SBML2SBGNMLOutput sOutput, ListOf<SpeciesGlyph> listOfSpeciesGlyphs) {

		if (listOfSpeciesGlyphs == null){return;}
		
		HashMap<String, List<String>> speciesMap = new HashMap<String, List<String>>();
		
		// Find all the SpeciesGlyphs that should contain a Clone marker, store their id in speciesMap
		// note: does not consider Species that do not have a SpeciesGlyph in layout
		for (SpeciesGlyph speciesGlyph : listOfSpeciesGlyphs){
			String speciesId = speciesGlyph.getSpecies();
			String speciesGlyphId = speciesGlyph.getId();
			
			if (speciesMap.get(speciesId) == null){
				speciesMap.put(speciesId, new ArrayList<String>());
				speciesMap.get(speciesId).add(speciesGlyphId);
			} else {
				speciesMap.get(speciesId).add(speciesGlyphId);
			}

		}

		// now find the corresponding glyphs, and add meta information in the glyph's Extension
		List<Glyph> listOfGlyphs = sOutput.sbgnObject.getMap().getGlyph();
		for (Glyph glyph: listOfGlyphs){
			if (glyph.getExtension() == null){
				glyph.setExtension(new Extension());
			}
			List<Element> elements = glyph.getExtension().getAny();
			for (Element e : elements){
				// this is the name (the title) of the Element
				String tagName = e.getTagName();
				
				if (tagName.equals(SBML2SBGNMLUtil.SBFCANNO_PREFIX + ":species")){
					String speciesId = e.getAttribute(SBML2SBGNMLUtil.SBFCANNO_PREFIX + ":id");
					
					// if there are more than one SpeciesGlyphs for the Species in the Model
					if (speciesMap.get(speciesId).size() > 1){

						// add the Clone marker
						SBML2SBGNMLUtil.setClone(glyph);
						//System.out.println("addCloneMarkers species="+ speciesId + " glyph="+glyph.getId());
					}
				}
			}
		}
	}

	/**
	 * Add all the stored child (in notAdded) glyphs to their associated parents, thus adding the child glyph to the output Sbgn Map
	 * Note that this function is used in a roundtrip conversion.
	 */
	private void addChildGlyphsToParent(SWrapperMap sWrapperMap) {
		
		for (String key : sWrapperMap.notAdded.keySet()){
			Glyph childGlyph;
			
			String parentKey = sWrapperMap.notAdded.get(key);
			Glyph parentGlyph = sWrapperMap.getGlyph(parentKey);
			childGlyph = sWrapperMap.getGlyph(key);
			//System.out.println(" listOfSWrapperAuxiliary size="+sWrapperMap.listOfSWrapperAuxiliary.size());
			
			Arc parentArc = null;
			
			// if we can't find the parent glyph, then the parent must be an Arc
			if (parentGlyph == null){
				parentArc = sWrapperMap.getArc(parentKey);
				
				if (parentArc == null){
					System.out.println("! addedChildGlyphs "+parentKey+" contains "+key );
					generalGlyphErrors++;
					continue;
				}
				
				// add the child glyph to the parent arc
				parentArc.getGlyph().add(childGlyph);
				
				// the glyph in arcs should have clazz "cardinality" 
				if (childGlyph.getClazz().equals("unit of information")){
					childGlyph.setClazz("cardinality");
				}
								
			} else {
				// add the child glyph to the parent glyph
				parentGlyph.getGlyph().add(childGlyph);
			}
				
			// for debugging
			try{
				System.out.println("addedChildGlyphs "+childGlyph.getId() +" in "+parentGlyph.getId());
			}catch(Exception e){
				if (parentArc == null){
					System.out.println("!! addedChildGlyphs "+parentKey+" contains "+key);
				}
			}
		}
	}

	/**
	 * For converting SBML ReferenceGlyphs to SBGN Arcs:
	 * 	add Port for each glyph that a converted Arc interacts with.
	 * 	Example: a ReferenceGlyphs (converted to SBGN Arc) goes from a Logic Operator to a Entity Pool,
	 * 	then create a Port for the Logic Operator and a Port for the Entity Pool
	 * Add port for AND OR NOT glyphs only, note that we already added ports to Reactions earlier in createFromOneSpeciesReferenceGlyph
	 * @param referenceGlyph
	 * @param arc
	 */
	public void addPortForArc(SWrapperMap sWrapperMap, GraphicalObject referenceGlyph, Arc arc){
		String sourceId = null;
		String targetId = null;
		List<CVTerm> cvTerms = referenceGlyph.getAnnotation().getListOfCVTerms();
		boolean hasPort = false;
		
		if (arc.getSource() == null || arc.getTarget() == null){
			System.out.println("createFromOneReferenceGlyph no source or target: " + arc.getId());
		} else {
			// it's okay, this happens for ReactionsGlyphs. They are not stored in listOfSWrapperGlyphEntityPools, but 
			// we already added the Port to the Reaction earlier
			return;
		}
		
		arc.setTarget(null);
		arc.setSource(null);
		
		// We find the meta information stored in the CVTerm.
		// Note that this step is useful for roundtrip conversions because we already stored the meta information when converting SBGN->SBML
		for (CVTerm cvt : cvTerms){
			if (cvt.getBiologicalQualifierType().getElementNameEquivalent().equals(Qualifier.BQB_HAS_PROPERTY.getElementNameEquivalent())){
				sourceId = cvt.getResources().get(0);
				targetId = cvt.getResources().get(1);
				hasPort = true;
			}
		}
		
		// Now we find the source and target glyphs, and add a Port to each
		if (hasPort){
			// the source glyph:
			SWrapperGlyphEntityPool sWrapperGlyph = sWrapperMap.listOfSWrapperGlyphEntityPools.get(sourceId);
			if (sWrapperGlyph == null){
				// it's okay, this happens for ReactionsGlyphs. They are not stored in listOfSWrapperGlyphEntityPools, but 
				// we already added the Port to the Reaction earlier
				return;
			}
			
			// we only want to add a Port to the Logic Operators, it would be incorrect to add Port to anything else
			Boolean addPort = false;
			if (sWrapperGlyph.clazz.equals("and")) {
				addPort =  true;
			} else if (sWrapperGlyph.clazz.equals("or")) {
				addPort =  true;
			} else if (sWrapperGlyph.clazz.equals("not")) {
				addPort =  true;
			}
			
			if (addPort){
				Bbox bbox = sWrapperGlyph.glyph.getBbox();
				Port port = new Port();
				port.setId("Port" + "_" + sourceId + "_" + targetId);
				port.setX(arc.getStart().getX());
				port.setY(arc.getStart().getY());
				
				// add the port to the source glyph
				sWrapperGlyph.glyph.getPort().add(port);
				
				// set the arc's source to the port
				arc.setSource(port);
				
				// this step is optional. the target glyph could be an Entity Pool or a Process
				SWrapperGlyphEntityPool targetGlyph = sWrapperMap.listOfSWrapperGlyphEntityPools.get(targetId);
				if (targetGlyph != null){
					// set the arc's target to the glyph
					arc.setTarget(targetGlyph.glyph);
				} else {
					SWrapperGlyphProcess targetProcess = sWrapperMap.listOfSWrapperGlyphProcesses.get(targetId);
					if (targetProcess != null){
						// set the arc's target to the glyph
						arc.setTarget(targetProcess.processNodeGlyph);
					}
				}
				
				
			}
			
			// do the same for the target glyph:
			sWrapperGlyph = sWrapperMap.listOfSWrapperGlyphEntityPools.get(targetId);
			Glyph targetGlyph;
			SWrapperGlyphProcess sWrapperReaction;
			if (sWrapperGlyph == null){
				sWrapperReaction = sWrapperMap.listOfSWrapperGlyphProcesses.get(targetId);
				targetGlyph = sWrapperReaction.processNodeGlyph;
			} else {
				targetGlyph = sWrapperGlyph.glyph;
			}
			
			if (targetGlyph == null){
				return;
			}
			
			String targetClazz = targetGlyph.getClazz();
			
			addPort = false;
			if (targetClazz.equals("and")) {
				addPort =  true;
			} else if (targetClazz.equals("or")) {
				addPort =  true;
			} else if (targetClazz.equals("not")) {
				addPort =  true;
			}
			
			if (addPort){
				Bbox bbox = sWrapperGlyph.glyph.getBbox();
				Port port = new Port();
				port.setId("Port" + "_" + sourceId + "_" + targetId);
				port.setX(arc.getEnd().getX());
				port.setY(arc.getEnd().getY());
				
				targetGlyph.getPort().add(port);
				arc.setTarget(port);
			}
			

		}
		

	}
	
	/**
	 * Preserve information stored in SBML core into the Sbgn object
	 */
	public void createExtensionsForModelObjects(SBML2SBGNMLOutput sOutput){
		ListOf<FunctionDefinition> listOfFunctionDefinitions = null;
		ListOf<UnitDefinition> listOfUnitDefinitions = null;
		ListOf<Parameter> listOfParameters = null;
		ListOf<InitialAssignment> listOfInitialAssignments = null;
		ListOf<Rule> listOfRules = null;
		ListOf<Constraint> listOfConstraints = null;
		
		listOfUnitDefinitions = sOutput.listOfUnitDefinitions;
		listOfParameters = sOutput.listOfParameters;
		
		// the following contain MathML
		listOfFunctionDefinitions = sOutput.listOfFunctionDefinitions;
		listOfInitialAssignments = sOutput.listOfInitialAssignments;
		listOfRules = sOutput.listOfRules;
		listOfConstraints = sOutput.listOfConstraints;
		
		for (UnitDefinition ud: listOfUnitDefinitions){
			SBML2SBGNMLUtil.addSbaseInExtension(sOutput.sbgnObject, ud);
		}
		
		// TODO: add the rest
			
	}
	
	/**
	 * Preserve information stored in SBML qual into the Sbgn object
	 */
	public void createExtensionsForQualMathML(SBML2SBGNMLOutput sOutput){
		ListOf<QualitativeSpecies> listOfQualitativeSpecies = null;
		ListOf<Transition> listOfTransitions = null;
		listOfTransitions = sOutput.listOfTransitions;
		
		for (Transition tr: listOfTransitions){
			for (FunctionTerm ft: tr.getListOfFunctionTerms()){
				SBML2SBGNMLUtil.addMathMLInExtension(sOutput.sbgnObject, tr, ft);
			}
		}		
	}
	
	public static String splitString(String input, int start){
		// TODO: need a better splitting pattern
		String[] array = input.split("_");
		if (array.length == 3 && start == 1){
			return array[1]+"_"+array[2];
		}
		if (array.length == 2 && start == 1){
			return array[1];
		}		
		
		return null;
	}
	
	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException {
		
		String sbmlFileNameInput;
		String sbgnFileNameOutput;
		SBMLDocument sbmlDocument;
		SBML2SBGNML_GSOC2017 sbml2sbgnml;
		Sbgn sbgnObject;
		File file;
		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbml2sbgnml.SBML2SBGNML_GSOC2017 <SBML filename>. "
					+ "An example of relative path: /examples/sbml_layout_examples/GeneralGlyph_Example.xml");
		}
		
		String workingDirectory = System.getProperty("user.dir");

		sbmlFileNameInput = args[0];
		sbmlFileNameInput = workingDirectory + sbmlFileNameInput;	
		sbgnFileNameOutput = sbmlFileNameInput.replaceAll(".xml", "_SBGN-ML.sbgn");
		
		sbmlDocument = SBML2SBGNMLUtil.getSBMLDocument(sbmlFileNameInput);
		if (sbmlDocument == null) {
			throw new FileNotFoundException("The SBMLDocument is null");
		}
			
		sbml2sbgnml = new SBML2SBGNML_GSOC2017();
		// visualize JTree
//		try {		
//			sbml2sbgnml.SBML2SBGNMLUtil.visualizeJTree(sbmlDocument);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
		
		sbgnObject = sbml2sbgnml.convertToSBGNML(sbmlDocument);	
		
		file = new File(sbgnFileNameOutput);
		try {
			SbgnUtil.writeToFile(sbgnObject, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		System.out.println("sbml2sbgnml.SBML2SBGNML_GSOC2017.main " + "output file at: " + sbgnFileNameOutput);

	}	

	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {

		try {
			inputModel = model;
			SBMLDocument sbmlDoc = ((SBMLModel) model).getSBMLDocument();
			Sbgn sbgnObj = convertToSBGNML(sbmlDoc);
			SBGNModel outputModel = new SBGNModel(sbgnObj);
			return outputModel;
		} catch (SBMLException e) {
			e.printStackTrace();
			throw new ConversionException(e.getMessage());
		}
	}

	@Override
	public String getResultExtension() {
		return ".sbgn";
	}
	
	@Override
	public String getName() {
		return "SBML2SBGNML";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}
}
