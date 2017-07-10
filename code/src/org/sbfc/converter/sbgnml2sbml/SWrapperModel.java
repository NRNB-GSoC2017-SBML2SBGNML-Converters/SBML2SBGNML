package org.sbfc.converter.sbgnml2sbml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;

public class SWrapperModel {
	public Map map;

	HashMap<String, Glyph> processNodes;
	HashMap<String, Glyph> entityPoolNodes;
	HashMap<String, Glyph> compartments;
	HashMap<String, Glyph> logicOperators;
	
	HashMap<String, SWrapperArc> consumptionArcs;
	HashMap<String, SWrapperArc> productionArcs;
	HashMap<String, SWrapperArc> logicArcs;
	HashMap<String, SWrapperArc> modifierArcs;
	
	HashMap<String, SWrapperSpeciesGlyph> listOfWrapperSpeciesGlyphs;
	HashMap<String, SWrapperCompartmentGlyph> listOfWrapperCompartmentGlyphs;
	HashMap<String, SWrapperReactionGlyph> listOfWrapperReactionGlyphs;
	HashMap<String, SWrapperSpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	HashMap<String, SWrapperGeneralGlyph> listOfWrapperGeneralGlyphs;
	HashMap<String, SWrapperReferenceGlyph> listOfWrapperReferenceGlyphs;
	
	Model model;
	
	HashMap<String, String> portGlyphMap = new HashMap<String, String>();
	
	// keep track of how many Arcs are in Sbgn
	int numberOfArcs;
	// keep track of how many Glyphs are in Sbgn
	int numberOfGlyphs;	
	
	SWrapperModel(Model model, Map map) {
		this.processNodes = new HashMap<String, Glyph>();
		this.entityPoolNodes = new HashMap<String, Glyph>();
		this.compartments = new HashMap<String, Glyph>();
		this.logicOperators = new HashMap<String, Glyph>();
		
		this.consumptionArcs = new HashMap<String, SWrapperArc>();
		this.productionArcs = new HashMap<String, SWrapperArc>();
		this.logicArcs = new HashMap<String, SWrapperArc>();
		this.modifierArcs = new HashMap<String, SWrapperArc>();
			
		this.listOfWrapperSpeciesGlyphs = new HashMap<String, SWrapperSpeciesGlyph>();
		this.listOfWrapperCompartmentGlyphs = new HashMap<String, SWrapperCompartmentGlyph>();
		this.listOfWrapperReactionGlyphs = new HashMap<String, SWrapperReactionGlyph>();
		this.listOfWrapperSpeciesReferenceGlyphs = new HashMap<String, SWrapperSpeciesReferenceGlyph>();
		this.listOfWrapperReferenceGlyphs = new HashMap<String, SWrapperReferenceGlyph>();
		this.listOfWrapperGeneralGlyphs = new HashMap<String, SWrapperGeneralGlyph>();
		
		this.model = model;
		this.map = map;
		
		this.numberOfArcs = 0;
		this.numberOfGlyphs = 0;	
	}
	
	public Model getModel() {
		return model;
	}
	
	public Glyph getGlyph(String key) {
		Glyph glyph = entityPoolNodes.get(key);
		if (glyph == null){
			glyph = logicOperators.get(key);
		}
		
		return glyph;
	}
	
	public SWrapperSpeciesGlyph getWrapperSpeciesGlyph(String speciesId) {
		return listOfWrapperSpeciesGlyphs.get(speciesId);
	}
	public SWrapperCompartmentGlyph getWrapperCompartmentGlyph(String compartmentId) {
		return listOfWrapperCompartmentGlyphs.get(compartmentId);
	}
	public SWrapperReactionGlyph getWrapperReactionGlyph(String reactionId) {
		return listOfWrapperReactionGlyphs.get(reactionId);
	}
	public SWrapperGeneralGlyph getWrapperGeneralGlyph(String reactionId) {
		return listOfWrapperGeneralGlyphs.get(reactionId);
	}
	
	public void addSWrapperSpeciesGlyph(String speciesId, SWrapperSpeciesGlyph sWrapperSpeciesGlyph){
		listOfWrapperSpeciesGlyphs.put(speciesId, sWrapperSpeciesGlyph);
	}
	public void addSWrapperCompartmentGlyph(String compartmentId, SWrapperCompartmentGlyph sWrapperCompartmentGlyph){
		listOfWrapperCompartmentGlyphs.put(compartmentId, sWrapperCompartmentGlyph);
	}
	public void addSWrapperReactionGlyph(String reactionId, SWrapperReactionGlyph sWrapperReactionGlyph){
		listOfWrapperReactionGlyphs.put(reactionId, sWrapperReactionGlyph);
	}
	public void addWrapperGeneralGlyph(String glyphId, SWrapperGeneralGlyph sWrapperGeneralGlyph){
		listOfWrapperGeneralGlyphs.put(glyphId, sWrapperGeneralGlyph);
	}
	
	public void addSWrapperReferenceGlyph(String speciesRefId, SWrapperReferenceGlyph sWrapperReferenceGlyph){
		listOfWrapperReferenceGlyphs.put(speciesRefId, sWrapperReferenceGlyph);
	}
	public void addSWrapperSpeciesReferenceGlyph(String refId, SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph){
		listOfWrapperSpeciesReferenceGlyphs.put(refId, sWrapperSpeciesReferenceGlyph);
	}
	
	public SWrapperSpeciesReferenceGlyph getSWrapperSpeciesReferenceGlyph(String speciesRefId){
		return listOfWrapperSpeciesReferenceGlyphs.get(speciesRefId);
	}
	
	public SWrapperReferenceGlyph getSWrapperReferenceGlyph(String refId){
		return listOfWrapperReferenceGlyphs.get(refId);
	}
	
	private String checkGlyphId(String id, HashMap<String, Glyph> container) {
		numberOfGlyphs++;	
		if (id == null) {
			id = "Glyph_" + Integer.toString(numberOfGlyphs);
			return id;
		} else if (container.get(id) != null) {
			id = "Glyph_" + Integer.toString(numberOfGlyphs);
			return id;			
		}

		return id;
	}
	
	private String checkArcId(String id, HashMap<String, SWrapperArc> container) {
		numberOfArcs++;
		if (id == null) {
			return "Arc_" + Integer.toString(numberOfArcs);
		} else if (container.get(id) != null) {
			return "Arc_" + Integer.toString(numberOfArcs);		
		}

		return id;
	}	
	
	public void addSbgnProcessNode(String id, Glyph glyph) {
		id = checkGlyphId(id, processNodes);
		processNodes.put(id, glyph);
		updatePortGlyphMap(glyph);
	}
	
	public void addSbgnCompartment(String id, Glyph glyph) {
		id = checkGlyphId(id, compartments);
		compartments.put(id, glyph);
		updatePortGlyphMap(glyph);
	}
	
	public void addSbgnEntityPoolNode(String id, Glyph glyph) {
		id = checkGlyphId(id, entityPoolNodes);
		entityPoolNodes.put(id, glyph);
		updatePortGlyphMap(glyph);		
	}
	
	public void addSbgnLogicOperator(String id, Glyph glyph) {
		id = checkGlyphId(id, logicOperators);
		logicOperators.put(id, glyph);
		updatePortGlyphMap(glyph);		
	}
	
	public void addConsumptionArc(String id, SWrapperArc arc) {
		id = checkArcId(id, consumptionArcs);
		consumptionArcs.put(id, arc);
	}
	public void addProductionArc(String id, SWrapperArc arc) {
		id = checkArcId(id, consumptionArcs);
		productionArcs.put(id, arc);
	}
	public void addLogicArc(String id, SWrapperArc arc) {
		id = checkArcId(id, logicArcs);
		logicArcs.put(id, arc);
	}
	public void addModifierArc(String id, SWrapperArc arc) {
		id = checkArcId(id, modifierArcs);
		modifierArcs.put(id, arc);
	}
	
	public void updatePortGlyphMap(Glyph glyph){
		List<Port> listOfPorts;
		listOfPorts = glyph.getPort();
		for (Port port: listOfPorts) {
			portGlyphMap.put(port.getId(), glyph.getId());
		}
	}
	
	public String findGlyphFromPort(Port port) {
		return portGlyphMap.get(port.getId());
	}
		
				
}
