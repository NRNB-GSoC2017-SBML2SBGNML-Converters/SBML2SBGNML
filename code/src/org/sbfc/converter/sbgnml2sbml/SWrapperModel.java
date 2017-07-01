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
	Map map;

	HashMap<String, Glyph> processNodes;
	HashMap<String, Glyph> entityPoolNodes;
	HashMap<String, Glyph> compartments;
	HashMap<String, Glyph> logicOperators;
	
	// see code below for definition of inwardArcs, outwardArcs, and undirectedArcs
	HashMap<String, Arc> glyphToPortArcs;
	HashMap<String, Arc> portToGlyphArcs;
	HashMap<String, Arc> glyphToGlyphArcs;
	HashMap<String, Arc> logicArcs;
	
	HashMap<String, SWrapperSpeciesGlyph> listOfWrapperSpeciesGlyphs;
	HashMap<String, SWrapperCompartmentGlyph> listOfWrapperCompartmentGlyphs;
	HashMap<String, SWrapperReactionGlyph> listOfWrapperReactionGlyphs;
	HashMap<String, SWrapperSpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	HashMap<String, SWrapperGeneralGlyph> listOfWrapperGeneralGlyphs;
	
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
		
		this.glyphToPortArcs = new HashMap<String, Arc>();
		this.portToGlyphArcs = new HashMap<String, Arc>();
		this.glyphToGlyphArcs = new HashMap<String, Arc>();
		this.logicArcs = new HashMap<String, Arc>();
			
		this.listOfWrapperSpeciesGlyphs = new HashMap<String, SWrapperSpeciesGlyph>();
		this.listOfWrapperCompartmentGlyphs = new HashMap<String, SWrapperCompartmentGlyph>();
		this.listOfWrapperReactionGlyphs = new HashMap<String, SWrapperReactionGlyph>();
		this.listOfWrapperSpeciesReferenceGlyphs = new HashMap<String, SWrapperSpeciesReferenceGlyph>();
		this.listOfWrapperGeneralGlyphs = new HashMap<String, SWrapperGeneralGlyph>();
		
		this.model = model;
		this.map = map;
		
		this.numberOfArcs = 0;
		this.numberOfGlyphs = 0;	
	}
	
	public Model getModel() {
		return model;
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
	
	private String checkArcId(String id, HashMap<String, Arc> container) {
		numberOfArcs++;
		if (id == null) {
			id = "Arc_" + Integer.toString(numberOfArcs);
			return id;
		} else if (container.get(id) != null) {
			id = "Arc_" + Integer.toString(numberOfArcs);
			return id;			
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
	
	public void addGlyphToGlyphArc(String id, Arc arc) {
		id = checkArcId(id, glyphToGlyphArcs);
		glyphToGlyphArcs.put(id, arc);
	}
	public void addGlyphToPortArc(String id, Arc arc) {
		id = checkArcId(id, glyphToPortArcs);
		glyphToPortArcs.put(id, arc);		
	}
	public void addPortToGlyphArc(String id, Arc arc) {
		id = checkArcId(id, portToGlyphArcs);
		portToGlyphArcs.put(id, arc);		
	}
	public void addLogicArc(String id, Arc arc) {
		id = checkArcId(id, logicArcs);
		logicArcs.put(id, arc);
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
