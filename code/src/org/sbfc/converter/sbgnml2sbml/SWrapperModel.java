package org.sbfc.converter.sbgnml2sbml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.sbfc.converter.sbgnml2sbml.qual.SWrapperQualitativeSpecies;
import org.sbfc.converter.sbgnml2sbml.qual.SWrapperTransition;
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

/**
 * A SWrapperModel Model wrapper stores the SBML Model as well as some objects contained in the Model.
 * Example: Species, Reaction, Compartment, SpeciesGlyph, ReactionGlyph, CompartmentGlyph, etc. and their
 * corresponding glyphs in the Sbgn model.
 * SWrapperModel allows to retrieve information easily without having to search in the Model
 * SWrapperModel stores SWrapperSpeciesGlyph, SWrapperReactionGlyph, etc, which are Wrappers for SpeciesGlyph, ReactionGlyph, etc.
 * @author haoran
 *
 */
public class SWrapperModel {
	// the Sbgn map
	public Map map;

	// we classified all the glyphs and arcs in an Sbgn model into the following categories.
	// we iterate each of these lists to convert and construct our SBML layout glyphs
	public HashMap<String, Glyph> processNodes;
	public HashMap<String, Glyph> entityPoolNodes;
	public HashMap<String, Glyph> compartments;
	public HashMap<String, Glyph> logicOperators;
	public HashMap<String, Glyph> annotations;
	
	public HashMap<String, SWrapperArc> consumptionArcs;
	public HashMap<String, SWrapperArc> productionArcs;
	public HashMap<String, SWrapperArc> logicArcs;
	public HashMap<String, SWrapperArc> modifierArcs;
	
	// stores all the wrappers we created as we interpret the Sbgn model, each wrapper stores meta information 
	// of the mapping from Sbgn glyphs to SBML layout glyphs
	public HashMap<String, SWrapperSpeciesGlyph> listOfWrapperSpeciesGlyphs;
	public HashMap<String, SWrapperCompartmentGlyph> listOfWrapperCompartmentGlyphs;
	public HashMap<String, SWrapperReactionGlyph> listOfWrapperReactionGlyphs;
	public HashMap<String, SWrapperSpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	public HashMap<String, SWrapperGeneralGlyph> listOfWrapperGeneralGlyphs;
	public HashMap<String, SWrapperReferenceGlyph> listOfWrapperReferenceGlyphs;
	public HashMap<String, SWrapperQualitativeSpecies> listOfSWrapperQualitativeSpecies;
	public HashMap<String, SWrapperTransition> listOfSWrapperTransitions;
	
	// The model that we will use to generate the output converted file
	Model model;
	
	// Stores mapping from a port id to a parent glyph id
	HashMap<String, String> portGlyphMap = new HashMap<String, String>();
	
	// stores the compartmentOrder values of all the compartment glyphs in the Sbgn model
	HashMap<String, Float> compartmentOrderList = new HashMap<String, Float>();
	
	// Maps a SWrapperGeneralGlyph id to a SWrapperTransition id
	HashMap<String, String> generalGlyphTransitionMap = new HashMap<String, String>();
	
	// Maps a TextGlyph id to the layout Glyph that contains the text.
	HashMap<String, String> textSourceMap = new HashMap<String, String>();
	
	// keep track of how many Arcs are in Sbgn
	int numberOfArcs;
	// keep track of how many Glyphs are in Sbgn
	int numberOfGlyphs;	
	
	SWrapperModel(Model model, Map map) {
		this.processNodes = new HashMap<String, Glyph>();
		this.entityPoolNodes = new HashMap<String, Glyph>();
		this.compartments = new HashMap<String, Glyph>();
		this.logicOperators = new HashMap<String, Glyph>();
		this.annotations = new HashMap<String, Glyph>();
		
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
		this.listOfSWrapperQualitativeSpecies = new HashMap<String, SWrapperQualitativeSpecies>();
		this.listOfSWrapperTransitions = new HashMap<String, SWrapperTransition>();
		
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
		} if (glyph == null){
			glyph = processNodes.get(key);
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
	
	/**
	 * Stores mapping from a port id to a parent glyph id
	 * @param glyph
	 */
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

	public void addSWrapperQualitativeSpecies(String id, SWrapperQualitativeSpecies sWrapperQualitativeSpecies) {
		listOfSWrapperQualitativeSpecies.put(id, sWrapperQualitativeSpecies);
	}

	public void addSWrapperTransition(String id, SWrapperTransition sWrapperTransition) {
		listOfSWrapperTransitions.put(id, sWrapperTransition);
	}
	
	public SWrapperQualitativeSpecies getSWrapperQualitativeSpecies(String id) {
		return listOfSWrapperQualitativeSpecies.get(id);
	}
		
	public void sortCompartmentOrderList(){	
		for (String key : listOfWrapperCompartmentGlyphs.keySet()){
			SWrapperCompartmentGlyph sWrapperCompartmentGlyph = getWrapperCompartmentGlyph(key);
			compartmentOrderList.put(key, sWrapperCompartmentGlyph.compartmentOrder);
		}
		compartmentOrderList = sortByValue(compartmentOrderList);
	}
	
	/**
	 * https://www.mkyong.com/java/how-to-sort-a-map-in-java/
	 * @param unsortMap
	 * @return
	 */
	private static HashMap<String, Float> sortByValue(HashMap<String, Float> unsortMap) {

        // 1. Convert Map to List of Map
        List<HashMap.Entry<String, Float>> list =
                new LinkedList<HashMap.Entry<String, Float>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<HashMap.Entry<String, Float>>() {
            public int compare(HashMap.Entry<String, Float> o1,
            					HashMap.Entry<String, Float> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        HashMap<String, Float> sortedMap = new LinkedHashMap<String, Float>();
        for (HashMap.Entry<String, Float> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

	public void addAnnotation(String id, Glyph glyph) {
		id = checkGlyphId(id, annotations);
		annotations.put(id, glyph);		
	}
	
	public void addToGeneralGlyphTransitionMap(String generalGlyphId, String transitionId){
		generalGlyphTransitionMap.put(generalGlyphId, transitionId);
	}
	
	public String getFromGeneralGlyphTransitionMap(String generalGlyphId){
		return generalGlyphTransitionMap.get(generalGlyphId);
	}
				
}
