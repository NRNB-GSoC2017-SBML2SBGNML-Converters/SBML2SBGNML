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
	HashMap<String, Arc> inwardArcs;
	HashMap<String, Arc> outwardArcs;
	HashMap<String, Arc> undirectedArcs;
	
	List<SWrapperSpeciesGlyph> listOfWrapperSpeciesGlyphs;
	List<SWrapperCompartmentGlyph> listOfWrapperCompartmentGlyphs;
	List<SWrapperReactionGlyph> listOfWrapperReactionGlyphs;
	List<SWrapperSpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	List<SWrapperGeneralGlyph> listOfWrapperGeneralGlyphs;
	
	Model model;
	
	HashMap<String, String> portToGlyphMap = new HashMap<String, String>();
	
	// keep track of how many Arcs are in Sbgn
	int numberOfArcs;
	// keep track of how many Glyphs are in Sbgn
	int numberOfGlyphs;	
	
	SWrapperModel(Model model, Map map) {
		this.processNodes = new HashMap<String, Glyph>();
		this.entityPoolNodes = new HashMap<String, Glyph>();
		this.compartments = new HashMap<String, Glyph>();
		this.logicOperators = new HashMap<String, Glyph>();
		
		this.inwardArcs = new HashMap<String, Arc>();
		this.outwardArcs = new HashMap<String, Arc>();
		this.undirectedArcs = new HashMap<String, Arc>();			
		
		this.listOfWrapperSpeciesGlyphs = new ArrayList<SWrapperSpeciesGlyph>();
		this.listOfWrapperCompartmentGlyphs = new ArrayList<SWrapperCompartmentGlyph>();
		this.listOfWrapperReactionGlyphs = new ArrayList<SWrapperReactionGlyph>();
		this.listOfWrapperSpeciesReferenceGlyphs = new ArrayList<SWrapperSpeciesReferenceGlyph>();
		this.listOfWrapperGeneralGlyphs = new ArrayList<SWrapperGeneralGlyph>();
		
		this.model = model;
		
		this.numberOfArcs = 0;
		this.numberOfGlyphs = 0;	
	}
	
	public Model getModel() {
		return model;
	}
	
//	public List<SWrapperSpeciesGlyph> getListOfWrapperSpeciesGlyphs() {
//		return listOfWrapperSpeciesGlyphs;
//	}
//	public List<SWrapperCompartmentGlyph> getListOfWrapperCompartmentGlyphs() {
//		return listOfWrapperCompartmentGlyphs;
//	}
//	public List<SWrapperReactionGlyph> getListOfWrapperReactionGlyphs() {
//		return listOfWrapperReactionGlyphs;
//	}
	
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
		updatePortToGlyphMap(glyph);
	}
	
	public void addSbgnCompartment(String id, Glyph glyph) {
		id = checkGlyphId(id, compartments);
		compartments.put(id, glyph);
		updatePortToGlyphMap(glyph);
	}
	
	public void addSbgnEntityPoolNode(String id, Glyph glyph) {
		id = checkGlyphId(id, entityPoolNodes);
		entityPoolNodes.put(id, glyph);
		updatePortToGlyphMap(glyph);		
	}
	
	public void addSbgnLogicOperator(String id, Glyph glyph) {
		id = checkGlyphId(id, logicOperators);
		logicOperators.put(id, glyph);
		updatePortToGlyphMap(glyph);		
	}
	
	public void addSbgnUndirectedArc(String id, Arc arc) {
		id = checkArcId(id, undirectedArcs);
		undirectedArcs.put(id, arc);
	}
	
	public void addSbgnInwardArc(String id, Arc arc) {
		id = checkArcId(id, inwardArcs);
		inwardArcs.put(id, arc);		
	}
	
	public void addSbgnOutwardArc(String id, Arc arc) {
		id = checkArcId(id, outwardArcs);
		outwardArcs.put(id, arc);		
	}
	
	public void updatePortToGlyphMap(Glyph glyph){
		List<Port> listOfPorts;
		listOfPorts = glyph.getPort();
		for (Port port: listOfPorts) {
			portToGlyphMap.put(port.getId(), glyph.getId());
		}
	}
	
	public String findGlyphFromPort(Port port) {
		return portToGlyphMap.get(port.getId());
	}
	
	
	
				
}
