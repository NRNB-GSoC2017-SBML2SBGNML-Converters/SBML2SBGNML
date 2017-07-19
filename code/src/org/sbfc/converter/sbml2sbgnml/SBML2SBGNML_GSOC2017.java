package org.sbfc.converter.sbml2sbgnml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;

import javax.xml.bind.JAXBException;

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
import org.sbgn.bindings.Sbgn;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
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
import org.xml.sax.SAXException;

public class SBML2SBGNML_GSOC2017 extends GeneralConverter { 
	
	private static Logger logger;
	public SBML2SBGNMLUtil sUtil;
	public SBML2SBGNMLOutput sOutput;
	public SWrapperMap sWrapperMap;
			
	/**
	 * Initialize the converter with a SBML2SBGNMLUtil and a SBML2SBGNMLOutput
	 * 
	 * @param <code>SBMLDocument</code> sbmlDocument
	 */	
	public SBML2SBGNML_GSOC2017(SBMLDocument sbmlDocument) {
		logger = Logger.getLogger(SBML2SBGNML_GSOC2017.class);
		
		sUtil = new SBML2SBGNMLUtil();
		sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		sWrapperMap = new SWrapperMap(sOutput.map, sOutput.sbmlModel);
	}
	
	/**
	 * Create an <code>Sbgn</code>, which corresponds to an SBML <code>Model</code>. 
	 * Each element in the <code>Model</code> is mapped to an SBGN <code>Glyph</code>.
	 * 
	 * @param <code>SBMLDocument</code> sbmlDocument
	 * @return <code>Sbgn</code> sbgnObject
	 */			
	public Sbgn convertToSBGNML(SBMLDocument sbmlDocument) throws SBMLException {
	
		// note: the order of execution matters
		createFromCompartmentGlyphs(sOutput.sbgnObject, sOutput.listOfCompartmentGlyphs);
		createFromSpeciesGlyphs(sOutput.sbgnObject, sOutput.listOfSpeciesGlyphs);
		createFromGeneralGlyphs(sOutput.sbgnObject, sOutput.listOfAdditionalGraphicalObjects);
		createLabelsFromTextGlyphs(sOutput.sbgnObject, sOutput.listOfTextGlyphs);
		createFromReactionGlyphs(sOutput.sbgnObject, sOutput.listOfReactionGlyphs);

		// return one sbgnObjects
		return sOutput.sbgnObject;		
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, each corresponding to an SBML <code>CompartmentGlyph</code>. 
	 * TODO: many things still need to be handled, see comments below
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<CompartmentGlyph></code> listOfCompartmentGlyphs
	 */			
	public void createFromCompartmentGlyphs(Sbgn sbgnObject, ListOf<CompartmentGlyph> listOfCompartmentGlyphs) {
		Glyph sbgnCompartmentGlyph;
		
		if (listOfCompartmentGlyphs == null){return;}
		
		for (CompartmentGlyph compartmentGlyph : listOfCompartmentGlyphs){
			sbgnCompartmentGlyph = createFromOneCompartmentGlyph(sbgnObject, compartmentGlyph);
			
			// add the created Glyph to the output
			sOutput.addGlyphToMap(sbgnCompartmentGlyph);
		}
		
	}
	
	public Glyph createFromOneCompartmentGlyph(Sbgn sbgnObject, CompartmentGlyph compartmentGlyph){
		Glyph sbgnCompartmentGlyph;
		
		// create a new Glyph, set its Bbox, but don't set a Label
		sbgnCompartmentGlyph = sUtil.createGlyph(compartmentGlyph.getId(), "compartment", 
				true, compartmentGlyph, 
				false, compartmentGlyph.getCompartment());	
		
		// todo: create Auxiliary items?
		// todo: need to keep compartment name
		
		return sbgnCompartmentGlyph;
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, each corresponding to an SBML <code>SpeciesGlyph</code>. 
	 * TODO: many things still need to be handled, see comments below
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<SpeciesGlyph></code> listOfSpeciesGlyphs
	 */		
	public void createFromSpeciesGlyphs(Sbgn sbgnObject, ListOf<SpeciesGlyph> listOfSpeciesGlyphs) {
		SWrapperGlyphEntityPool sbgnSpeciesGlyph;
		
		if (listOfSpeciesGlyphs == null){return;}
		
		for (SpeciesGlyph speciesGlyph : listOfSpeciesGlyphs){
			sbgnSpeciesGlyph = createFromOneSpeciesGlyph(sbgnObject, speciesGlyph);
			
			sOutput.addGlyphToMap(sbgnSpeciesGlyph.glyph);
			sWrapperMap.listOfSWrapperGlyphEntityPools.put(sbgnSpeciesGlyph.id, sbgnSpeciesGlyph);
		}
	}
	
	public SWrapperGlyphEntityPool createFromOneSpeciesGlyph(Sbgn sbgnObject, SpeciesGlyph speciesGlyph){
		Glyph sbgnSpeciesGlyph;
		SWrapperGlyphEntityPool sWrapperGlyphEntityPool;
		
		// create a new Glyph, set its Bbox, set a Label
		// todo: or clazz could be simple chemical etc.
		sbgnSpeciesGlyph = sUtil.createGlyph(speciesGlyph.getId(), "macromolecule", 
				true, speciesGlyph, 
				false, speciesGlyph.getSpecies());	
		
		// todo: create Auxiliary items?
		
		sWrapperGlyphEntityPool = new SWrapperGlyphEntityPool(sbgnSpeciesGlyph, (Species) speciesGlyph.getSpeciesInstance(), speciesGlyph);
		
		return sWrapperGlyphEntityPool;
	}	
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, each corresponding to an SBML <code>ReactionGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<ReactionGlyph></code> listOfReactionGlyphs
	 */			
	public void createFromReactionGlyphs(Sbgn sbgnObject, ListOf<ReactionGlyph> listOfReactionGlyphs) {
		SWrapperArcGroup sbgnReactionGlyph;
		SWrapperGlyphProcess sWrapperGlyphProcess;

		if (listOfReactionGlyphs == null){return;}
		
		for (ReactionGlyph reactionGlyph : listOfReactionGlyphs){
			// todo: return an ArcGroup instead
			sbgnReactionGlyph = createFromOneReactionGlyph(sbgnObject, reactionGlyph);
			
			sWrapperGlyphProcess = new SWrapperGlyphProcess(sbgnReactionGlyph, reactionGlyph, 
										(Reaction) reactionGlyph.getReactionInstance(), null, 
										// the first Glyph is the Process Node
										sbgnReactionGlyph.arcGroup.getGlyph().get(0));
			sWrapperMap.listOfSWrapperGlyphProcesses.put(sbgnReactionGlyph.reactionId, sWrapperGlyphProcess);
			System.out.println("sbgnReactionGlyph.reactionId "+sbgnReactionGlyph.reactionId);
		}		
	}
	
	/**
	 * Create SBGN <code>Glyph</code> and <code>Arc</code>, corresponding to an SBML <code>ReactionGlyph</code>. 
	 * TODO: many things still need to be handled, see comments below
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ReactionGlyph</code> reactionGlyph
	 */		
	public SWrapperArcGroup createFromOneReactionGlyph(Sbgn sbgnObject, ReactionGlyph reactionGlyph) {
		Arcgroup processNode = null;
		Curve sbmlCurve;
		ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs;
		Reaction reaction;
		SWrapperArcGroup sWrapperArcGroup = null;
		
		if (reactionGlyph.isSetReaction()) {
			System.out.println("here");
			// create a process node from dimensions of the curve
			// todo: change to more robust method
			// todo: create Auxiliary items?	
			if (reactionGlyph.isSetCurve()) {
				sbmlCurve = reactionGlyph.getCurve();
				processNode = sUtil.createOneProcessNode(reactionGlyph.getReaction(), sbmlCurve);
				sOutput.addArcgroupToMap(processNode);
				sWrapperArcGroup = new SWrapperArcGroup(reactionGlyph.getReaction(), processNode);
				//System.out.println("reactionGlyph.getReaction() "+reactionGlyph.getReaction());
			}
			
			listOfSpeciesReferenceGlyphs = reactionGlyph.getListOfSpeciesReferenceGlyphs();
			if (listOfSpeciesReferenceGlyphs.size() > 0) {
				
				createFromSpeciesReferenceGlyphs(listOfSpeciesReferenceGlyphs, processNode.getGlyph().get(0));
			}	
			
			// store any additional information into SBGN
			reaction = (Reaction) reactionGlyph.getReactionInstance();
			if (reaction.getKineticLaw() != null && reactionGlyph.isSetCurve()) {

				String math = reaction.getKineticLaw().getMathMLString();
				sUtil.addExtensionElement(processNode, math);
			}
		}
		
		return sWrapperArcGroup;
	}
	
	public void createFromSpeciesReferenceGlyphs(ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs, Glyph reactionGlyph) {
		Arc arc;
		SWrapperArc sWrapperArc;
		
		if (listOfSpeciesReferenceGlyphs == null){return;}
		
		for (SpeciesReferenceGlyph speciesReferenceGlyph : listOfSpeciesReferenceGlyphs){
			sUtil.printHelper("createGlyphFromReactionGlyph", 
					String.format("speciesGlyph = %s, speciesReference = %s \n", 
					speciesReferenceGlyph.getSpeciesGlyph(), speciesReferenceGlyph.getSpeciesReference()));
			
			sWrapperArc = createFromOneSpeciesReferenceGlyph(speciesReferenceGlyph, reactionGlyph);
			// store the created Arc into SBGN
			sOutput.addArcToMap(sWrapperArc.arc);	
			
			sWrapperMap.listOfSWrapperArcs.put(sWrapperArc.id, sWrapperArc);
		}		
	}
	
	public SWrapperArc createFromOneSpeciesReferenceGlyph(SpeciesReferenceGlyph speciesReferenceGlyph, Glyph reactionGlyph){
		Arc arc;
		Curve sbmlCurve;
		SWrapperArc sWrapperArc;
		
		sbmlCurve = speciesReferenceGlyph.getCurve();
		
		// create an Arc for the SpeciesReferenceGlyph
		// todo: need source/target glyphs without violating syntax? i.e process nodes always connect arcs?
		// can't do need bezier, libSBGN doesn't have it
		// need port
		arc = sUtil.createOneArc(sbmlCurve);
		
		// set Clazz of the Arc
		// todo: need to determine from getOutputFromClass between production/consumption etc			
		String clazz = sUtil.searchForReactionRole(speciesReferenceGlyph.getSpeciesReferenceRole());
		arc.setClazz(clazz);
				
		SimpleSpeciesReference simpleSpeciesReference = (SimpleSpeciesReference) speciesReferenceGlyph.getSpeciesReferenceInstance();
		if (simpleSpeciesReference instanceof SpeciesReference){
			sWrapperArc = new SWrapperArc(arc, speciesReferenceGlyph,
					(SpeciesReference) simpleSpeciesReference);			
		} else {
			sWrapperArc = new SWrapperArc(arc, speciesReferenceGlyph,
					(ModifierSpeciesReference) simpleSpeciesReference);			
		}

		String sourceTargetType;
		String reactionId = speciesReferenceGlyph.getSpeciesReferenceInstance().getParentSBMLObject().getParentSBMLObject().getId();
		//System.out.println("reactionId: "+ reactionId);
		
		String speciesId = null;
		Glyph glyph = null;
		try{
		speciesId = speciesReferenceGlyph.getSpeciesGlyphInstance().getSpecies();
		glyph = sWrapperMap.listOfSWrapperGlyphEntityPools.get(speciesId).glyph;
		} catch (Exception e){}
		
		if (clazz.equals("production")){
			sourceTargetType="reactionToSpecies";

			Port port = new Port();
			port.setId("Port" + "_" + reactionId + "_" + speciesId);
			port.setX(arc.getStart().getX());
			port.setY(arc.getStart().getY());
			reactionGlyph.getPort().add(port);
			arc.setSource(port);
			
			arc.setTarget(glyph);
			
		}else{
			sourceTargetType="speciesToReaction";

			Port port = new Port();
			port.setId("Port" + "_" + speciesId + "_" + reactionId);
			port.setX(arc.getEnd().getX());
			port.setY(arc.getEnd().getY());
			reactionGlyph.getPort().add(port);
			arc.setTarget(port);
			
			arc.setSource(glyph);
		}
		sWrapperArc.setSourceTarget(reactionId, speciesId, sourceTargetType);
				
		return sWrapperArc;
	}
	
	/**
	 * Create multiple SBGN <code>Label</code>, each corresponding to an SBML <code>TextGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<TextGlyph></code> listOfTextGlyphs
	 */			
	public void createLabelsFromTextGlyphs(Sbgn sbgnObject, ListOf<TextGlyph> listOfTextGlyphs) {
		
		if (listOfTextGlyphs == null){return;}
		
		for (TextGlyph textGlyph : listOfTextGlyphs){	
			createLabelFromOneTextGlyph(textGlyph);
		}
	}
	
	public void createLabelFromOneTextGlyph(TextGlyph textGlyph) {
		Glyph sbgnGlyph;
		String id = null;
		String text;
		List<Glyph> listOfGlyphs;
		int indexOfSpeciesGlyph;		
		
		if (textGlyph.isSetText()) {
			text = textGlyph.getText();
		} else if (textGlyph.isSetOriginOfText()) {
			// todo: don't get the reference, get the text instead
			text = textGlyph.getOriginOfText();
		} else {
			text = "";
		}
		
		if (textGlyph.isSetGraphicalObject()) {
			id = textGlyph.getGraphicalObjectInstance().getId();
			listOfGlyphs = sOutput.sbgnObject.getMap().getGlyph();
			
			// find the Glyph that should contain this text
			indexOfSpeciesGlyph = sUtil.searchForIndex(listOfGlyphs, id);
			sbgnGlyph = listOfGlyphs.get(indexOfSpeciesGlyph);
			
			sUtil.setLabel(sbgnGlyph, text);
			
		} else {
			// clazz is unknown
			sbgnGlyph = sUtil.createGlyph(textGlyph.getId(), "unspecified entity", false, null, true, text);
			sUtil.createVoidBBox(sbgnGlyph);
			// add this new glyph to Map
			sOutput.addGlyphToMap(sbgnGlyph);		
					
		}		
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code>, corresponding to SBML <code>GeneralGlyph</code>. 
	 * Each <code>GraphicalObject</code> is casted to <code>GeneralGlyph</code>. 
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<GraphicalObject></code> listOfAdditionalGraphicalObjects
	 */		
	public void createFromGeneralGlyphs(Sbgn sbgnObject, ListOf<GraphicalObject> listOfAdditionalGraphicalObjects) {
		GeneralGlyph generalGlyph;
		BoundingBox bbox;
		Arcgroup arcgroup;
		
		if (listOfAdditionalGraphicalObjects == null){return;}
		
		// treat each GeneralGlyph like a ReactionGlyph
		for (GraphicalObject graphicalObject : listOfAdditionalGraphicalObjects) {
			bbox = graphicalObject.getBoundingBox();
			generalGlyph = (GeneralGlyph) graphicalObject;

			arcgroup = createFromOneGeneralGlyph(sbgnObject, generalGlyph);
			sOutput.addArcgroupToMap(arcgroup);
		}
	}
	
	/**
	 * Create multiple SBGN <code>Glyph</code> from an SBML <code>GeneralGlyph</code>. 
	 * TODO: handle isSetCurve and isSetListOfSubGlyphs. Iterate the listOfSubGlyphs.
	 * 
	 * @param <code>Sbgn</code> sbgnObject
	 * @param <code>ListOf<GraphicalObject></code> listOfAdditionalGraphicalObjects
	 */		
	public Arcgroup createFromOneGeneralGlyph(Sbgn sbgnObject, GeneralGlyph generalGlyph){
		ListOf<ReferenceGlyph> listOfReferenceGlyphs;
		ListOf<GraphicalObject> listOfSubGlyphs;	
		
		List<Glyph> listOfGlyphs = new ArrayList<Glyph>();
		List<Arc> listOfArcs = new ArrayList<Arc>();
		Glyph glyph;
		Arc arc;
		Arcgroup arcgroup;
		Arcgroup processNode;
		Curve sbmlCurve;
		
		if (generalGlyph.isSetCurve()){
			sbmlCurve = generalGlyph.getCurve();
			processNode = sUtil.createOneProcessNode(generalGlyph.getId(), sbmlCurve);
			sOutput.addArcgroupToMap(processNode);	
		}
		if (generalGlyph.isSetListOfReferenceGlyphs()){
			listOfReferenceGlyphs = generalGlyph.getListOfReferenceGlyphs();
			
			for (ReferenceGlyph referenceGlyph : listOfReferenceGlyphs) {
				arc = createFromOneReferenceGlyph(referenceGlyph);
				listOfArcs.add(arc);
			}
		}
		if (generalGlyph.isSetListOfSubGlyphs()){
			listOfSubGlyphs = generalGlyph.getListOfSubGlyphs();
			
			for (GraphicalObject graphicalObject : listOfSubGlyphs) {
				glyph = createFromOneGraphicalObject(sbgnObject, graphicalObject);
				listOfGlyphs.add(glyph);
			}
		}	
		
		arcgroup = sUtil.createOneArcgroup(listOfGlyphs, listOfArcs, generalGlyph.getId());
		return arcgroup;
	}
	
	public Arc createFromOneReferenceGlyph(ReferenceGlyph referenceGlyph){
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
		
		sUtil.printHelper("createGlyphsFromGeneralGlyphs", 
				String.format("id=%s, glyph=%s, reference=%s, role=%s \n", 
				referenceGlyphId, glyph, reference, role));
		
		sbmlCurve = referenceGlyph.getCurve();
		// todo: this is wrong, need to create one arc for each referenceGlyph, not for each curveSegment
		// todo: need source/target glyphs without violating syntax? i.e process nodes always connect arcs?
		// can't do need bezier, libSBGN doesn't have it
		// need port
		arc = sUtil.createOneArc(sbmlCurve);
		
		// todo: need to determine from getOutputFromClass between production/consumption etc
		String clazz = sUtil.searchForReactionRole(role);
		arc.setClazz(clazz);
		
		// todo: set Arc referenceGlyphId, glyph, and reference
				
		return arc;
	}
	
	public Glyph createFromOneGraphicalObject(Sbgn sbgnObject, GraphicalObject graphicalObject){
		Glyph sbgnGlyph;
		
		// create a new Glyph, set its Bbox, don't set a Label
		// todo: or clazz could be simple chemical etc.
		sbgnGlyph = sUtil.createGlyph(graphicalObject.getId(), "macromolecule", 
				true, graphicalObject, 
				false, null);	
		// todo: set a Label
		
		// todo: create Auxiliary items?
		
		return sbgnGlyph;
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
			
		sbml2sbgnml = new SBML2SBGNML_GSOC2017(sbmlDocument);
		// visualize JTree
		try {		
			sbml2sbgnml.sUtil.visualizeJTree(sbmlDocument);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		sbgnObject = sbml2sbgnml.convertToSBGNML(sbmlDocument);	
		
		file = new File(sbgnFileNameOutput);
		try {
			SbgnUtil.writeToFile(sbgnObject, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		//sbml2sbgnml.printHelper("sbml2sbgnml.SBML2SBGNML_GSOC2017.main", "output file at: " + sbgnFileNameOutput);

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
